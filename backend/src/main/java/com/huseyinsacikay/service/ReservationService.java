package com.huseyinsacikay.service;

import com.huseyinsacikay.dto.request.ReservationCreateRequest;
import com.huseyinsacikay.dto.response.ReservationResponse;

import java.util.List;
import java.util.UUID;

public interface ReservationService {
    ReservationResponse createReservation(ReservationCreateRequest request);
    ReservationResponse getReservationById(UUID id);
    List<ReservationResponse> getReservationsByUserId(UUID userId);
    List<ReservationResponse> getReservationsByPitchId(UUID pitchId);
    void cancelReservation(UUID id);
}
