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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Match Requests API.
 * Tests: request to join, invite, accept, reject with JoinPolicy enforcement.
 * Uses in-memory H2 database.
 */
@SpringBootTest
class MatchRequestControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository userRepository;
    @Autowired private PitchRepository pitchRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ReservationParticipantRepository participantRepository;
    @Autowired private MatchRequestRepository matchRequestRepository;
    @Autowired private UserContactRepository userContactRepository;
    @Autowired private FriendRequestRepository friendRequestRepository;
    @Autowired private UserBlockRepository userBlockRepository;
    @Autowired private ContactStrikeRepository contactStrikeRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String token1;
    private String token2;
    private String token3;
    private UUID user1Id;
    private UUID user2Id;
    private UUID user3Id;
    private UUID pitchId;
    private UUID publicReservationId;
    private UUID friendsReservationId;
    private UUID inviteReservationId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()).build();

        // Clean all dependent tables
        participantRepository.deleteAll();
        matchRequestRepository.deleteAll();
        userContactRepository.deleteAll();
        userBlockRepository.deleteAll();
        contactStrikeRepository.deleteAll();
        friendRequestRepository.deleteAll();
        reservationRepository.deleteAll();
        pitchRepository.deleteAll();
        userRepository.deleteAll();

        User u1 = userRepository.save(User.builder()
                .username("organizer").email("org@test.com")
                .password(passwordEncoder.encode("Org1_pass")).phoneNumber("001")
                .role(Role.USER).isActive(true).build());
        user1Id = u1.getId();

        User u2 = userRepository.save(User.builder()
                .username("member").email("mem@test.com")
                .password(passwordEncoder.encode("Mem1_pass")).phoneNumber("002")
                .role(Role.USER).isActive(true).build());
        user2Id = u2.getId();

        User u3 = userRepository.save(User.builder()
                .username("stranger").email("str@test.com")
                .password(passwordEncoder.encode("Str1_pass")).phoneNumber("003")
                .role(Role.USER).isActive(true).build());
        user3Id = u3.getId();

        Pitch pitch = pitchRepository.save(Pitch.builder()
                .name("Arena").location("Istanbul").hourlyPrice(BigDecimal.valueOf(100))
                .capacity(11).isAvailable(true).build());
        pitchId = pitch.getId();

        // Create reservations for organizer (user1) with different policies
        publicReservationId = createReservation(u1, pitch, JoinPolicy.PUBLIC);
        friendsReservationId = createReservation(u1, pitch, JoinPolicy.FRIENDS_ONLY);
        inviteReservationId = createReservation(u1, pitch, JoinPolicy.INVITE_ONLY);

        token1 = login("organizer", "Org1_pass");
        token2 = login("member", "Mem1_pass");
        token3 = login("stranger", "Str1_pass");
    }

    // ─── Request to Join ─────────────────────────────────────────────────────

    @Test
    void requestToJoin_PublicReservation_AnyoneCanRequest_Returns200() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + publicReservationId + "/join")
                .header("Authorization", "Bearer " + token3))
                .andExpect(status().isOk());
    }

    @Test
    void requestToJoin_FriendsOnly_StrangerIsRejected_Returns403() throws Exception {
        // user3 is not a friend of user1 (organizer)
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + friendsReservationId + "/join")
                .header("Authorization", "Bearer " + token3))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestToJoin_FriendsOnly_FriendCanRequest_Returns200() throws Exception {
        // Make user2 a friend of user1
        makeContacts(user1Id, user2Id);

        mockMvc.perform(post("/api/v1/match-requests/reservations/" + friendsReservationId + "/join")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());
    }

    @Test
    void requestToJoin_InviteOnly_Returns403() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + inviteReservationId + "/join")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestToJoin_AlreadyRequested_Returns409() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + publicReservationId + "/join")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());

        // Second request
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + publicReservationId + "/join")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isConflict());
    }

    @Test
    void requestToJoin_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + publicReservationId + "/join"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Invite ───────────────────────────────────────────────────────────────

    @Test
    void invite_OrganizerCanInviteToInviteOnly_Returns200() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + inviteReservationId + "/invite/" + user2Id)
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());
    }

    @Test
    void invite_NonOrganizerCannotInvite_Returns403() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + inviteReservationId + "/invite/" + user3Id)
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
    }

    @Test
    void invite_DuplicateInvite_Returns409() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + inviteReservationId + "/invite/" + user2Id)
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/match-requests/reservations/" + inviteReservationId + "/invite/" + user2Id)
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isConflict());
    }

    // ─── Accept ───────────────────────────────────────────────────────────────

    @Test
    void acceptJoinRequest_ByOrganizer_AddsParticipantAndDeletesRequest() throws Exception {
        // user2 requests to join
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + publicReservationId + "/join")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());

        UUID reqId = matchRequestRepository
                .findByReservationIdAndUserIdAndTypeAndStatus(
                        publicReservationId, user2Id, MatchRequestType.JOIN_REQUEST, MatchRequestStatus.PENDING)
                .orElseThrow().getId();

        // organizer accepts
        mockMvc.perform(post("/api/v1/match-requests/" + reqId + "/accept")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());

        // user2 should now be in the reservation's users list
        mockMvc.perform(get("/api/v1/reservations/" + publicReservationId + "/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username=='member')]").exists());
    }

    @Test
    void acceptJoinRequest_ByNonOrganizer_Returns403() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + publicReservationId + "/join")
                .header("Authorization", "Bearer " + token3))
                .andExpect(status().isOk());

        UUID reqId = matchRequestRepository
                .findByReservationIdAndUserIdAndTypeAndStatus(
                        publicReservationId, user3Id, MatchRequestType.JOIN_REQUEST, MatchRequestStatus.PENDING)
                .orElseThrow().getId();

        // user2 (not organizer) tries to accept
        mockMvc.perform(post("/api/v1/match-requests/" + reqId + "/accept")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
    }

    @Test
    void acceptInvite_ByInvitedUser_Returns200AndJoinsMatch() throws Exception {
        // Organizer invites user2
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + inviteReservationId + "/invite/" + user2Id)
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());

        UUID reqId = matchRequestRepository
                .findByReservationIdAndUserIdAndTypeAndStatus(
                        inviteReservationId, user2Id, MatchRequestType.MATCH_INVITE, MatchRequestStatus.PENDING)
                .orElseThrow().getId();

        // user2 accepts their invite
        mockMvc.perform(post("/api/v1/match-requests/" + reqId + "/accept")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());

        // user2 should be in the match
        mockMvc.perform(get("/api/v1/reservations/" + inviteReservationId + "/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username=='member')]").exists());
    }

    @Test
    void acceptInvite_ByWrongUser_Returns403() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + inviteReservationId + "/invite/" + user2Id)
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());

        UUID reqId = matchRequestRepository
                .findByReservationIdAndUserIdAndTypeAndStatus(
                        inviteReservationId, user2Id, MatchRequestType.MATCH_INVITE, MatchRequestStatus.PENDING)
                .orElseThrow().getId();

        // user3 (not invited) tries to accept user2's invite
        mockMvc.perform(post("/api/v1/match-requests/" + reqId + "/accept")
                .header("Authorization", "Bearer " + token3))
                .andExpect(status().isForbidden());
    }

    // ─── Reject ───────────────────────────────────────────────────────────────

    @Test
    void rejectJoinRequest_ByOrganizer_Returns200() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + publicReservationId + "/join")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());

        UUID reqId = matchRequestRepository
                .findByReservationIdAndUserIdAndTypeAndStatus(
                        publicReservationId, user2Id, MatchRequestType.JOIN_REQUEST, MatchRequestStatus.PENDING)
                .orElseThrow().getId();

        mockMvc.perform(post("/api/v1/match-requests/" + reqId + "/reject")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());
    }

    @Test
    void rejectInvite_ByInvitedUser_Returns200() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + inviteReservationId + "/invite/" + user2Id)
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());

        UUID reqId = matchRequestRepository
                .findByReservationIdAndUserIdAndTypeAndStatus(
                        inviteReservationId, user2Id, MatchRequestType.MATCH_INVITE, MatchRequestStatus.PENDING)
                .orElseThrow().getId();

        mockMvc.perform(post("/api/v1/match-requests/" + reqId + "/reject")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());
    }

    // ─── List Requests ────────────────────────────────────────────────────────

    @Test
    void listPendingRequestsForReservation_AsOrganizer_Returns200() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + publicReservationId + "/join")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/match-requests/reservations/" + publicReservationId)
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].user.username").value("member"));
    }

    @Test
    void listPendingRequestsForReservation_AsNonOrganizer_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/match-requests/reservations/" + publicReservationId)
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
    }

    @Test
    void listPendingInvitesForUser_Returns200() throws Exception {
        mockMvc.perform(post("/api/v1/match-requests/reservations/" + inviteReservationId + "/invite/" + user2Id)
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/match-requests/me")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private UUID createReservation(User organizer, Pitch pitch, JoinPolicy policy) {
        Reservation r = reservationRepository.save(Reservation.builder()
                .organizer(organizer).pitch(pitch)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(2))
                .totalPrice(BigDecimal.valueOf(200))
                .status(ReservationStatus.PENDING)
                .joinPolicy(policy)
                .build());
        participantRepository.save(ReservationParticipant.builder()
                .reservation(r).user(organizer).isOrganizer(true).build());
        return r.getId();
    }

    private void makeContacts(UUID id1, UUID id2) {
        User u1 = userRepository.findById(id1).orElseThrow();
        User u2 = userRepository.findById(id2).orElseThrow();
        UUID pairU1 = id1.compareTo(id2) < 0 ? id1 : id2;
        UUID pairU2 = id1.compareTo(id2) < 0 ? id2 : id1;
        User first = id1.compareTo(id2) < 0 ? u1 : u2;
        User second = id1.compareTo(id2) < 0 ? u2 : u1;
        userContactRepository.save(UserContact.builder().user1(first).user2(second).build());
    }

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
