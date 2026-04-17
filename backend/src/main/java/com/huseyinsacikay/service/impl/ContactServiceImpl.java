package com.huseyinsacikay.service.impl;

import com.huseyinsacikay.dto.response.FriendRequestResponse;
import com.huseyinsacikay.dto.response.UserResponse;
import com.huseyinsacikay.entity.*;
import org.springframework.security.access.AccessDeniedException;
import com.huseyinsacikay.exception.BadRequestException;
import com.huseyinsacikay.exception.ConflictException;
import com.huseyinsacikay.exception.MessageType;
import com.huseyinsacikay.exception.NotFoundException;
import com.huseyinsacikay.repository.*;
import com.huseyinsacikay.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final FriendRequestRepository friendRequestRepository;
    private final ContactStrikeRepository contactStrikeRepository;
    private final UserContactRepository userContactRepository;
    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
    }

    private void ensureNotBlocked(UUID u1, UUID u2) {
        if (userBlockRepository.isBlocked(u1, u2)) {
            throw new AccessDeniedException("Interaction blocked.");
        }
    }

    @Override
    @Transactional
    public void sendFriendRequest(UUID targetUserId) {
        User current = getCurrentUser();
        if (current.getId().equals(targetUserId)) {
            throw new BadRequestException(MessageType.VALIDATION_ERROR);
        }

        User target = getUserOrThrow(targetUserId);
        ensureNotBlocked(current.getId(), targetUserId);

        // Check if already friends
        if (userContactRepository.areUsersContacts(current.getId(), targetUserId)) {
            throw new ConflictException(MessageType.VALIDATION_ERROR);
        }

        // Already sent
        if (friendRequestRepository.existsBySenderIdAndReceiverId(current.getId(), targetUserId)) {
            throw new ConflictException(MessageType.VALIDATION_ERROR);
        }

        // Reverse request exists -> auto accept
        friendRequestRepository.findBySenderIdAndReceiverId(targetUserId, current.getId()).ifPresentOrElse(
                request -> acceptFriendRequest(request.getId()),
                () -> friendRequestRepository.save(FriendRequest.builder().sender(current).receiver(target).build())
        );
    }

    @Override
    @Transactional
    public void acceptFriendRequest(UUID requestId) {
        User current = getCurrentUser();
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));

        if (!request.getReceiver().getId().equals(current.getId())) {
            throw new AccessDeniedException(MessageType.ACCESS_DENIED.getMessage());
        }

        User u1 = request.getSender().getId().compareTo(current.getId()) < 0 ? request.getSender() : current;
        User u2 = request.getSender().getId().compareTo(current.getId()) < 0 ? current : request.getSender();

        userContactRepository.save(UserContact.builder().user1(u1).user2(u2).build());
        friendRequestRepository.delete(request);
    }

    @Override
    @Transactional
    public void rejectFriendRequest(UUID requestId) {
        User current = getCurrentUser();
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));

        if (!request.getReceiver().getId().equals(current.getId())) {
            throw new AccessDeniedException(MessageType.ACCESS_DENIED.getMessage());
        }

        UUID senderId = request.getSender().getId();
        UUID receiverId = current.getId();

        ContactStrike strike = contactStrikeRepository.findByRequesterIdAndTargetId(senderId, receiverId)
                .orElseGet(() -> ContactStrike.builder()
                        .requester(request.getSender())
                        .target(current) // They rejected ME? Wait, sender = them, receiver = me. I am rejecting the sender's request to me! Yes.
                        .strikeCount(0)
                        .build());

        int count = strike.getStrikeCount() + 1;
        strike.setStrikeCount(count);
        contactStrikeRepository.save(strike);

        friendRequestRepository.delete(request);

        if (count >= 2) {
            blockUserLogic(request.getSender(), current);
        }
    }

    @Override
    @Transactional
    public void removeContact(UUID targetUserId) {
        User current = getCurrentUser();
        userContactRepository.findByUserId(current.getId(), targetUserId)
                .ifPresent(userContactRepository::delete);
    }

    @Override
    @Transactional
    public void blockUser(UUID targetUserId) {
        User current = getCurrentUser();
        User target = getUserOrThrow(targetUserId);
        if (current.getId().equals(targetUserId)) {
            throw new BadRequestException(MessageType.VALIDATION_ERROR);
        }
        blockUserLogic(current, target);
    }

    private void blockUserLogic(User u1, User u2) {
        User first = u1.getId().compareTo(u2.getId()) < 0 ? u1 : u2;
        User second = u1.getId().compareTo(u2.getId()) < 0 ? u2 : u1;

        if (!userBlockRepository.isBlocked(u1.getId(), u2.getId())) {
            userBlockRepository.save(UserBlock.builder().user1(first).user2(second).build());
        }

        userContactRepository.findByUserId(u1.getId(), u2.getId()).ifPresent(userContactRepository::delete);
        friendRequestRepository.findBySenderIdAndReceiverId(u1.getId(), u2.getId()).ifPresent(friendRequestRepository::delete);
        friendRequestRepository.findBySenderIdAndReceiverId(u2.getId(), u1.getId()).ifPresent(friendRequestRepository::delete);
    }

    @Override
    public Page<FriendRequestResponse> getPendingIncomingRequests(Pageable pageable) {
        User current = getCurrentUser();
        Page<FriendRequest> requests = friendRequestRepository.findByReceiverId(current.getId(), pageable);
        List<FriendRequestResponse> responses = requests.stream().map(this::mapToResponse).collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, requests.getTotalElements());
    }

    @Override
    public Page<FriendRequestResponse> getPendingOutgoingRequests(Pageable pageable) {
        User current = getCurrentUser();
        Page<FriendRequest> requests = friendRequestRepository.findBySenderId(current.getId(), pageable);
        List<FriendRequestResponse> responses = requests.stream().map(this::mapToResponse).collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, requests.getTotalElements());
    }

    @Override
    public Page<UserResponse> getContacts(Pageable pageable) {
        User current = getCurrentUser();
        List<UserContact> contacts = userContactRepository.findAllByUserId(current.getId());
        
        List<UserResponse> mapped = contacts.stream()
                .map(c -> c.getUser1().getId().equals(current.getId()) ? c.getUser2() : c.getUser1())
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        // Simple manual paging logic since the repo query didn't use Spring Page for custom OR logic easily
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), mapped.size());
        List<UserResponse> subList = (start > mapped.size()) ? List.of() : mapped.subList(start, end);

        return new PageImpl<>(subList, pageable, mapped.size());
    }

    private FriendRequestResponse mapToResponse(FriendRequest req) {
        return FriendRequestResponse.builder()
                .id(req.getId())
                .sender(mapToUserResponse(req.getSender()))
                .receiver(mapToUserResponse(req.getReceiver()))
                .createdAt(req.getCreatedAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
