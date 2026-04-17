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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for user profile and pitch management endpoints.
 * Covers: GET/PUT /users, GET/DELETE/PUT /pitches.
 * Uses an in-memory H2 database (see test/resources/application.yaml).
 */
@SpringBootTest
class UserAndPitchControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository userRepository;
    @Autowired private PitchRepository pitchRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ReservationParticipantRepository participantRepository;
    @Autowired private MatchRequestRepository matchRequestRepository;
    @Autowired private FriendRequestRepository friendRequestRepository;
    @Autowired private UserContactRepository userContactRepository;
    @Autowired private UserBlockRepository userBlockRepository;
    @Autowired private ContactStrikeRepository contactStrikeRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;
    private String user2Token;
    private java.util.UUID userId;
    private java.util.UUID user2Id;
    private java.util.UUID adminId;
    private java.util.UUID pitchId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()).build();

        // Delete in FK-safe order
        participantRepository.deleteAll();
        matchRequestRepository.deleteAll();
        userContactRepository.deleteAll();
        userBlockRepository.deleteAll();
        contactStrikeRepository.deleteAll();
        friendRequestRepository.deleteAll();
        reservationRepository.deleteAll();
        pitchRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(User.builder()
                .username("admin").email("admin@test.com")
                .password(passwordEncoder.encode("Admin1_pass"))
                .phoneNumber("000").role(Role.ADMIN).isActive(true).build());
        adminId = admin.getId();

        User user = userRepository.save(User.builder()
                .username("user1").email("user1@test.com")
                .password(passwordEncoder.encode("User1_pass"))
                .phoneNumber("111").role(Role.USER).isActive(true).build());
        userId = user.getId();

        User user2 = userRepository.save(User.builder()
                .username("user2").email("user2@test.com")
                .password(passwordEncoder.encode("User2_pass"))
                .phoneNumber("222").role(Role.USER).isActive(true).build());
        user2Id = user2.getId();

        Pitch pitch = pitchRepository.save(Pitch.builder()
                .name("Test Pitch").location("Istanbul")
                .hourlyPrice(BigDecimal.valueOf(100)).capacity(11)
                .isAvailable(true).build());
        pitchId = pitch.getId();

        userToken = login("user1", "User1_pass");
        adminToken = login("admin", "Admin1_pass");
        user2Token = login("user2", "User2_pass");
    }

    // ─── GET /users/me ────────────────────────────────────────────────────────

    @Test
    void getMe_WithValidToken_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    void getMe_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /users/{id} ──────────────────────────────────────────────────────

    @Test
    void getUserById_OwnProfile_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + userId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    void getUserById_OtherUserProfile_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + user2Id)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_AdminCanViewAnyUser_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + userId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ─── GET /users (list all) ─────────────────────────────────────────────────

    @Test
    void getAllUsers_AsAdmin_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_AsUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ─── PUT /users/{id} ──────────────────────────────────────────────────────

    @Test
    void updateUser_OwnProfile_Returns200() throws Exception {
        String body = "{\"username\":\"user1updated\",\"email\":\"updated@test.com\",\"phoneNumber\":\"999\"}";
        mockMvc.perform(put("/api/v1/users/" + userId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1updated"));
    }

    @Test
    void updateUser_OtherUserProfile_Returns403() throws Exception {
        String body = "{\"username\":\"hacked\",\"email\":\"hacked@test.com\",\"phoneNumber\":\"000\"}";
        mockMvc.perform(put("/api/v1/users/" + user2Id)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_AdminCanUpdateRole_Returns200() throws Exception {
        // Admin updates another user's role
        String body = "{\"role\":\"ADMIN\"}";
        mockMvc.perform(put("/api/v1/users/" + userId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    // ─── DELETE /users/{id} ───────────────────────────────────────────────────

    @Test
    void deleteUser_OwnAccount_Returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + user2Id)
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_OtherAccountAsUser_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + user2Id)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ─── Pitch Endpoints ──────────────────────────────────────────────────────

    @Test
    void createPitch_AsAdmin_Returns200() throws Exception {
        String body = "{\"name\":\"New Pitch\",\"location\":\"Ankara\",\"hourlyPrice\":80.0,\"capacity\":6}";
        mockMvc.perform(post("/api/v1/pitches")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Pitch"));
    }

    @Test
    void createPitch_AsUser_Returns403() throws Exception {
        String body = "{\"name\":\"Hack Pitch\",\"location\":\"Izmir\",\"hourlyPrice\":50.0,\"capacity\":5}";
        mockMvc.perform(post("/api/v1/pitches")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllPitches_PublicEndpoint_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/pitches"))
                .andExpect(status().isOk());
    }

    @Test
    void getPitchById_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/pitches/" + pitchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Pitch"));
    }

    @Test
    void getPitchById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/pitches/" + java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePitch_AsAdmin_Returns200() throws Exception {
        String body = "{\"name\":\"Updated Pitch\",\"location\":\"Bursa\",\"hourlyPrice\":120.0,\"capacity\":8}";
        mockMvc.perform(put("/api/v1/pitches/" + pitchId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Pitch"));
    }

    @Test
    void updatePitch_AsUser_Returns403() throws Exception {
        String body = "{\"name\":\"Hacked\",\"location\":\"Bursa\",\"hourlyPrice\":1.0,\"capacity\":2}";
        mockMvc.perform(put("/api/v1/pitches/" + pitchId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletePitch_AsAdmin_Returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/pitches/" + pitchId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePitch_AsUser_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/pitches/" + pitchId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPitchReservations_PublicEndpoint_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/pitches/" + pitchId + "/reservations"))
                .andExpect(status().isOk());
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

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
