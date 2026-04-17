package com.huseyinsacikay.repository;

import com.huseyinsacikay.entity.ContactStrike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ContactStrikeRepository extends JpaRepository<ContactStrike, UUID> {
    Optional<ContactStrike> findByRequesterIdAndTargetId(UUID requesterId, UUID targetId);
}
