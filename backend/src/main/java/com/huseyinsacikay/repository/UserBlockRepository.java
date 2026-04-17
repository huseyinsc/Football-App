package com.huseyinsacikay.repository;

import com.huseyinsacikay.entity.UserBlock;
import com.huseyinsacikay.entity.UserPairId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface UserBlockRepository extends JpaRepository<UserBlock, UserPairId> {
    @Query("SELECT COUNT(u) > 0 FROM UserBlock u WHERE " +
           "(u.user1.id = :user1 AND u.user2.id = :user2) OR " +
           "(u.user1.id = :user2 AND u.user2.id = :user1)")
    boolean isBlocked(@Param("user1") UUID user1, @Param("user2") UUID user2);
}
