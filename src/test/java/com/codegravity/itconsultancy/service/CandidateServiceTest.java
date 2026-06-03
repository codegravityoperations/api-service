package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.dto.request.CandidateProfileUpdateRequest;
import com.codegravity.itconsultancy.dto.response.CandidateProfileResponse;
import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.exception.ResourceNotFoundException;
import com.codegravity.itconsultancy.repository.CandidateRepository;
import com.codegravity.itconsultancy.repository.RoleRepository;
import com.codegravity.itconsultancy.service.impl.CandidateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock private CandidateRepository candidateRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IdGeneratorService idGeneratorService;
    @Mock private MailService mailService;
    @Mock private Authentication authentication;

    @InjectMocks
    private CandidateServiceImpl candidateService;

    private Candidate existingCandidate;
    private CandidateProfileUpdateRequest validRequest;

    @BeforeEach
    void setUp() {
        existingCandidate = Candidate.builder()
                .candidateId("CND_2026_00001")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("9876543210")
                .build();

        validRequest = CandidateProfileUpdateRequest.builder()
                .highestEducation("Bachelor's")
                .fieldOfStudy("Computer Science")
                .workAuthorization("Citizen")
                .toolsTechnologies("Java, Spring Boot, Docker")
                .accommodationNeeded("None")
                .build();
    }

    // ── Happy path: CANDIDATE updates their own profile ──────────

    @Test
    @DisplayName("updateProfile() → CANDIDATE updates own profile, returns updated response")
    void updateProfile_candidateUpdatesOwn_returnsResponse() {
        given(candidateRepository.findByCandidateId("CND_2026_00001"))
                .willReturn(Optional.of(existingCandidate));
        given(authentication.getName()).willReturn("jane.smith@example.com::CANDIDATE");
        given(authentication.getAuthorities()).willAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE")));
        given(candidateRepository.save(any(Candidate.class)))
                .willAnswer(inv -> inv.getArgument(0));

        CandidateProfileResponse response =
                candidateService.updateProfile("CND_2026_00001", validRequest, authentication);

        assertThat(response).isNotNull();
        assertThat(response.getCandidateId()).isEqualTo("CND_2026_00001");
        assertThat(response.getHighestEducation()).isEqualTo("Bachelor's");
        assertThat(response.getFieldOfStudy()).isEqualTo("Computer Science");
        assertThat(response.getWorkAuthorization()).isEqualTo("Citizen");
        assertThat(response.getToolsTechnologies()).isEqualTo("Java, Spring Boot, Docker");
        assertThat(response.getAccommodationNeeded()).isEqualTo("None");

        verify(candidateRepository).save(any(Candidate.class));
    }

    // ── Happy path: ADMIN updates any profile ────────────────────

    @Test
    @DisplayName("updateProfile() → ADMIN can update any candidate's profile")
    void updateProfile_adminUpdatesAny_returnsResponse() {
        given(candidateRepository.findByCandidateId("CND_2026_00001"))
                .willReturn(Optional.of(existingCandidate));
        given(authentication.getAuthorities()).willAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        given(candidateRepository.save(any(Candidate.class)))
                .willAnswer(inv -> inv.getArgument(0));

        CandidateProfileResponse response =
                candidateService.updateProfile("CND_2026_00001", validRequest, authentication);

        assertThat(response).isNotNull();
        assertThat(response.getHighestEducation()).isEqualTo("Bachelor's");
        verify(candidateRepository).save(any(Candidate.class));
    }

    // ── Auth failure: CANDIDATE tries to update someone else ─────

    @Test
    @DisplayName("updateProfile() → throws AccessDeniedException when CANDIDATE updates another's profile")
    void updateProfile_candidateUpdatesOther_throwsAccessDenied() {
        given(candidateRepository.findByCandidateId("CND_2026_00001"))
                .willReturn(Optional.of(existingCandidate));
        given(authentication.getAuthorities()).willAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE")));
        given(authentication.getName()).willReturn("intruder@example.com::CANDIDATE");

        assertThatThrownBy(() ->
                candidateService.updateProfile("CND_2026_00001", validRequest, authentication))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ── Not found ─────────────────────────────────────────────────

    @Test
    @DisplayName("updateProfile() → throws ResourceNotFoundException when candidateId not found")
    void updateProfile_candidateNotFound_throwsResourceNotFoundException() {
        given(candidateRepository.findByCandidateId("CND_9999_99999"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                candidateService.updateProfile("CND_9999_99999", validRequest, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("CND_9999_99999");
    }
}
