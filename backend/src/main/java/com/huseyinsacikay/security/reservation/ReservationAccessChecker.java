package com.huseyinsacikay.security.reservation;

@FunctionalInterface
public interface ReservationAccessChecker {
    boolean hasAccess(ReservationAuthorizationContext context);
}
