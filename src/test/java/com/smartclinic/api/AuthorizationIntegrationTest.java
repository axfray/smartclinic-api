package com.smartclinic.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartclinic.api.model.User;
import com.smartclinic.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthorizationIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "pass123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void seedPatients() {
        createPatientIfMissing("patientA@test.local");
        createPatientIfMissing("patientB@test.local");
    }

    @Test
    void patient_cannotListUsers() throws Exception {
        String token = login("patientA@test.local");

        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void patient_cannotReadOtherPatientAppointments() throws Exception {
        String token = login("patientA@test.local");
        Long otherPatientId = idOf("patientB@test.local");

        mockMvc.perform(get("/api/appointments/patient/" + otherPatientId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void patient_canReadOwnAppointments() throws Exception {
        String token = login("patientA@test.local");
        Long ownId = idOf("patientA@test.local");

        mockMvc.perform(get("/api/appointments/patient/" + ownId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void unknownAuthenticatedPath_returnsNotFound() throws Exception {
        String token = login("patientA@test.local");

        mockMvc.perform(get("/api/does-not-exist").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void index_isPublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void me_returnsCurrentUser() throws Exception {
        String token = login("patientA@test.local");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("patientA@test.local"))
                .andExpect(jsonPath("$.role").value("ROLE_PATIENT"));
    }

    @Test
    void me_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patient_cannotListAllAppointments() throws Exception {
        String token = login("patientA@test.local");

        mockMvc.perform(get("/api/appointments").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private void createPatientIfMissing(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            userRepository.save(User.builder()
                    .firstName("Paciente")
                    .lastName("Test")
                    .email(email)
                    .passwordHash(passwordEncoder.encode(PASSWORD))
                    .role(User.Role.ROLE_PATIENT)
                    .isActive(true)
                    .build());
        }
    }

    private Long idOf(String email) {
        return userRepository.findByEmail(email).orElseThrow().getId();
    }

    private String login(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }
}
