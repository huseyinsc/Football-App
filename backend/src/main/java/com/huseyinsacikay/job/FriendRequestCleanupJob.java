package com.huseyinsacikay.job;

import com.huseyinsacikay.entity.ContactStrike;
import com.huseyinsacikay.entity.FriendRequest;
import com.huseyinsacikay.entity.User;
import com.huseyinsacikay.entity.UserBlock;
import com.huseyinsacikay.entity.UserPairId;
import com.huseyinsacikay.repository.ContactStrikeRepository;
import com.huseyinsacikay.repository.FriendRequestRepository;
import com.huseyinsacikay.repository.UserBlockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendRequestCleanupJob {

    private final FriendRequestRepository friendRequestRepository;
    private final ContactStrikeRepository contactStrikeRepository;
    private final UserBlockRepository userBlockRepository;

    @Scheduled(cron = "0 0 2 * * ?") // 2 AM every day
    @Transactional
    public void cleanupExpiredFriendRequests() {
        log.info("Starting scheduled job: cleanupExpiredFriendRequests");

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<FriendRequest> expiredRequests = friendRequestRepository.findByCreatedAtBefore(threeDaysAgo);

        if (expiredRequests.isEmpty()) {
            log.info("No expired friend requests found.");
            return;
        }

        for (FriendRequest request : expiredRequests) {
            UUID senderId = request.getSender().getId();
            UUID receiverId = request.getReceiver().getId();

            // Increment strike
            ContactStrike strike = contactStrikeRepository.findByRequesterIdAndTargetId(senderId, receiverId)
                    .orElseGet(() -> ContactStrike.builder()
                            .requester(request.getSender())
                            .target(request.getReceiver())
                            .strikeCount(0)
                            .build());

            int updatedStrikes = strike.getStrikeCount() + 1;
            strike.setStrikeCount(updatedStrikes);
            contactStrikeRepository.save(strike);

            // Check if blocked
            if (updatedStrikes >= 2) {
                if (!userBlockRepository.isBlocked(senderId, receiverId)) {
                    User first = senderId.compareTo(receiverId) < 0 ? request.getSender() : request.getReceiver();
                    User second = senderId.compareTo(receiverId) < 0 ? request.getReceiver() : request.getSender();
                    
                    UserBlock block = UserBlock.builder()
                            .user1(first)
                            .user2(second)
                            .build();
                    userBlockRepository.save(block);
                    log.info("Auto-blocked users {} and {} due to 2 ignored/rejected requests.", first.getId(), second.getId());
                }
            }

            // Finally, delete the expired request
            friendRequestRepository.delete(request);
        }

        log.info("Successfully cleaned up {} expired friend requests.", expiredRequests.size());
    }
}
