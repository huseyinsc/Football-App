package com.huseyinsacikay.job;

import com.huseyinsacikay.entity.Reservation;
import com.huseyinsacikay.entity.ReservationStatus;
import com.huseyinsacikay.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupJob {

    private final ReservationRepository reservationRepository;

    /**
     * This job runs every hour to check for pending reservations
     * whose start time has already passed, marking them as EXPIRED.
     */
    @Scheduled(cron = "0 0 * * * *") // Runs at the top of every hour
    @Transactional
    public void markExpiredReservations() {
        log.info("Starting scheduled job: markExpiredReservations");

        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndStartTimeBefore(ReservationStatus.PENDING, now);

        if (!expiredReservations.isEmpty()) {
            expiredReservations.forEach(reservation -> {
                reservation.setStatus(ReservationStatus.EXPIRED);
                // Here we would typically also send an email/notification to the user
                log.info("Reservation {} marked as EXPIRED for user {}", reservation.getId(),
                        reservation.getOrganizer().getUsername());
            });

            reservationRepository.saveAll(expiredReservations);
            log.info("Successfully updated {} expired reservations.", expiredReservations.size());
        } else {
            log.info("No expired reservations found.");
        }
    }

    /**
     * This job runs every day to hard delete reservations
     * that have been expired or completed for over 30 days.
     */
    @Scheduled(cron = "0 0 4 * * ?") // Runs at 4 AM every day
    @Transactional
    public void deleteOldReservations() {
        log.info("Starting scheduled job: deleteOldReservations");

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);

        List<Reservation> oldExpired = reservationRepository
                .findByStatusAndStartTimeBefore(ReservationStatus.EXPIRED, oneMonthAgo);
        
        List<Reservation> oldCompleted = reservationRepository
                .findByStatusAndStartTimeBefore(ReservationStatus.COMPLETED, oneMonthAgo);

        int totalDeleted = 0;
        
        if (!oldExpired.isEmpty()) {
            reservationRepository.deleteAll(oldExpired);
            totalDeleted += oldExpired.size();
        }
        
        if (!oldCompleted.isEmpty()) {
            reservationRepository.deleteAll(oldCompleted);
            totalDeleted += oldCompleted.size();
        }

        log.info("Successfully deleted {} old reservations.", totalDeleted);
    }
}
