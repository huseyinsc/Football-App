package com.huseyinsacikay.service.impl;

import com.huseyinsacikay.dto.request.ReservationCreateRequest;
import com.huseyinsacikay.dto.response.ReservationResponse;
import com.huseyinsacikay.entity.Pitch;
import com.huseyinsacikay.entity.Reservation;
import com.huseyinsacikay.entity.ReservationStatus;
import com.huseyinsacikay.entity.User;
import com.huseyinsacikay.exception.ConflictException;
import com.huseyinsacikay.exception.NotFoundException;
import com.huseyinsacikay.repository.PitchRepository;
import com.huseyinsacikay.repository.ReservationRepository;
import com.huseyinsacikay.repository.UserRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PitchRepository pitchRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private ReservationCreateRequest createRequest;
    private User mockUser;
    private Pitch mockPitch;
    private Reservation mockReservation;
    private UUID userId;
    private UUID pitchId;
    private UUID reservationId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        pitchId = UUID.randomUUID();
        reservationId = UUID.randomUUID();

        createRequest = new ReservationCreateRequest();
        createRequest.setUserId(userId);
        createRequest.setPitchId(pitchId);
        createRequest.setStartTime(LocalDateTime.now().plusDays(1));
        createRequest.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        mockUser = User.builder()
                .id(userId)
                .username("testuser")
                .build();

        mockPitch = Pitch.builder()
                .id(pitchId)
                .name("Camp Nou")
                .hourlyPrice(BigDecimal.valueOf(100.0))
                .isAvailable(true)
                .build();

        mockReservation = Reservation.builder()
                .id(reservationId)
                .user(mockUser)
                .pitch(mockPitch)
                .startTime(createRequest.getStartTime())
                .endTime(createRequest.getEndTime())
                .totalPrice(200.0)
                .status(ReservationStatus.PENDING)
                .build();
    }

    @Test
    void createReservation_ShouldReturnReservationResponse_WhenSuccessful() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(pitchRepository.findById(pitchId)).thenReturn(Optional.of(mockPitch));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        ReservationResponse response = reservationService.createReservation(createRequest);

        assertNotNull(response);
        assertEquals(200.0, response.getTotalPrice());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_ShouldThrowConflictException_WhenPitchNotAvailable() {
        mockPitch.setAvailable(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(pitchRepository.findById(pitchId)).thenReturn(Optional.of(mockPitch));

        assertThrows(ConflictException.class, () -> reservationService.createReservation(createRequest));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void cancelReservation_ShouldSetStatusToCancelled() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        reservationService.cancelReservation(reservationId);

        assertEquals(ReservationStatus.CANCELLED, mockReservation.getStatus());
        verify(reservationRepository).save(mockReservation);
    }
    
    @Test
    void getReservationsByUserId_ShouldReturnPagedResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> pagedReservations = new PageImpl<>(List.of(mockReservation));
        when(reservationRepository.findByUserId(userId, pageable)).thenReturn(pagedReservations);

        Page<ReservationResponse> responses = reservationService.getReservationsByUserId(userId, pageable);

        assertNotNull(responses);
        assertEquals(1, responses.getContent().size());
    }
}