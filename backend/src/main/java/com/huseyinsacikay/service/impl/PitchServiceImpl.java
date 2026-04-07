package com.huseyinsacikay.service.impl;

import com.huseyinsacikay.dto.request.PitchCreateRequest;
import com.huseyinsacikay.dto.response.PitchResponse;
import com.huseyinsacikay.entity.Pitch;
import com.huseyinsacikay.exception.MessageType;
import com.huseyinsacikay.exception.NotFoundException;
import com.huseyinsacikay.repository.PitchRepository;
import com.huseyinsacikay.service.PitchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PitchServiceImpl implements PitchService {

    private final PitchRepository pitchRepository;

    @Override
    public PitchResponse createPitch(PitchCreateRequest request) {
        Pitch pitch = Pitch.builder()
                .name(request.getName())
                .location(request.getLocation())
                .hourlyPrice(request.getHourlyPrice())
                .capacity(request.getCapacity())
                .isAvailable(true)
                .build();

        Pitch savedPitch = pitchRepository.save(pitch);

        return mapToResponse(savedPitch);
    }

    @Override
    public PitchResponse getPitchById(UUID id) {
        Pitch pitch = pitchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
        return mapToResponse(pitch);
    }

    @Override
    public List<PitchResponse> getAllPitches() {
        return pitchRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePitch(UUID id) {
        Pitch pitch = pitchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
        pitchRepository.delete(pitch);
    }

    private PitchResponse mapToResponse(Pitch pitch) {
        return PitchResponse.builder()
                .id(pitch.getId())
                .name(pitch.getName())
                .location(pitch.getLocation())
                .hourlyPrice(pitch.getHourlyPrice())
                .capacity(pitch.getCapacity())
                .isAvailable(pitch.isAvailable())
                .build();
    }
}
