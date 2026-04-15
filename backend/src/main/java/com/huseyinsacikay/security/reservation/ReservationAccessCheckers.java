package com.huseyinsacikay.security.reservation;

import com.huseyinsacikay.entity.Role;

public final class ReservationAccessCheckers {

    public static final ReservationAccessChecker ADMIN = context -> 
            context.getCurrentUser().getRole() == Role.ADMIN;

    public static final ReservationAccessChecker PARTICIPANT = context -> 
            context.getParticipantRepository().existsByReservationIdAndUserId(
                    context.getReservation().getId(), 
                    context.getCurrentUser().getId()
            );

    private ReservationAccessCheckers() {
        // Prevent instantiation
    }
}
