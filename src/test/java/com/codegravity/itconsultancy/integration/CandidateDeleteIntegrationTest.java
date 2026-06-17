package com.codegravity.itconsultancy.integration;

import com.codegravity.itconsultancy.container.MySQLContainerConfig;
import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.enums.UserStatus;
import com.codegravity.itconsultancy.repository.CandidateRepository;
import com.codegravity.itconsultancy.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.codegravity.itconsultancy.enums.EmailStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MySQLContainerConfig.class)
@ActiveProfiles("test")
class CandidateDeleteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidateRepository candidateRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private MailService mailService;

    @Test
    @DisplayName("EMPLOYEE deletes candidate → 200 with confirmation message and DB marked DELETED")
    void deleteCandidate_asEmployee_returns200AndSoftDeletes() throws Exception {
        given(mailService.sendRegistrationEmail(any(RegistrationEmailRequest.class)))
                .willReturn(EmailStatus.SENT);

        // ── 1. Register candidate ──────────────────────────────────────
        String candidateJson = mockMvc.perform(post("/api/candidates/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Delete",
                                "lastName", "Target",
                                "email", "delete.target@example.com",
                                "password", "SecurePass123!",
                                "phone", "5550000010"
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        String candidateId = (String)
                ((Map<String, Object>) objectMapper.readValue(candidateJson, Map.class).get("data"))
                        .get("generatedId");
        assertThat(candidateId).isNotBlank();

        // ── 2. Register employee ───────────────────────────────────────
        mockMvc.perform(post("/api/employees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Test",
                                "lastName", "Employee",
                                "email", "test.employee.delete@example.com",
                                "password", "SecurePass123!",
                                "phone", "5550000011"
                        ))))
                .andExpect(status().isCreated());

        // ── 3. Login as employee ───────────────────────────────────────
        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "test.employee.delete@example.com",
                                "password", "SecurePass123!",
                                "userType", "EMPLOYEE"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        String employeeToken = (String)
                ((Map<String, Object>) objectMapper.readValue(loginJson, Map.class).get("data"))
                        .get("accessToken");
        assertThat(employeeToken).isNotBlank();

        // ── 4. DELETE candidate ────────────────────────────────────────
        mockMvc.perform(delete("/api/candidates/" + candidateId)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Candidate deleted successfully"));

        // ── 5. Verify database state ───────────────────────────────────
        Candidate deleted = candidateRepository.findByCandidateId(candidateId).orElseThrow();
        assertThat(deleted.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(deleted.isActive()).isFalse();
        assertThat(deleted.getDeletedAt()).isNotNull();
        assertThat(deleted.getDeletedBy()).isEqualTo("test.employee.delete@example.com");
    }

    @Test
    @DisplayName("CANDIDATE tries to delete → 403 Forbidden")
    void deleteCandidate_asCandidate_returns403() throws Exception {
        given(mailService.sendRegistrationEmail(any(RegistrationEmailRequest.class)))
                .willReturn(EmailStatus.SENT);

        // ── 1. Register two candidates ────────────────────────────────
        String targetJson = mockMvc.perform(post("/api/candidates/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Unauthorized",
                                "lastName", "Target",
                                "email", "unauth.target@example.com",
                                "password", "SecurePass123!",
                                "phone", "5550000020"
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        String targetId = (String)
                ((Map<String, Object>) objectMapper.readValue(targetJson, Map.class).get("data"))
                        .get("generatedId");

        mockMvc.perform(post("/api/candidates/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "firstName", "Unauthorized",
                        "lastName", "Attacker",
                        "email", "unauth.attacker@example.com",
                        "password", "SecurePass123!",
                        "phone", "5550000021"
                ))));

        // ── 2. Login as candidate ─────────────────────────────────────
        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "unauth.attacker@example.com",
                                "password", "SecurePass123!",
                                "userType", "CANDIDATE"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        String candidateToken = (String)
                ((Map<String, Object>) objectMapper.readValue(loginJson, Map.class).get("data"))
                        .get("accessToken");

        // ── 3. Attempt DELETE as CANDIDATE → 403 ─────────────────────
        mockMvc.perform(delete("/api/candidates/" + targetId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EMPLOYEE deletes non-existent candidate → 404")
    void deleteCandidate_notFound_returns404() throws Exception {
        given(mailService.sendRegistrationEmail(any(RegistrationEmailRequest.class)))
                .willReturn(EmailStatus.SENT);

        // Register and login as employee
        mockMvc.perform(post("/api/employees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "NotFound",
                                "lastName", "Employee",
                                "email", "notfound.employee@example.com",
                                "password", "SecurePass123!",
                                "phone", "5550000030"
                        ))))
                .andExpect(status().isCreated());

        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "notfound.employee@example.com",
                                "password", "SecurePass123!",
                                "userType", "EMPLOYEE"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        String employeeToken = (String)
                ((Map<String, Object>) objectMapper.readValue(loginJson, Map.class).get("data"))
                        .get("accessToken");

        mockMvc.perform(delete("/api/candidates/CND_9999_99999")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}