package com.huseyinsacikay.job;

import com.huseyinsacikay.entity.Reservation;
import com.huseyinsacikay.entity.ReservationStatus;
import com.huseyinsacikay.entity.User;
import com.huseyinsacikay.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationCleanupJobTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationCleanupJob reservationCleanupJob;

    private Reservation mockReservation;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(UUID.randomUUID()).username("testuser").build();
        
        mockReservation = Reservation.builder()
                .id(UUID.randomUUID())
                .organizer(user)
                .status(ReservationStatus.PENDING)
                .startTime(LocalDateTime.now().minusHours(1))
                .build();
    }

    @Test
    void markExpiredReservations_ShouldUpdateStatusToExpired_WhenFound() {
        when(reservationRepository.findByStatusAndStartTimeBefore(eq(ReservationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(mockReservation));

        reservationCleanupJob.markExpiredReservations();

        assertEquals(ReservationStatus.EXPIRED, mockReservation.getStatus());
        verify(reservationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void markExpiredReservations_ShouldDoNothing_WhenNoneFound() {
        when(reservationRepository.findByStatusAndStartTimeBefore(eq(ReservationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        reservationCleanupJob.markExpiredReservations();

        verify(reservationRepository, never()).saveAll(anyList());
    }
}
