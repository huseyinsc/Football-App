package com.huseyinsacikay.repository;

import com.huseyinsacikay.entity.Pitch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PitchRepository extends JpaRepository<Pitch, UUID> {
}