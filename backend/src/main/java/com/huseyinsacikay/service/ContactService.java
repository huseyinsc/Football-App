package com.huseyinsacikay.service;

import com.huseyinsacikay.dto.response.FriendRequestResponse;
import com.huseyinsacikay.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface ContactService {
    void sendFriendRequest(UUID targetUserId);
    void acceptFriendRequest(UUID requestId);
    void rejectFriendRequest(UUID requestId);
    void removeContact(UUID targetUserId);
    void blockUser(UUID targetUserId);
    
    Page<FriendRequestResponse> getPendingIncomingRequests(Pageable pageable);
    Page<FriendRequestResponse> getPendingOutgoingRequests(Pageable pageable);
    Page<UserResponse> getContacts(Pageable pageable);
}
