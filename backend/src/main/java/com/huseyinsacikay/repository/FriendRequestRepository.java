package com.huseyinsacikay.repository;

import com.huseyinsacikay.entity.FriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    Optional<FriendRequest> findBySenderIdAndReceiverId(UUID senderId, UUID receiverId);
    boolean existsBySenderIdAndReceiverId(UUID senderId, UUID receiverId);
    
    Page<FriendRequest> findByReceiverId(UUID receiverId, Pageable pageable);
    Page<FriendRequest> findBySenderId(UUID senderId, Pageable pageable);
    
    List<FriendRequest> findByCreatedAtBefore(LocalDateTime dateTime);
}
