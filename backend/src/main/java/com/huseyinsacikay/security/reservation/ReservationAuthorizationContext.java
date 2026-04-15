package com.huseyinsacikay.security.reservation;

import com.huseyinsacikay.entity.Reservation;
import com.huseyinsacikay.entity.User;
import com.huseyinsacikay.repository.ReservationParticipantRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReservationAuthorizationContext {
    private final Reservation reservation;
    private final User currentUser;
    private final ReservationParticipantRepository participantRepository;
}
