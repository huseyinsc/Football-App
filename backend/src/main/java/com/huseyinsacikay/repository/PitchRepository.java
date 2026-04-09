package com.huseyinsacikay.repository;

import com.huseyinsacikay.entity.Pitch;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PitchRepository extends JpaRepository<Pitch, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pitch p where p.id = :id")
    Optional<Pitch> findByIdForUpdate(UUID id);
}
