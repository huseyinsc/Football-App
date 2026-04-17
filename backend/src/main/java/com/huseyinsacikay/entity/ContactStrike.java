package com.huseyinsacikay.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contact_strikes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactStrike {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    @Column(name = "strike_count", nullable = false)
    @Builder.Default
    private int strikeCount = 1;

    @CreationTimestamp
    @Column(name = "last_strike_at", nullable = false)
    private LocalDateTime lastStrikeAt;

    @PreUpdate
    public void setLastStrikeAt() {
        this.lastStrikeAt = LocalDateTime.now();
    }
}
