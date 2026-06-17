package com.codegravity.itconsultancy.integration;

import com.codegravity.itconsultancy.container.MySQLContainerConfig;
import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.enums.EmailStatus;
import com.codegravity.itconsultancy.repository.CandidateRepository;
import com.codegravity.itconsultancy.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import com.codegravity.itconsultancy.dto.request.RegistrationEmailRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration test: register → login → update profile → verify DB.
 * Uses a real MySQL container via Testcontainers; Flyway runs all migrations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(MySQLContainerConfig.class)
@ActiveProfiles("test")
class CandidateProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidateRepository candidateRepository;

    // Instantiated directly — avoids Spring Boot 4.x context wiring issue
    // with ObjectMapper when @SpringBootTest + @AutoConfigureMockMvc are combined.
    // We only parse Map<String, Object> so no custom modules are needed.
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Avoid real SMTP calls during integration tests
    @MockitoBean
    private MailService mailService;

    @BeforeEach
    void stubMail() {
        given(mailService.sendRegistrationEmail(any(RegistrationEmailRequest.class)))
                .willReturn(EmailStatus.SENT);
    }

    @Test
    @DisplayName("register → login → PUT /profile → verify DB state")
    void registerLoginUpdateProfile_persistsProfileToDatabase() throws Exception {

        // ── 1. Register ──────────────────────────────────────────────
        Map<String, String> registerBody = Map.of(
                "firstName", "Integration",
                "lastName", "Tester",
                "email", "integration.tester@example.com",
                "password", "SecurePass123!",
                "phone", "5551234567"
        );

        String registerJson = mockMvc.perform(post("/api/candidates/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        String candidateId = (String)
                ((Map<String, Object>) objectMapper.readValue(registerJson, Map.class).get("data"))
                        .get("generatedId");
        assertThat(candidateId).isNotBlank();

        // ── 2. Login to get JWT ───────────────────────────────────────
        Map<String, String> loginBody = Map.of(
                "email", "integration.tester@example.com",
                "password", "SecurePass123!",
                "userType", "CANDIDATE"
        );

        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        String accessToken = (String)
                ((Map<String, Object>) objectMapper.readValue(loginJson, Map.class).get("data"))
                        .get("accessToken");
        assertThat(accessToken).isNotBlank();

        // ── 3. PUT /api/candidates/{id}/profile ───────────────────────
        Map<String, String> profileBody = Map.of(
                "phone", "5551234567",
                "address", "456 Integration Ave",
                "resumeUrl", "https://s3.example.com/resume.pdf",
                "highestEducation", "Master's",
                "fieldOfStudy", "Software Engineering",
                "workAuthorization", "Citizen",
                "toolsTechnologies", "Java, Spring Boot, Kubernetes",
                "accommodationNeeded", "Remote work"
        );

        mockMvc.perform(put("/api/candidates/" + candidateId + "/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.highestEducation").value("Master's"))
                .andExpect(jsonPath("$.data.workAuthorization").value("Citizen"));

        // ── 4. Verify database state ──────────────────────────────────
        Candidate saved = candidateRepository.findByCandidateId(candidateId).orElseThrow();

        assertThat(saved.getPhone()).isEqualTo("5551234567");
        assertThat(saved.getAddress()).isEqualTo("456 Integration Ave");
        assertThat(saved.getResumeUrl()).isEqualTo("https://s3.example.com/resume.pdf");
        assertThat(saved.getHighestEducation()).isEqualTo("Master's");
        assertThat(saved.getFieldOfStudy()).isEqualTo("Software Engineering");
        assertThat(saved.getWorkAuthorization()).isEqualTo("Citizen");
        assertThat(saved.getToolsTechnologies()).isEqualTo("Java, Spring Boot, Kubernetes");
        assertThat(saved.getAccommodationNeeded()).isEqualTo("Remote work");
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("PUT /profile with wrong JWT → 403 Forbidden")
    void updateProfile_wrongCandidate_returns403() throws Exception {

        Map<String, String> targetBody = Map.of(
                "firstName", "Target", "lastName", "User",
                "email", "target.user403@example.com",
                "password", "SecurePass123!", "phone", "5559990001"
        );
        Map<String, String> attackerBody = Map.of(
                "firstName", "Attacker", "lastName", "User",
                "email", "attacker.user403@example.com",
                "password", "SecurePass123!", "phone", "5559990002"
        );

        String targetJson = mockMvc.perform(post("/api/candidates/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(targetBody)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        String targetId = (String)
                ((Map<String, Object>) objectMapper.readValue(targetJson, Map.class).get("data"))
                        .get("generatedId");

        mockMvc.perform(post("/api/candidates/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attackerBody)));

        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "attacker.user403@example.com",
                                       "password", "SecurePass123!",
                                       "userType", "CANDIDATE"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        String attackerToken = (String)
                ((Map<String, Object>) objectMapper.readValue(loginJson, Map.class).get("data"))
                        .get("accessToken");

        mockMvc.perform(put("/api/candidates/" + targetId + "/profile")
                        .header("Authorization", "Bearer " + attackerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "phone", "5559990001",
                                "highestEducation", "Injected",
                                "workAuthorization", "None",
                                "toolsTechnologies", "Hacking tools"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /profile with missing required fields → 400 with field-level errors")
    void updateProfile_missingRequiredFields_returns400() throws Exception {

        Map<String, String> registerBody = Map.of(
                "firstName", "Validation", "lastName", "Tester",
                "email", "validation.tester@example.com",
                "password", "SecurePass123!", "phone", "5550000001"
        );

        String registerJson = mockMvc.perform(post("/api/candidates/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        String candidateId = (String)
                ((Map<String, Object>) objectMapper.readValue(registerJson, Map.class).get("data"))
                        .get("generatedId");

        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "validation.tester@example.com",
                                "password", "SecurePass123!",
                                "userType", "CANDIDATE"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        String token = (String)
                ((Map<String, Object>) objectMapper.readValue(loginJson, Map.class).get("data"))
                        .get("accessToken");

        // Send request with invalid phone and missing required fields
        mockMvc.perform(put("/api/candidates/" + candidateId + "/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "phone", "NOT-A-PHONE"   // fails @Pattern; omits other @NotNull fields
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isMap());
    }
}
