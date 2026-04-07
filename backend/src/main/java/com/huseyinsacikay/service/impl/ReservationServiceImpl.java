package com.huseyinsacikay.service.impl;

import com.huseyinsacikay.dto.request.ReservationCreateRequest;
import com.huseyinsacikay.dto.response.ReservationResponse;
import com.huseyinsacikay.entity.Pitch;
import com.huseyinsacikay.entity.Reservation;
import com.huseyinsacikay.entity.ReservationStatus;
import com.huseyinsacikay.entity.User;
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
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final PitchRepository pitchRepository;

    @Override
    public ReservationResponse createReservation(ReservationCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));

        Pitch pitch = pitchRepository.findById(request.getPitchId())
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));

        // Check if pitch is available for this time.
        // For simplicity in Phase 6, we just do a basic check or skip complex overlapping logic.
        // Actually we can check existing reservations for overlap if we want, but let's keep it simple.
        if (!pitch.isAvailable()) {
            throw new ConflictException(MessageType.PITCH_NOT_AVAILABLE);
        }

        long hours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();
        double totalPrice = hours * pitch.getHourlyPrice().doubleValue();

        Reservation reservation = Reservation.builder()
                .user(user)
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
        return mapToResponse(reservation);
    }

    @Override
    public Page<ReservationResponse> getReservationsByUserId(UUID userId, Pageable pageable) {
        return reservationRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<ReservationResponse> getReservationsByPitchId(UUID pitchId, Pageable pageable) {
        return reservationRepository.findByPitchId(pitchId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public void cancelReservation(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
        
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .username(reservation.getUser().getUsername())
                .pitchId(reservation.getPitch().getId())
                .pitchName(reservation.getPitch().getName())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .build();
    }
}
