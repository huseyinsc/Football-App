package com.huseyinsacikay.service;

import com.huseyinsacikay.dto.request.PitchCreateRequest;
import com.huseyinsacikay.dto.response.PitchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PitchService {
    PitchResponse createPitch(PitchCreateRequest request);
    PitchResponse getPitchById(UUID id);
    PitchResponse updatePitch(UUID id, PitchCreateRequest request);
    Page<PitchResponse> getAllPitches(Pageable pageable);
    void deletePitch(UUID id);
}
