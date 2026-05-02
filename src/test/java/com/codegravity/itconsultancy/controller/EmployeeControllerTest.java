package com.codegravity.itconsultancy.controller;

import com.codegravity.itconsultancy.config.TestSecurityConfig;
import com.codegravity.itconsultancy.dto.response.RegistrationResponse;
import com.codegravity.itconsultancy.enums.EmailStatus;
import com.codegravity.itconsultancy.enums.UserType;
import com.codegravity.itconsultancy.exception.DuplicateResourceException;
import com.codegravity.itconsultancy.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest loads ONLY the web layer: EmployeeController + MockMvc.
// Does NOT start DB, does NOT load services — fast and focused.
// We import TestSecurityConfig to bypass JWT filter.
@WebMvcTest(EmployeeController.class)
@Import(TestSecurityConfig.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;      // Simulates HTTP requests without a real server

    @Autowired
    private ObjectMapper objectMapper; // Converts Java objects ↔ JSON

    // @MockitoBean replaces the real EmployeeService bean with a Mockito mock
    // in the Spring context — different from @Mock which is pure Mockito
    @MockitoBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("POST /api/employees/register → 201 Created with valid payload")
    void register_validRequest_returns201() throws Exception {
        // GIVEN — build the JSON payload
        Map<String, String> requestBody = Map.of(
                "firstName", "John",
                "lastName", "Doe",
                "email", "john.doe@example.com",
                "password", "SecurePass123!",
                "phone", "1234567890",
                "address", "123 Main St"
        );

        RegistrationResponse mockResponse = RegistrationResponse.builder()
                .message("Registration successful")
                .userType(UserType.EMPLOYEE)
                .generatedId("EMP-001")
                .emailStatus(EmailStatus.SENT)
                .build();

        given(employeeService.register(any())).willReturn(mockResponse);

        // WHEN + THEN — perform POST and assert HTTP response
        mockMvc.perform(post("/api/employees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())                    // 201
                .andExpect(jsonPath("$.data.generatedId").value("EMP-001"))
                .andExpect(jsonPath("$.data.emailStatus").value("SENT"))
                .andExpect(jsonPath("$.message").value("Employee registered successfully"));
    }

    @Test
    @DisplayName("POST /api/employees/register → 409 Conflict when email duplicate")
    void register_duplicateEmail_returns409() throws Exception {
        Map<String, String> requestBody = Map.of(
                "firstName", "John",
                "lastName", "Doe",
                "email", "exists@example.com",
                "password", "SecurePass123!",
                "phone", "1234567890",
                "address", "123 Main St"
        );

        // Make the service throw DuplicateResourceException
        // Your GlobalExceptionHandler should catch this and return 409
        given(employeeService.register(any()))
                .willThrow(new DuplicateResourceException("Email already registered: exists@example.com"));

        mockMvc.perform(post("/api/employees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isConflict()); // 409 — assumes your @ControllerAdvice maps this
    }

    @Test
    @DisplayName("POST /api/employees/register → 400 Bad Request when fields missing")
    void register_missingFields_returns400() throws Exception {
        // Empty body — @Valid should reject this
        Map<String, String> requestBody = Map.of("email", "bad");

        mockMvc.perform(post("/api/employees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest()); // 400 — @Valid kicks in
    }
}