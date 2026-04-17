package com.huseyinsacikay.repository;

import com.huseyinsacikay.entity.MatchRequest;
import com.huseyinsacikay.entity.MatchRequestStatus;
import com.huseyinsacikay.entity.MatchRequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, UUID> {
    Optional<MatchRequest> findByReservationIdAndUserIdAndTypeAndStatus(
            UUID reservationId, UUID userId, MatchRequestType type, MatchRequestStatus status);
            
    boolean existsByReservationIdAndUserIdAndStatus(UUID reservationId, UUID userId, MatchRequestStatus status);
    
    Page<MatchRequest> findByReservationIdAndTypeAndStatus(
            UUID reservationId, MatchRequestType type, MatchRequestStatus status, Pageable pageable);
            
    Page<MatchRequest> findByUserIdAndTypeAndStatus(
            UUID userId, MatchRequestType type, MatchRequestStatus status, Pageable pageable);
}
