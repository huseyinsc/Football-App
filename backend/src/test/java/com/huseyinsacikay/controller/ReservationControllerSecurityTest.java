package com.huseyinsacikay.controller;

import com.huseyinsacikay.entity.Pitch;
import com.huseyinsacikay.entity.Reservation;
import com.huseyinsacikay.entity.ReservationStatus;
import com.huseyinsacikay.entity.User;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ReservationControllerSecurityTest {

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
    private String user2Token;
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

        // Create Pitch
        Pitch pitch = pitchRepository.save(Pitch.builder()
                .name("Camp Nou")
                .location("Barcelona")
                .hourlyPrice(BigDecimal.valueOf(100.0))
                .capacity(11)
                .isAvailable(true)
                .build());
        pitchId = pitch.getId();

        // Register User 1
        String req1 = "{\"username\":\"user1\",\"email\":\"user1@test.com\",\"password\":\"StrongPass1_\",\"phoneNumber\":\"111\"}";
        String res1 = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req1))
                .andReturn().getResponse().getContentAsString();
        
        user1Token = extractToken(res1);
        user1Id = userRepository.findByUsername("user1").get().getId();

        // Register User 2
        String req2 = "{\"username\":\"user2\",\"email\":\"user2@test.com\",\"password\":\"StrongPass2_\",\"phoneNumber\":\"222\"}";
        String res2 = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req2))
                .andReturn().getResponse().getContentAsString();
        
        user2Token = extractToken(res2);
        user2Id = userRepository.findByUsername("user2").get().getId();
    }
    
    private String extractToken(String response) {
        // Primitive way to extract "token":"<value>"
        String search = "\"token\":\"";
        int start = response.indexOf(search) + search.length();
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }

    @Test
    void userCannotCreateReservationForAnotherUser() throws Exception {
        String req = "{\"userId\":\"" + user2Id + "\",\"pitchId\":\"" + pitchId + "\",\"startTime\":\"2026-05-01T10:00:00\",\"endTime\":\"2026-05-01T12:00:00\"}";

        mockMvc.perform(post("/api/v1/reservations")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanCreateReservationForThemselves() throws Exception {
        String req = "{\"userId\":\"" + user1Id + "\",\"pitchId\":\"" + pitchId + "\",\"startTime\":\"2026-05-01T10:00:00\",\"endTime\":\"2026-05-01T12:00:00\"}";

        mockMvc.perform(post("/api/v1/reservations")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotGetReservationsOfAnotherUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + user2Id + "/reservations")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanGetOwnReservations() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + user1Id + "/reservations")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotGetAnotherUsersReservationById() throws Exception {
        UUID reservationId = createReservationForUser(user2Id, "user2");

        mockMvc.perform(get("/api/v1/reservations/" + reservationId)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanGetOwnReservationById() throws Exception {
        UUID reservationId = createReservationForUser(user1Id, "user1");

        mockMvc.perform(get("/api/v1/reservations/" + reservationId)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotCancelAnotherUsersReservation() throws Exception {
        UUID reservationId = createReservationForUser(user2Id, "user2");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/reservations/" + reservationId)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanCancelOwnReservation() throws Exception {
        UUID reservationId = createReservationForUser(user1Id, "user1");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/reservations/" + reservationId)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isNoContent());
    }

    private UUID createReservationForUser(UUID reservationUserId, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Reservation reservation = reservationRepository.save(Reservation.builder()
                .organizer(user)
                .pitch(pitchRepository.findById(pitchId).orElseThrow())
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .status(ReservationStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(200.00))
                .build());
        if (!user.getId().equals(reservationUserId)) {
            throw new IllegalStateException("Test reservation user mismatch");
        }
        return reservation.getId();
    }
}
