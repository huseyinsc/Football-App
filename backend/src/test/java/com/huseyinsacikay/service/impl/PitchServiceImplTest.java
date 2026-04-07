package com.huseyinsacikay.service.impl;

import com.huseyinsacikay.dto.request.PitchCreateRequest;
import com.huseyinsacikay.dto.response.PitchResponse;
import com.huseyinsacikay.entity.Pitch;
import com.huseyinsacikay.exception.NotFoundException;
import com.huseyinsacikay.repository.PitchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PitchServiceImplTest {

    @Mock
    private PitchRepository pitchRepository;

    @InjectMocks
    private PitchServiceImpl pitchService;

    private PitchCreateRequest createRequest;
    private Pitch mockPitch;
    private UUID pitchId;

    @BeforeEach
    void setUp() {
        pitchId = UUID.randomUUID();
        
        createRequest = new PitchCreateRequest();
        createRequest.setName("Camp Nou");
        createRequest.setLocation("Barcelona");
        createRequest.setHourlyPrice(BigDecimal.valueOf(100.0));
        createRequest.setCapacity(11);

        mockPitch = Pitch.builder()
                .id(pitchId)
                .name("Camp Nou")
                .location("Barcelona")
                .hourlyPrice(BigDecimal.valueOf(100.0))
                .capacity(11)
                .isAvailable(true)
                .build();
    }

    @Test
    void createPitch_ShouldReturnPitchResponse_WhenSuccessful() {
        when(pitchRepository.save(any(Pitch.class))).thenReturn(mockPitch);

        PitchResponse response = pitchService.createPitch(createRequest);

        assertNotNull(response);
        assertEquals(createRequest.getName(), response.getName());
        verify(pitchRepository).save(any(Pitch.class));
    }

    @Test
    void getPitchById_ShouldReturnPitchResponse_WhenExists() {
        when(pitchRepository.findById(pitchId)).thenReturn(Optional.of(mockPitch));

        PitchResponse response = pitchService.getPitchById(pitchId);

        assertNotNull(response);
        assertEquals(pitchId, response.getId());
    }

    @Test
    void getPitchById_ShouldThrowNotFoundException_WhenNotExists() {
        when(pitchRepository.findById(pitchId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pitchService.getPitchById(pitchId));
    }

    @Test
    void getAllPitches_ShouldReturnPagedPitchResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pitch> pagedPitches = new PageImpl<>(List.of(mockPitch));
        when(pitchRepository.findAll(pageable)).thenReturn(pagedPitches);

        Page<PitchResponse> responses = pitchService.getAllPitches(pageable);

        assertNotNull(responses);
        assertEquals(1, responses.getContent().size());
    }

    @Test
    void deletePitch_ShouldDeletePitch_WhenExists() {
        when(pitchRepository.findById(pitchId)).thenReturn(Optional.of(mockPitch));
        doNothing().when(pitchRepository).delete(mockPitch);

        assertDoesNotThrow(() -> pitchService.deletePitch(pitchId));
        verify(pitchRepository).delete(mockPitch);
    }
}