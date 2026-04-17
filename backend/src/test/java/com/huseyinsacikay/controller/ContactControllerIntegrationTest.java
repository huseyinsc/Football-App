package com.huseyinsacikay.controller;

import com.huseyinsacikay.entity.*;
import com.huseyinsacikay.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Contacts API.
 * Tests: send/accept/reject friend request, remove contact, block user,
 * list contacts, list incoming/outgoing requests.
 * Uses in-memory H2 database.
 */
@SpringBootTest
class ContactControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository userRepository;
    @Autowired private FriendRequestRepository friendRequestRepository;
    @Autowired private UserContactRepository userContactRepository;
    @Autowired private UserBlockRepository userBlockRepository;
    @Autowired private ContactStrikeRepository contactStrikeRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String user1Token;
    private String user2Token;
    private String user3Token;
    private UUID user1Id;
    private UUID user2Id;
    private UUID user3Id;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()).build();

        // Clean up in correct FK order
        userContactRepository.deleteAll();
        userBlockRepository.deleteAll();
        contactStrikeRepository.deleteAll();
        friendRequestRepository.deleteAll();
        userRepository.deleteAll();

        User u1 = userRepository.save(User.builder()
                .username("user1").email("u1@test.com")
                .password(passwordEncoder.encode("Pass1_ok")).phoneNumber("111")
                .role(Role.USER).isActive(true).build());
        user1Id = u1.getId();

        User u2 = userRepository.save(User.builder()
                .username("user2").email("u2@test.com")
                .password(passwordEncoder.encode("Pass2_ok")).phoneNumber("222")
                .role(Role.USER).isActive(true).build());
        user2Id = u2.getId();

        User u3 = userRepository.save(User.builder()
                .username("user3").email("u3@test.com")
                .password(passwordEncoder.encode("Pass3_ok")).phoneNumber("333")
                .role(Role.USER).isActive(true).build());
        user3Id = u3.getId();

        user1Token = login("user1", "Pass1_ok");
        user2Token = login("user2", "Pass2_ok");
        user3Token = login("user3", "Pass3_ok");
    }

    // ─── Send Friend Request ──────────────────────────────────────────────────

    @Test
    void sendFriendRequest_ToNewUser_Returns200() throws Exception {
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());
    }

    @Test
    void sendFriendRequest_ToSelf_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/contacts/request/" + user1Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendFriendRequest_Duplicate_Returns409() throws Exception {
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        // Second request to same person
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isConflict());
    }

    @Test
    void sendFriendRequest_WhenBlockedByTarget_Returns403() throws Exception {
        // Block user1 from user2's side: block is sorted so we save it correctly
        UUID u1 = user1Id.compareTo(user2Id) < 0 ? user1Id : user2Id;
        UUID u2 = user1Id.compareTo(user2Id) < 0 ? user2Id : user1Id;
        User first = userRepository.findById(u1).orElseThrow();
        User second = userRepository.findById(u2).orElseThrow();
        userBlockRepository.save(UserBlock.builder().user1(first).user2(second).build());

        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void sendFriendRequest_WithReverseExisting_AutoAccepts_Returns200() throws Exception {
        // user2 sends to user1
        mockMvc.perform(post("/api/v1/contacts/request/" + user1Id)
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk());

        // user1 sends back -> should auto-accept
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        // Now they should be contacts and pending request should be gone
        mockMvc.perform(get("/api/v1/contacts")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ─── Accept Friend Request ────────────────────────────────────────────────

    @Test
    void acceptFriendRequest_AsReceiver_Returns200AndCreatesContact() throws Exception {
        // user1 sends request
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        UUID requestId = friendRequestRepository.findBySenderIdAndReceiverId(user1Id, user2Id)
                .orElseThrow().getId();

        // user2 accepts
        mockMvc.perform(post("/api/v1/contacts/accept/" + requestId)
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk());

        // Verify they are now contacts
        mockMvc.perform(get("/api/v1/contacts")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void acceptFriendRequest_AsSender_Returns403() throws Exception {
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        UUID requestId = friendRequestRepository.findBySenderIdAndReceiverId(user1Id, user2Id)
                .orElseThrow().getId();

        // user1 tries to accept their own request
        mockMvc.perform(post("/api/v1/contacts/accept/" + requestId)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void acceptFriendRequest_NonExistent_Returns404() throws Exception {
        mockMvc.perform(post("/api/v1/contacts/accept/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isNotFound());
    }

    // ─── Reject Friend Request ────────────────────────────────────────────────

    @Test
    void rejectFriendRequest_AsReceiver_Returns200AndIncrementsStrike() throws Exception {
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        UUID requestId = friendRequestRepository.findBySenderIdAndReceiverId(user1Id, user2Id)
                .orElseThrow().getId();

        // user2 rejects
        mockMvc.perform(post("/api/v1/contacts/reject/" + requestId)
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk());

        // Strike count should be 1
        var strike = contactStrikeRepository.findByRequesterIdAndTargetId(user1Id, user2Id);
        assert strike.isPresent();
        assert strike.get().getStrikeCount() == 1;
    }

    @Test
    void rejectFriendRequest_Twice_AutoBlocks() throws Exception {
        // First rejection
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        UUID requestId = friendRequestRepository.findBySenderIdAndReceiverId(user1Id, user2Id)
                .orElseThrow().getId();

        mockMvc.perform(post("/api/v1/contacts/reject/" + requestId)
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk());

        // Second request & rejection
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        UUID requestId2 = friendRequestRepository.findBySenderIdAndReceiverId(user1Id, user2Id)
                .orElseThrow().getId();

        mockMvc.perform(post("/api/v1/contacts/reject/" + requestId2)
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk());

        // They should now be blocked - use the isBlocked query (no UserPairId needed)
        assert userBlockRepository.isBlocked(user1Id, user2Id);

        // user1 can no longer send a request
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isForbidden());
    }

    // ─── Remove Contact ───────────────────────────────────────────────────────

    @Test
    void removeContact_ExistingContact_Returns200AndMutuallyRemoves() throws Exception {
        // Establish contact
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        UUID reqId = friendRequestRepository.findBySenderIdAndReceiverId(user1Id, user2Id)
                .orElseThrow().getId();
        mockMvc.perform(post("/api/v1/contacts/accept/" + reqId)
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk());

        // user1 removes user2
        mockMvc.perform(delete("/api/v1/contacts/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        // user2's contact list should also be empty 
        mockMvc.perform(get("/api/v1/contacts")
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── Block User ───────────────────────────────────────────────────────────

    @Test
    void blockUser_Returns200AndRemovesFromContacts() throws Exception {
        // First become contacts
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        UUID reqId = friendRequestRepository.findBySenderIdAndReceiverId(user1Id, user2Id)
                .orElseThrow().getId();
        mockMvc.perform(post("/api/v1/contacts/accept/" + reqId)
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk());

        // Then block
        mockMvc.perform(post("/api/v1/contacts/block/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        // user2 should see empty contact list now
        mockMvc.perform(get("/api/v1/contacts")
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void blockUser_Self_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/contacts/block/" + user1Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isBadRequest());
    }

    // ─── List Contacts / Requests ─────────────────────────────────────────────

    @Test
    void getContacts_EmptyList_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/contacts")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getIncomingRequests_Returns200() throws Exception {
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/contacts/requests/incoming")
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].sender.username").value("user1"));
    }

    @Test
    void getOutgoingRequests_Returns200() throws Exception {
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/contacts/requests/outgoing")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].receiver.username").value("user2"));
    }

    @Test
    void contactEndpoints_WithoutToken_Return401() throws Exception {
        mockMvc.perform(get("/api/v1/contacts")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/contacts/request/" + user2Id)).andExpect(status().isUnauthorized());
    }

    // ─── Utils ────────────────────────────────────────────────────────────────

    private String login(String username, String password) throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        String res = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
        String search = "\"token\":\"";
        int start = res.indexOf(search) + search.length();
        return res.substring(start, res.indexOf("\"", start));
    }
}
