package com.huseyinsacikay.service;

import com.huseyinsacikay.dto.request.PitchCreateRequest;
import com.huseyinsacikay.dto.response.PitchResponse;

import java.util.List;
import java.util.UUID;

public interface PitchService {
    PitchResponse createPitch(PitchCreateRequest request);
    PitchResponse getPitchById(UUID id);
    List<PitchResponse> getAllPitches();
    void deletePitch(UUID id);
}
