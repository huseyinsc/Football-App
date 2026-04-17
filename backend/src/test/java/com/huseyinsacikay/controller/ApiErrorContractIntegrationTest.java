package com.huseyinsacikay.controller;

import com.huseyinsacikay.entity.Pitch;
import com.huseyinsacikay.entity.Role;
import com.huseyinsacikay.entity.User;
import com.huseyinsacikay.repository.PitchRepository;
import com.huseyinsacikay.repository.ReservationRepository;
import com.huseyinsacikay.repository.UserRepository;
import com.huseyinsacikay.repository.ReservationParticipantRepository;
import com.huseyinsacikay.repository.MatchRequestRepository;
import com.huseyinsacikay.repository.FriendRequestRepository;
import com.huseyinsacikay.repository.UserContactRepository;
import com.huseyinsacikay.repository.UserBlockRepository;
import com.huseyinsacikay.repository.ContactStrikeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ApiErrorContractIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PitchRepository pitchRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired private ReservationParticipantRepository participantRepository;
    @Autowired private MatchRequestRepository matchRequestRepository;
    @Autowired private FriendRequestRepository friendRequestRepository;
    @Autowired private UserContactRepository userContactRepository;
    @Autowired private UserBlockRepository userBlockRepository;
    @Autowired private ContactStrikeRepository contactStrikeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String user1Token;
    private UUID user1Id;
    private UUID user2Id;
    private UUID pitchId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        participantRepository.deleteAll();
        matchRequestRepository.deleteAll();
        userContactRepository.deleteAll();
        userBlockRepository.deleteAll();
        contactStrikeRepository.deleteAll();
        friendRequestRepository.deleteAll();
        reservationRepository.deleteAll();
        pitchRepository.deleteAll();
        userRepository.deleteAll();

        Pitch pitch = pitchRepository.save(Pitch.builder()
                .name("Camp Nou")
                .location("Barcelona")
                .hourlyPrice(BigDecimal.valueOf(100.0))
                .capacity(11)
                .isAvailable(true)
                .build());
        pitchId = pitch.getId();

        // Create users directly in the database instead of via API
        User user1 = userRepository.save(User.builder()
                .username("user1")
                .email("user1@test.com")
                .password(passwordEncoder.encode("pass1"))
                .phoneNumber("111")
                .role(Role.USER)
                .isActive(true)
                .build());
        user1Id = user1.getId();

        User user2 = userRepository.save(User.builder()
                .username("user2")
                .email("user2@test.com")
                .password(passwordEncoder.encode("pass2"))
                .phoneNumber("222")
                .role(Role.USER)
                .isActive(true)
                .build());
        user2Id = user2.getId();

        // Get a token for user1 by calling login
        user1Token = loginAndGetToken("user1", "pass1");
    }

    @Test
    void loginWithWrongPassword_ShouldReturnUnauthorizedApiError() throws Exception {
        String requestBody = "{\"username\":\"user1\",\"password\":\"wrong-password\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.exception.code").value("1011"))
                .andExpect(jsonPath("$.exception.path").value("/api/v1/auth/login"))
                .andExpect(jsonPath("$.exception.message").value("Invalid username or password"));
    }

    @Test
    void malformedToken_ShouldReturnUnauthorizedApiError() throws Exception {
        mockMvc.perform(get("/api/v1/reservations/user/" + user1Id)
                        .header("Authorization", "Bearer malformed-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.exception.code").value("1010"))
                .andExpect(jsonPath("$.exception.path").value("/api/v1/reservations/user/" + user1Id))
                .andExpect(jsonPath("$.exception.message").value("Authentication is required to access this resource"));
    }

    @Test
    void preAuthorizeFailure_ShouldReturnForbiddenApiError() throws Exception {
        String requestBody = "{\"userId\":\"" + user2Id + "\",\"pitchId\":\"" + pitchId
                + "\",\"startTime\":\"2026-05-01T10:00:00\",\"endTime\":\"2026-05-01T12:00:00\"}";

        mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.exception.code").value("1007"))
                .andExpect(jsonPath("$.exception.path").value("/api/v1/reservations"))
                .andExpect(jsonPath("$.exception.message").value("You are not allowed to access this reservation"));
    }

    @Test
    void validationFailure_ShouldReturnBadRequestApiError() throws Exception {
        String requestBody = "{\"userId\":\"" + user1Id + "\",\"pitchId\":\"" + pitchId
                + "\",\"startTime\":\"2020-05-01T10:00:00\",\"endTime\":\"2026-05-01T12:00:00\"}";

        mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.exception.code").value("1009"))
                .andExpect(jsonPath("$.exception.path").value("/api/v1/reservations"))
                .andExpect(jsonPath("$.exception.message.startTime").value("Start time must be in the future"));
    }

    @Test
    void invalidSortParameter_SwaggerStyle_ShouldReturn400NotServerError() throws Exception {
        // Swagger UI sends sort=["asc"] which is not a valid field name.
        // Previously this caused a 500. Must return 400 Bad Request.
        mockMvc.perform(get("/api/v1/contacts/requests/outgoing")
                        .param("sort", "[\"asc\"]")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.exception.code").value("1009"))
                .andExpect(jsonPath("$.exception.message").value(
                        "Invalid sort or query parameter. Use format: sort=fieldName,asc|desc"));
    }

    @Test
    void validSortParameter_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/contacts/requests/outgoing")
                        .param("sort", "createdAt,asc")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String requestBody = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                username,
                password
        );
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractToken(response);
    }

    private String extractToken(String response) {
        String search = "\"token\":\"";
        int start = response.indexOf(search) + search.length();
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
}
