package com.huseyinsacikay.service;

import com.huseyinsacikay.dto.request.ReservationCreateRequest;
import com.huseyinsacikay.dto.response.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReservationService {
    ReservationResponse createReservation(ReservationCreateRequest request);
    ReservationResponse getReservationById(UUID id);
    Page<ReservationResponse> getReservationsByUserId(UUID userId, Pageable pageable);
    Page<ReservationResponse> getReservationsByPitchId(UUID pitchId, Pageable pageable);
    void cancelReservation(UUID id);
}
