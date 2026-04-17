package com.huseyinsacikay.service;

import com.huseyinsacikay.dto.response.MatchRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface MatchRequestService {
    void requestToJoin(UUID reservationId);
    void inviteToMatch(UUID reservationId, UUID userId);
    void acceptRequest(UUID requestId);
    void rejectRequest(UUID requestId);
    
    Page<MatchRequestResponse> getPendingRequestsForReservation(UUID reservationId, Pageable pageable);
    Page<MatchRequestResponse> getPendingInvitesForUser(Pageable pageable);
}
