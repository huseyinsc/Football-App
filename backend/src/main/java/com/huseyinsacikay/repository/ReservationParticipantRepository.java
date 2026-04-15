package com.huseyinsacikay.repository;

import com.huseyinsacikay.entity.ReservationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface ReservationParticipantRepository extends JpaRepository<ReservationParticipant, UUID> {
    
    Optional<ReservationParticipant> findByReservationIdAndUserId(UUID reservationId, UUID userId);
    
    boolean existsByReservationIdAndUserId(UUID reservationId, UUID userId);
    
    List<ReservationParticipant> findByReservationId(UUID reservationId);
    
    int countByReservationId(UUID reservationId);
}
