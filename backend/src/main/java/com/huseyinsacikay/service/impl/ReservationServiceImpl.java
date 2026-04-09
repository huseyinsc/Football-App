package com.huseyinsacikay.service.impl;

import com.huseyinsacikay.dto.request.ReservationCreateRequest;
import com.huseyinsacikay.dto.response.ReservationResponse;
import com.huseyinsacikay.entity.Pitch;
import com.huseyinsacikay.entity.Role;
import com.huseyinsacikay.entity.Reservation;
import com.huseyinsacikay.entity.ReservationStatus;
import com.huseyinsacikay.entity.User;
import com.huseyinsacikay.exception.BadRequestException;
import com.huseyinsacikay.exception.ConflictException;
import com.huseyinsacikay.exception.MessageType;
import com.huseyinsacikay.exception.NotFoundException;
import com.huseyinsacikay.repository.PitchRepository;
import com.huseyinsacikay.repository.ReservationRepository;
import com.huseyinsacikay.repository.UserRepository;
import com.huseyinsacikay.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
            ReservationStatus.CONFIRMED
    );

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final PitchRepository pitchRepository;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request) {
        validateReservationTimes(request);

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
                request.getStartTime()
        );
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

        return mapToResponse(savedReservation);
    }

    @Override
    public ReservationResponse getReservationById(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
        authorizeReservationAccess(reservation);
        return mapToResponse(reservation);
    }

    @Override
    public Page<ReservationResponse> getReservationsByUserId(UUID userId, Pageable pageable) {
        return reservationRepository.findByOrganizerId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<ReservationResponse> getReservationsByPitchId(UUID pitchId, Pageable pageable) {
        return reservationRepository.findByPitchId(pitchId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void cancelReservation(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));

        authorizeReservationAccess(reservation);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return;
        }
        if (reservation.getStatus() == ReservationStatus.COMPLETED || reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new ConflictException(MessageType.RESERVATION_NOT_CANCELLABLE);
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

    private void authorizeReservationAccess(Reservation reservation) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (!reservation.getOrganizer().getId().equals(currentUser.getId())) {
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
