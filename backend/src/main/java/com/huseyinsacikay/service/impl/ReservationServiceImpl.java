package com.huseyinsacikay.service.impl;

import com.huseyinsacikay.dto.request.ReservationCreateRequest;
import com.huseyinsacikay.dto.request.ReservationUpdateRequest;
import com.huseyinsacikay.dto.response.ReservationResponse;
import com.huseyinsacikay.dto.response.UserResponse;
import com.huseyinsacikay.entity.Pitch;
import com.huseyinsacikay.entity.Reservation;
import com.huseyinsacikay.entity.ReservationStatus;
import com.huseyinsacikay.entity.ReservationParticipant;
import com.huseyinsacikay.entity.User;
import com.huseyinsacikay.exception.BadRequestException;
import com.huseyinsacikay.exception.ConflictException;
import com.huseyinsacikay.exception.MessageType;
import com.huseyinsacikay.exception.NotFoundException;
import com.huseyinsacikay.repository.PitchRepository;
import com.huseyinsacikay.repository.ReservationRepository;
import com.huseyinsacikay.repository.ReservationParticipantRepository;
import com.huseyinsacikay.repository.UserRepository;
import com.huseyinsacikay.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.huseyinsacikay.security.reservation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private static final List<ReservationStatus> ACTIVE_RESERVATION_STATUSES = List.of(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED);

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final PitchRepository pitchRepository;
    private final ReservationParticipantRepository reservationParticipantRepository;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request) {
        validateReservationTimes(request);

        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(request.getUserId())) {
            throw new AccessDeniedException(MessageType.ACCESS_DENIED.getMessage());
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));

        Pitch pitch = pitchRepository.findByIdForUpdate(request.getPitchId())
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));

        if (!pitch.isAvailable()) {
            throw new ConflictException(MessageType.PITCH_NOT_AVAILABLE);
        }

        boolean hasOverlap = reservationRepository.existsByPitchIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                pitch.getId(),
                ACTIVE_RESERVATION_STATUSES,
                request.getEndTime(),
                request.getStartTime());
        if (hasOverlap) {
            throw new ConflictException(MessageType.RESERVATION_TIME_OVERLAP);
        }

        BigDecimal totalPrice = calculateTotalPrice(pitch, request.getStartTime(), request.getEndTime());

        Reservation reservation = Reservation.builder()
                .organizer(user)
                .pitch(pitch)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalPrice(totalPrice)
                .status(ReservationStatus.PENDING)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        ReservationParticipant participant = ReservationParticipant.builder()
                .reservation(savedReservation)
                .user(user)
                .isOrganizer(true)
                .build();
        reservationParticipantRepository.save(participant);

        return mapToResponse(savedReservation);
    }

    @Override
    public ReservationResponse getReservationById(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
        authorizeReservationAccess(
                reservation,
                ReservationAccessCheckers.ADMIN,
                ReservationAccessCheckers.PARTICIPANT
        );
        return mapToResponse(reservation);
    }

    @Override
    public Page<ReservationResponse> getReservationsByUserId(UUID userId, Pageable pageable) {
        return reservationRepository.findReservationsByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<ReservationResponse> getReservationsByPitchId(UUID pitchId, Pageable pageable) {
        return reservationRepository.findByPitchId(pitchId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<UserResponse> getReservationUsers(UUID reservationId) {
        if (!reservationRepository.existsById(reservationId)) {
            throw new NotFoundException(MessageType.NO_RECORD_EXIST);
        }
        return reservationParticipantRepository.findByReservationId(reservationId).stream()
                .map(rp -> UserResponse.builder()
                        .id(rp.getUser().getId())
                        .username(rp.getUser().getUsername())
                        .email(rp.getUser().getEmail())
                        .role(rp.getUser().getRole())
                        .phoneNumber(rp.getUser().getPhoneNumber())
                        .isActive(rp.getUser().isActive())
                        .createdAt(rp.getUser().getCreatedAt())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public ReservationResponse updateReservation(UUID id, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));

        authorizeReservationAccess(reservation);

        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.COMPLETED
                || reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new ConflictException(MessageType.RESERVATION_NOT_CANCELLABLE);
        }

        LocalDateTime now = LocalDateTime.now();
        if (!request.getStartTime().isAfter(now)) {
            throw new BadRequestException(MessageType.RESERVATION_TIME_IN_PAST);
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException(MessageType.INVALID_RESERVATION_TIME_RANGE);
        }

        Pitch pitchToUse = reservation.getPitch();
        if (request.getPitchId() != null && !reservation.getPitch().getId().equals(request.getPitchId())) {
            pitchToUse = pitchRepository.findById(request.getPitchId())
                    .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
            if (!pitchToUse.isAvailable()) {
                throw new ConflictException(MessageType.PITCH_NOT_AVAILABLE);
            }
        }

        // Manual overlap check to ignore current reservation
        List<Reservation> overlaps = reservationRepository.findByPitchId(pitchToUse.getId(), Pageable.unpaged())
                .getContent();
        boolean hasOverlap = overlaps.stream()
                .filter(r -> ACTIVE_RESERVATION_STATUSES.contains(r.getStatus()))
                .filter(r -> !r.getId().equals(reservation.getId()))
                .anyMatch(r -> request.getStartTime().isBefore(r.getEndTime())
                        && request.getEndTime().isAfter(r.getStartTime()));

        if (hasOverlap) {
            throw new ConflictException(MessageType.RESERVATION_TIME_OVERLAP);
        }

        reservation.setPitch(pitchToUse);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setTotalPrice(calculateTotalPrice(pitchToUse, request.getStartTime(), request.getEndTime()));

        if (request.getOrganizerId() != null && !reservation.getOrganizer().getId().equals(request.getOrganizerId())) {
            User newOrganizer = userRepository.findById(request.getOrganizerId())
                    .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
            if (!reservationParticipantRepository.existsByReservationIdAndUserId(id, newOrganizer.getId())) {
                throw new BadRequestException(MessageType.VALIDATION_ERROR);
            }
            reservationParticipantRepository.findByReservationIdAndUserId(id, reservation.getOrganizer().getId())
                    .ifPresent(p -> p.setOrganizer(false));
            reservationParticipantRepository.findByReservationIdAndUserId(id, newOrganizer.getId())
                    .ifPresent(p -> p.setOrganizer(true));
            reservation.setOrganizer(newOrganizer);
        }

        Reservation updatedReservation = reservationRepository.save(reservation);
        return mapToResponse(updatedReservation);
    }

    @Override
    @Transactional
    public void cancelReservation(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));

        authorizeReservationAccess(reservation, ReservationAccessCheckers.ADMIN);

        if (reservation.getStatus() == ReservationStatus.COMPLETED
                || reservation.getStatus() == ReservationStatus.EXPIRED) {
            reservationRepository.delete(reservation);
            return;
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return;
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    private void validateReservationTimes(ReservationCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        if (!request.getStartTime().isAfter(now)) {
            throw new BadRequestException(MessageType.RESERVATION_TIME_IN_PAST);
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException(MessageType.INVALID_RESERVATION_TIME_RANGE);
        }
    }

    private BigDecimal calculateTotalPrice(Pitch pitch, LocalDateTime startTime, LocalDateTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        BigDecimal reservedHours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

        return pitch.getHourlyPrice()
                .multiply(reservedHours)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void authorizeReservationAccess(
            Reservation reservation, ReservationAccessChecker... allowedCheckers
    ) {
        User currentUser = getCurrentUser();

        // Organizer is always allowed (default rule)
        if (reservation.getOrganizer().getId().equals(currentUser.getId())) {
            return;
        }

        ReservationAuthorizationContext context = new ReservationAuthorizationContext(
                reservation, currentUser, reservationParticipantRepository);

        boolean allowed = java.util.Arrays.stream(allowedCheckers)
                .anyMatch(checker -> checker.hasAccess(context));

        if (!allowed) {
            throw new AccessDeniedException(MessageType.ACCESS_DENIED.getMessage());
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException(MessageType.ACCESS_DENIED.getMessage());
        }
        return user;
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getOrganizer().getId())
                .username(reservation.getOrganizer().getUsername())
                .pitchId(reservation.getPitch().getId())
                .pitchName(reservation.getPitch().getName())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .build();
    }
}
