package com.huseyinsacikay.repository;

import com.huseyinsacikay.entity.UserContact;
import com.huseyinsacikay.entity.UserPairId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserContactRepository extends JpaRepository<UserContact, UserPairId> {
    @Query("SELECT u FROM UserContact u WHERE u.user1.id = :userId OR u.user2.id = :userId")
    List<UserContact> findAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(u) > 0 FROM UserContact u WHERE " +
           "(u.user1.id = :user1 AND u.user2.id = :user2) OR " +
           "(u.user1.id = :user2 AND u.user2.id = :user1)")
    boolean areUsersContacts(@Param("user1") UUID user1, @Param("user2") UUID user2);

    @Query("SELECT u FROM UserContact u WHERE " +
           "(u.user1.id = :user1 AND u.user2.id = :user2) OR " +
           "(u.user1.id = :user2 AND u.user2.id = :user1)")
    Optional<UserContact> findByUserId(@Param("user1") UUID user1, @Param("user2") UUID user2);
}
