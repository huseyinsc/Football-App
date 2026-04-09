package com.huseyinsacikay.service.impl;

import com.huseyinsacikay.dto.request.ReservationCreateRequest;
import com.huseyinsacikay.dto.response.ReservationResponse;
import com.huseyinsacikay.entity.Pitch;
import com.huseyinsacikay.entity.Reservation;
import com.huseyinsacikay.entity.ReservationStatus;
import com.huseyinsacikay.entity.User;
import com.huseyinsacikay.exception.BadRequestException;
import com.huseyinsacikay.exception.ConflictException;
import com.huseyinsacikay.exception.NotFoundException;
import com.huseyinsacikay.repository.PitchRepository;
import com.huseyinsacikay.repository.ReservationRepository;
import com.huseyinsacikay.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
                .organizer(mockUser)
                .pitch(mockPitch)
                .startTime(createRequest.getStartTime())
                .endTime(createRequest.getEndTime())
                .totalPrice(BigDecimal.valueOf(200.0))
                .status(ReservationStatus.PENDING)
                .build();

        authenticateAs(mockUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createReservation_ShouldReturnReservationResponse_WhenSuccessful() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(pitchRepository.findByIdForUpdate(pitchId)).thenReturn(Optional.of(mockPitch));
        when(reservationRepository.existsByPitchIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                eq(pitchId),
                anyCollection(),
                eq(createRequest.getEndTime()),
                eq(createRequest.getStartTime())
        )).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        ReservationResponse response = reservationService.createReservation(createRequest);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(200.0), response.getTotalPrice());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_ShouldThrowConflictException_WhenPitchNotAvailable() {
        mockPitch.setAvailable(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(pitchRepository.findByIdForUpdate(pitchId)).thenReturn(Optional.of(mockPitch));

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
    void createReservation_ShouldCalculateProRatedPrice_WhenDurationIsPartialHour() {
        createRequest.setEndTime(createRequest.getStartTime().plusMinutes(90));
        mockReservation.setEndTime(createRequest.getEndTime());
        mockReservation.setTotalPrice(BigDecimal.valueOf(150.00).setScale(2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(pitchRepository.findByIdForUpdate(pitchId)).thenReturn(Optional.of(mockPitch));
        when(reservationRepository.existsByPitchIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                eq(pitchId),
                anyCollection(),
                eq(createRequest.getEndTime()),
                eq(createRequest.getStartTime())
        )).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation savedReservation = invocation.getArgument(0);
            savedReservation.setId(reservationId);
            return savedReservation;
        });

        ReservationResponse response = reservationService.createReservation(createRequest);

        assertEquals(BigDecimal.valueOf(150.00).setScale(2), response.getTotalPrice());
    }

    @Test
    void createReservation_ShouldThrowBadRequestException_WhenStartTimeIsInPast() {
        createRequest.setStartTime(LocalDateTime.now().minusMinutes(30));
        createRequest.setEndTime(LocalDateTime.now().plusMinutes(30));

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(createRequest));
        verifyNoInteractions(userRepository, pitchRepository, reservationRepository);
    }

    @Test
    void createReservation_ShouldThrowBadRequestException_WhenEndTimeIsNotAfterStartTime() {
        createRequest.setEndTime(createRequest.getStartTime());

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(createRequest));
        verifyNoInteractions(userRepository, pitchRepository, reservationRepository);
    }

    @Test
    void createReservation_ShouldThrowConflictException_WhenReservationOverlaps() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(pitchRepository.findByIdForUpdate(pitchId)).thenReturn(Optional.of(mockPitch));
        when(reservationRepository.existsByPitchIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                eq(pitchId),
                anyCollection(),
                eq(createRequest.getEndTime()),
                eq(createRequest.getStartTime())
        )).thenReturn(true);

        assertThrows(ConflictException.class, () -> reservationService.createReservation(createRequest));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void cancelReservation_ShouldThrowConflictException_WhenReservationAlreadyCompleted() {
        mockReservation.setStatus(ReservationStatus.COMPLETED);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));

        assertThrows(ConflictException.class, () -> reservationService.cancelReservation(reservationId));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
    
    @Test
    void getReservationsByUserId_ShouldReturnPagedResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> pagedReservations = new PageImpl<>(List.of(mockReservation));
        when(reservationRepository.findByOrganizerId(userId, pageable)).thenReturn(pagedReservations);

        Page<ReservationResponse> responses = reservationService.getReservationsByUserId(userId, pageable);

        assertNotNull(responses);
        assertEquals(1, responses.getContent().size());
    }

    private void authenticateAs(User user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
