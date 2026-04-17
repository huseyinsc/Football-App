package com.huseyinsacikay.service.impl;

import com.huseyinsacikay.dto.response.MatchRequestResponse;
import com.huseyinsacikay.dto.response.UserResponse;
import com.huseyinsacikay.entity.*;
import org.springframework.security.access.AccessDeniedException;
import com.huseyinsacikay.exception.BadRequestException;
import com.huseyinsacikay.exception.ConflictException;
import com.huseyinsacikay.exception.MessageType;
import com.huseyinsacikay.exception.NotFoundException;
import com.huseyinsacikay.repository.*;
import com.huseyinsacikay.service.MatchRequestService;
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
public class MatchRequestServiceImpl implements MatchRequestService {

    private final MatchRequestRepository matchRequestRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationParticipantRepository reservationParticipantRepository;
    private final UserContactRepository userContactRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Reservation getReservationOrThrow(UUID resId) {
        return reservationRepository.findById(resId).orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
    }

    private void ensureNotInMatch(UUID resId, UUID userId) {
        if (reservationParticipantRepository.existsByReservationIdAndUserId(resId, userId)) {
            throw new ConflictException(MessageType.VALIDATION_ERROR);
        }
    }

    @Override
    @Transactional
    public void requestToJoin(UUID reservationId) {
        User currentUser = getCurrentUser();
        Reservation reservation = getReservationOrThrow(reservationId);
        
        ensureNotInMatch(reservationId, currentUser.getId());

        if (reservation.getJoinPolicy() == JoinPolicy.INVITE_ONLY) {
            throw new AccessDeniedException("This match is invite only.");
        }

        if (reservation.getJoinPolicy() == JoinPolicy.FRIENDS_ONLY) {
            if (!userContactRepository.areUsersContacts(reservation.getOrganizer().getId(), currentUser.getId())) {
                throw new AccessDeniedException("Only friends of the organizer can request to join.");
            }
        }

        if (matchRequestRepository.existsByReservationIdAndUserIdAndStatus(reservationId, currentUser.getId(), MatchRequestStatus.PENDING)) {
            throw new ConflictException(MessageType.VALIDATION_ERROR);
        }

        MatchRequest req = MatchRequest.builder()
                .reservation(reservation)
                .user(currentUser)
                .type(MatchRequestType.JOIN_REQUEST)
                .status(MatchRequestStatus.PENDING)
                .build();
        matchRequestRepository.save(req);
    }

    @Override
    @Transactional
    public void inviteToMatch(UUID reservationId, UUID userId) {
        User currentUser = getCurrentUser();
        Reservation reservation = getReservationOrThrow(reservationId);

        if (!reservation.getOrganizer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the organizer can invite users.");
        }

        User targetUser = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
        ensureNotInMatch(reservationId, targetUser.getId());

        if (matchRequestRepository.existsByReservationIdAndUserIdAndStatus(reservationId, targetUser.getId(), MatchRequestStatus.PENDING)) {
            throw new ConflictException(MessageType.VALIDATION_ERROR);
        }

        MatchRequest req = MatchRequest.builder()
                .reservation(reservation)
                .user(targetUser)
                .type(MatchRequestType.MATCH_INVITE)
                .status(MatchRequestStatus.PENDING)
                .build();
        matchRequestRepository.save(req);
    }

    @Override
    @Transactional
    public void acceptRequest(UUID requestId) {
        User currentUser = getCurrentUser();
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));

        if (request.getStatus() != MatchRequestStatus.PENDING) {
            throw new BadRequestException(MessageType.VALIDATION_ERROR);
        }

        if (request.getType() == MatchRequestType.JOIN_REQUEST) {
            // Only organizer can accept join requests
            if (!request.getReservation().getOrganizer().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException(MessageType.ACCESS_DENIED.getMessage());
            }
        } else if (request.getType() == MatchRequestType.MATCH_INVITE) {
            // Only the invited user can accept match invites
            if (!request.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException(MessageType.ACCESS_DENIED.getMessage());
            }
        }

        // Add them strictly to the match
        ReservationParticipant participant = ReservationParticipant.builder()
                .reservation(request.getReservation())
                .user(request.getUser())
                .isOrganizer(false)
                .build();
        reservationParticipantRepository.save(participant);

        matchRequestRepository.delete(request);
    }

    @Override
    @Transactional
    public void rejectRequest(UUID requestId) {
        User currentUser = getCurrentUser();
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));

        if (request.getType() == MatchRequestType.JOIN_REQUEST) {
            if (!request.getReservation().getOrganizer().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException(MessageType.ACCESS_DENIED.getMessage());
            }
        } else if (request.getType() == MatchRequestType.MATCH_INVITE) {
            if (!request.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException(MessageType.ACCESS_DENIED.getMessage());
            }
        }

        request.setStatus(MatchRequestStatus.REJECTED);
        matchRequestRepository.save(request);
    }

    @Override
    public Page<MatchRequestResponse> getPendingRequestsForReservation(UUID reservationId, Pageable pageable) {
        User currentUser = getCurrentUser();
        Reservation reservation = getReservationOrThrow(reservationId);

        if (!reservation.getOrganizer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(MessageType.ACCESS_DENIED.getMessage());
        }

        Page<MatchRequest> requests = matchRequestRepository.findByReservationIdAndTypeAndStatus(
                reservationId, MatchRequestType.JOIN_REQUEST, MatchRequestStatus.PENDING, pageable);
                
        return mapToPage(requests, pageable);
    }

    @Override
    public Page<MatchRequestResponse> getPendingInvitesForUser(Pageable pageable) {
        User currentUser = getCurrentUser();
        
        Page<MatchRequest> requests = matchRequestRepository.findByUserIdAndTypeAndStatus(
                currentUser.getId(), MatchRequestType.MATCH_INVITE, MatchRequestStatus.PENDING, pageable);
                
        return mapToPage(requests, pageable);
    }

    private Page<MatchRequestResponse> mapToPage(Page<MatchRequest> requests, Pageable pageable) {
        List<MatchRequestResponse> responses = requests.stream().map(this::mapToResponse).collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, requests.getTotalElements());
    }

    private MatchRequestResponse mapToResponse(MatchRequest req) {
        UserResponse userResponse = UserResponse.builder()
                .id(req.getUser().getId())
                .username(req.getUser().getUsername())
                .email(req.getUser().getEmail())
                .phoneNumber(req.getUser().getPhoneNumber())
                .role(req.getUser().getRole())
                .isActive(req.getUser().isActive())
                .createdAt(req.getUser().getCreatedAt())
                .build();

        return MatchRequestResponse.builder()
                .id(req.getId())
                .reservationId(req.getReservation().getId())
                .user(userResponse)
                .type(req.getType())
                .status(req.getStatus())
                .createdAt(req.getCreatedAt())
                .build();
    }
}
