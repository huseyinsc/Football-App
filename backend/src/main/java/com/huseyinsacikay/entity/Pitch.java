package com.huseyinsacikay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pitches")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Pitch {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String location;

    @Column(name = "hourly_price")
    private BigDecimal hourlyPrice;

    private Integer capacity; // 6v6, 7v7 vb.

    @Column(name = "is_available")
    @Builder.Default
    private boolean isAvailable = true;
}