package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.dto.request.CandidateProfileUpdateRequest;
import com.codegravity.itconsultancy.dto.response.CandidateProfileResponse;
import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.repository.CandidateRepository;
import com.codegravity.itconsultancy.repository.RoleRepository;
import com.codegravity.itconsultancy.service.impl.CandidateServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock private CandidateRepository candidateRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IdGeneratorService idGeneratorService;
    @Mock private MailService mailService;

    @InjectMocks
    private CandidateServiceImpl candidateService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("updateProfile() -> success for candidate's own profile")
    void updateProfile_ownCandidateProfile_success() {
        Candidate candidate = candidate();
        CandidateProfileUpdateRequest request = request();
        given(candidateRepository.findByCandidateId("CAN-001")).willReturn(Optional.of(candidate));
        given(candidateRepository.save(any(Candidate.class))).willAnswer(invocation -> {
            Candidate saved = invocation.getArgument(0);
            saved.setUpdatedAt(LocalDateTime.of(2026, 6, 1, 12, 0));
            return saved;
        });
        authenticate("jane.doe@example.com", "ROLE_CANDIDATE");

        CandidateProfileResponse response = candidateService.updateProfile("CAN-001", request);

        ArgumentCaptor<Candidate> captor = ArgumentCaptor.forClass(Candidate.class);
        verify(candidateRepository).save(captor.capture());
        Candidate saved = captor.getValue();

        assertThat(saved.getCandidateId()).isEqualTo("CAN-001");
        assertThat(saved.getPhone()).isEqualTo("9876543210");
        assertThat(saved.getAddress()).isEqualTo("456 Updated Ave");
        assertThat(saved.getResumeUrl()).isEqualTo("https://cdn.example.com/new-resume.pdf");
        assertThat(response.getCandidateId()).isEqualTo("CAN-001");
        assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 1, 12, 0));
    }

    @Test
    @DisplayName("updateProfile() -> success for admin updating any candidate")
    void updateProfile_adminCanUpdateAnyCandidate_success() {
        Candidate candidate = candidate();
        CandidateProfileUpdateRequest request = request();
        given(candidateRepository.findByCandidateId("CAN-001")).willReturn(Optional.of(candidate));
        given(candidateRepository.save(any(Candidate.class))).willAnswer(invocation -> invocation.getArgument(0));
        authenticate("admin@example.com", "ROLE_ADMIN");

        CandidateProfileResponse response = candidateService.updateProfile("CAN-001", request);

        assertThat(response.getCandidateId()).isEqualTo("CAN-001");
        verify(candidateRepository).save(any(Candidate.class));
    }

    @Test
    @DisplayName("updateProfile() -> denies candidate updating another candidate's profile")
    void updateProfile_otherCandidate_throwsAccessDenied() {
        given(candidateRepository.findByCandidateId("CAN-001")).willReturn(Optional.of(candidate()));
        authenticate("other.candidate@example.com", "ROLE_CANDIDATE");

        assertThatThrownBy(() -> candidateService.updateProfile("CAN-001", request()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Candidates can update only their own profile");

        verify(candidateRepository, never()).save(any());
    }

    private Candidate candidate() {
        Candidate candidate = Candidate.builder()
                .candidateId("CAN-001")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .password("encoded-password")
                .phone("1234567890")
                .address("123 Main St")
                .appliedRole("Java Developer")
                .resumeUrl("https://cdn.example.com/old-resume.pdf")
                .build();
        candidate.setUpdatedAt(LocalDateTime.of(2026, 5, 31, 12, 0));
        return candidate;
    }

    private CandidateProfileUpdateRequest request() {
        CandidateProfileUpdateRequest request = new CandidateProfileUpdateRequest();
        request.setPhone("9876543210");
        request.setAddress("456 Updated Ave");
        request.setResumeUrl("https://cdn.example.com/new-resume.pdf");
        return request;
    }

    private void authenticate(String username, String authority) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                username,
                "password",
                List.of(new SimpleGrantedAuthority(authority))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
