package com.huseyinsacikay.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_blocks")
@IdClass(UserPairId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBlock {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @CreationTimestamp
    @Column(name = "blocked_at", nullable = false, updatable = false)
    private LocalDateTime blockedAt;
}
