package com.codegravity.itconsultancy.controller;

import com.codegravity.itconsultancy.config.TestSecurityConfig;
import com.codegravity.itconsultancy.dto.response.CandidateProfileResponse;
import com.codegravity.itconsultancy.security.CustomUserDetailsService;
import com.codegravity.itconsultancy.security.JwtTokenProvider;
import com.codegravity.itconsultancy.service.CandidateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CandidateController.class)
@Import(TestSecurityConfig.class)
class CandidateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CandidateService candidateService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("PUT /api/candidates/{candidateId}/profile -> 200 OK with valid payload")
    void updateProfile_validRequest_returns200() throws Exception {
        CandidateProfileResponse response = CandidateProfileResponse.builder()
                .candidateId("CAN-001")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("1234567890")
                .address("123 Main St")
                .appliedRole("Java Developer")
                .resumeUrl("https://cdn.example.com/resume.pdf")
                .updatedAt(LocalDateTime.of(2026, 6, 1, 12, 0))
                .build();

        given(candidateService.updateProfile(eq("CAN-001"), any())).willReturn(response);

        Map<String, String> requestBody = Map.of(
                "phone", "1234567890",
                "address", "123 Main St",
                "resumeUrl", "https://cdn.example.com/resume.pdf"
        );

        mockMvc.perform(put("/api/candidates/CAN-001/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Candidate profile updated successfully"))
                .andExpect(jsonPath("$.data.candidateId").value("CAN-001"))
                .andExpect(jsonPath("$.data.phone").value("1234567890"))
                .andExpect(jsonPath("$.data.resumeUrl").value("https://cdn.example.com/resume.pdf"))
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @Test
    @DisplayName("PUT /api/candidates/{candidateId}/profile -> 400 Bad Request with field details")
    void updateProfile_invalidRequest_returns400WithFieldDetails() throws Exception {
        Map<String, String> requestBody = Map.of(
                "phone", "",
                "address", "A".repeat(256),
                "resumeUrl", "R".repeat(501)
        );

        mockMvc.perform(put("/api/candidates/CAN-001/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.phone").value("Phone is required"))
                .andExpect(jsonPath("$.data.address").value("Address must not exceed 255 characters"))
                .andExpect(jsonPath("$.data.resumeUrl").value("Resume URL must not exceed 500 characters"));
    }
}
