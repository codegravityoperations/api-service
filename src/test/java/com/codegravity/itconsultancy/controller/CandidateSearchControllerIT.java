package com.codegravity.itconsultancy.controller;

import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.enums.UserStatus;
import com.codegravity.itconsultancy.repository.CandidateRepository;
import com.codegravity.itconsultancy.service.MailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CandidateSearchControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidateRepository candidateRepository;

    @MockitoBean
    private MailService mailService;

    @BeforeEach
    void setUp() {
        candidateRepository.deleteAll();

        candidateRepository.save(candidate("CAN-2026-0001", "John",    "Smith",   "john.smith@test.com",   "1111111111", UserStatus.ACTIVE));
        candidateRepository.save(candidate("CAN-2026-0002", "Jane",    "Doe",     "jane.doe@test.com",     "2222222222", UserStatus.ACTIVE));
        candidateRepository.save(candidate("CAN-2026-0003", "Bob",     "Johnson", "bob.j@test.com",        "3333333333", UserStatus.INACTIVE));
        candidateRepository.save(candidate("CAN-2026-0004", "Alice",   "Brown",   "alice.b@test.com",      "4444444444", UserStatus.ACTIVE));
        candidateRepository.save(candidate("CAN-2026-0005", "Charlie", "Smith",   "charlie.s@test.com",    "5555555555", UserStatus.PENDING));
    }

    @AfterEach
    void tearDown() {
        candidateRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/candidates → 200 with all candidates when no filters applied")
    @WithMockUser(roles = "EMPLOYEE")
    void searchAll_asEmployee_returns200WithAllCandidates() throws Exception {
        mockMvc.perform(get("/api/candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.content", hasSize(5)));
    }

    @Test
    @DisplayName("GET /api/candidates?firstName=John → 1 result")
    @WithMockUser(roles = "EMPLOYEE")
    void searchByFirstName_returnsMatchingCandidates() throws Exception {
        mockMvc.perform(get("/api/candidates").param("firstName", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].candidateId").value("CAN-2026-0001"))
                .andExpect(jsonPath("$.data.content[0].fullName").value("John Smith"));
    }

    @Test
    @DisplayName("GET /api/candidates?lastName=Smith → 2 results (John + Charlie)")
    @WithMockUser(roles = "EMPLOYEE")
    void searchByLastName_returnsMultipleMatches() throws Exception {
        mockMvc.perform(get("/api/candidates").param("lastName", "Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/candidates?email=jane.doe → matches by partial email")
    @WithMockUser(roles = "EMPLOYEE")
    void searchByEmail_returnsMatchingCandidate() throws Exception {
        mockMvc.perform(get("/api/candidates").param("email", "jane.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].email").value("jane.doe@test.com"));
    }

    @Test
    @DisplayName("GET /api/candidates?phoneNumber=3333 → matches by partial phone")
    @WithMockUser(roles = "EMPLOYEE")
    void searchByPhoneNumber_returnsMatchingCandidate() throws Exception {
        mockMvc.perform(get("/api/candidates").param("phoneNumber", "3333"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].phoneNumber").value("3333333333"));
    }

    @Test
    @DisplayName("GET /api/candidates?status=ACTIVE → 3 active candidates")
    @WithMockUser(roles = "EMPLOYEE")
    void filterByStatus_returnsOnlyActiveCandiates() throws Exception {
        mockMvc.perform(get("/api/candidates").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3));
    }

    @Test
    @DisplayName("GET /api/candidates?page=0&size=2 → first page with 2 items")
    @WithMockUser(roles = "EMPLOYEE")
    void pagination_returnsCorrectPage() throws Exception {
        mockMvc.perform(get("/api/candidates").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.totalPages").value(3));
    }

    @Test
    @DisplayName("GET /api/candidates → 200 when accessed with ROLE_ADMIN")
    @WithMockUser(roles = "ADMIN")
    void searchAll_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/candidates"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/candidates → 403 Forbidden for ROLE_CANDIDATE")
    @WithMockUser(roles = "CANDIDATE")
    void search_asCandidate_returns403() throws Exception {
        mockMvc.perform(get("/api/candidates"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/candidates response includes required fields")
    @WithMockUser(roles = "EMPLOYEE")
    void search_responseContainsAllRequiredFields() throws Exception {
        mockMvc.perform(get("/api/candidates").param("candidateId", "CAN-2026-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].candidateId").exists())
                .andExpect(jsonPath("$.data.content[0].fullName").exists())
                .andExpect(jsonPath("$.data.content[0].email").exists())
                .andExpect(jsonPath("$.data.content[0].phoneNumber").exists())
                .andExpect(jsonPath("$.data.content[0].status").exists())
                .andExpect(jsonPath("$.data.content[0].createdAt").exists())
                .andExpect(jsonPath("$.data.content[0].updatedAt").exists());
    }

    private Candidate candidate(String id, String firstName, String lastName, String email, String phone, UserStatus status) {
        return Candidate.builder()
                .candidateId(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password("hashed-password")
                .phone(phone)
                .status(status)
                .build();
    }
}