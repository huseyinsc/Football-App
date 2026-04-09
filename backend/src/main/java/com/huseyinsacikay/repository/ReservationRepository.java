package com.huseyinsacikay.repository;

import com.huseyinsacikay.entity.Reservation;
import com.huseyinsacikay.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Page<Reservation> findByOrganizerId(UUID userId, Pageable pageable);
    Page<Reservation> findByPitchId(UUID pitchId, Pageable pageable);
    boolean existsByPitchIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID pitchId,
            Collection<ReservationStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
    
    List<Reservation> findByStatusAndStartTimeBefore(ReservationStatus status, LocalDateTime startTime);
}
