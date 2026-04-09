package com.huseyinsacikay.controller;

import com.huseyinsacikay.entity.Pitch;
import com.huseyinsacikay.repository.PitchRepository;
import com.huseyinsacikay.repository.ReservationRepository;
import com.huseyinsacikay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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

    private String user1Token;
    private UUID user1Id;
    private UUID user2Id;
    private UUID pitchId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

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

        user1Token = registerUserAndGetToken("user1", "user1@test.com", "pass1");
        user1Id = userRepository.findByUsername("user1").orElseThrow().getId();

        registerUserAndGetToken("user2", "user2@test.com", "pass2");
        user2Id = userRepository.findByUsername("user2").orElseThrow().getId();
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

    private String registerUserAndGetToken(String username, String email, String password) throws Exception {
        String requestBody = String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"phoneNumber\":\"111\"}",
                username,
                email,
                password
        );
        String response = mockMvc.perform(post("/api/v1/auth/register")
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
