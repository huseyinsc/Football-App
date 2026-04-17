package com.huseyinsacikay.service;

import com.huseyinsacikay.dto.request.ReservationCreateRequest;
import com.huseyinsacikay.dto.response.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ReservationService {
    ReservationResponse createReservation(ReservationCreateRequest request);
    ReservationResponse getReservationById(UUID id);
    Page<ReservationResponse> getReservationsByUserId(UUID userId, Pageable pageable);
    Page<ReservationResponse> getReservationsByPitchId(UUID pitchId, Pageable pageable);
    ReservationResponse updateReservation(UUID id, com.huseyinsacikay.dto.request.ReservationUpdateRequest request);
    List<com.huseyinsacikay.dto.response.UserResponse> getReservationUsers(UUID reservationId);
    void cancelReservation(UUID id);
}
