package com.codegravity.itconsultancy.service.impl;

import com.codegravity.itconsultancy.dto.request.CandidateProfileUpdateRequest;
import com.codegravity.itconsultancy.dto.request.CandidateRegisterRequest;
import com.codegravity.itconsultancy.dto.request.RegistrationEmailRequest;
import com.codegravity.itconsultancy.dto.response.CandidateListResponse;
import com.codegravity.itconsultancy.dto.response.CandidateProfileResponse;
import com.codegravity.itconsultancy.dto.response.RegistrationResponse;
import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.entity.RoleEntity;
import com.codegravity.itconsultancy.enums.EmailStatus;
import com.codegravity.itconsultancy.enums.Role;
import com.codegravity.itconsultancy.enums.UserStatus;
import com.codegravity.itconsultancy.enums.UserType;
import com.codegravity.itconsultancy.exception.DuplicateResourceException;
import com.codegravity.itconsultancy.exception.ResourceNotFoundException;
import com.codegravity.itconsultancy.repository.CandidateRepository;
import com.codegravity.itconsultancy.repository.RoleRepository;
import com.codegravity.itconsultancy.service.CandidateService;
import com.codegravity.itconsultancy.service.IdGeneratorService;
import com.codegravity.itconsultancy.service.MailService;
import com.codegravity.itconsultancy.specification.CandidateSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdGeneratorService idGeneratorService;
    private final MailService mailService;

    @Override
    @Transactional
    public RegistrationResponse register(CandidateRegisterRequest request) {

        if (candidateRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        String generatedId = idGeneratorService.generateCandidateId();

        RoleEntity candidateRole = roleRepository.findByName(Role.ROLE_CANDIDATE)
                .orElseThrow(() -> new RuntimeException("ROLE_CANDIDATE not found in DB — check V2 migration"));

        Candidate candidate = Candidate.builder()
                .candidateId(generatedId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .appliedRole(request.getAppliedRole())
                .resumeUrl(request.getResumeUrl())
                .eadUrl(request.getEadUrl())
                .drivingLicenseUrl(request.getDrivingLicenseUrl())
                .notes(request.getNotes())
                .highestEducation(request.getHighestEducation())
                .fieldOfStudy(request.getFieldOfStudy())
                .workAuthorization(request.getWorkAuthorization())
                .toolsTechnologies(request.getToolsTechnologies())
                .accommodationNeeded(request.getAccommodationNeeded())
                .isActive(true)
                .roles(Set.of(candidateRole))
                .build();

        candidateRepository.save(candidate);
        log.info("Candidate registered: {} | id: {}", request.getEmail(), generatedId);

        EmailStatus emailStatus = mailService.sendRegistrationEmail(
                RegistrationEmailRequest.builder()
                        .toEmail(request.getEmail())
                        .firstName(request.getFirstName())
                        .userType(UserType.CANDIDATE)
                        .generatedId(generatedId)
                        .submissionDate(LocalDate.now())
                        .resumeUploaded(request.getResumeUrl() != null && !request.getResumeUrl().isBlank())
                        .eadUploaded(request.getEadUrl() != null && !request.getEadUrl().isBlank())
                        .drivingLicenseUploaded(request.getDrivingLicenseUrl() != null && !request.getDrivingLicenseUrl().isBlank())
                        .build()
        );

        return RegistrationResponse.builder()
                .message("Registration successful")
                .userType(UserType.CANDIDATE)
                .generatedId(generatedId)
                .emailStatus(emailStatus)
                .build();
    }

    @Override
    public Page<CandidateListResponse> searchCandidates(
            String candidateId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            UserStatus status,
            int page,
            int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return candidateRepository
                .findAll(CandidateSpecification.withFilters(candidateId, firstName, lastName, email, phoneNumber, status), pageable)
                .map(this::toListResponse);
    }

    @Override
    @Transactional
    public CandidateProfileResponse updateProfile(String candidateId,
                                                   CandidateProfileUpdateRequest request,
                                                   Authentication authentication) {

        Candidate candidate = candidateRepository.findByCandidateId(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + candidateId));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            // Principal name format: "email::USERTYPE" — see CustomUserDetailsService
            String principalName = authentication.getName();
            String authenticatedEmail = principalName.contains("::")
                    ? principalName.split("::")[0]
                    : principalName;

            if (!authenticatedEmail.equals(candidate.getEmail())) {
                throw new AccessDeniedException("You can only update your own profile");
            }
        }

        candidate.setPhone(request.getPhone());
        candidate.setAddress(request.getAddress());
        candidate.setResumeUrl(request.getResumeUrl());
        candidate.setHighestEducation(request.getHighestEducation());
        candidate.setFieldOfStudy(request.getFieldOfStudy());
        candidate.setWorkAuthorization(request.getWorkAuthorization());
        candidate.setToolsTechnologies(request.getToolsTechnologies());
        candidate.setAccommodationNeeded(request.getAccommodationNeeded());

        Candidate saved = candidateRepository.save(candidate);
        log.info("Profile updated for candidate: {}", candidateId);

        return toProfileResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCandidate(String candidateId, Authentication authentication) {
        Candidate candidate = candidateRepository.findByCandidateId(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + candidateId));

        String principal = authentication.getName();
        String deletedBy = principal.contains("::") ? principal.split("::")[0] : principal;

        candidate.setStatus(UserStatus.DELETED);
        candidate.setActive(false);
        candidate.setDeletedAt(LocalDateTime.now());
        candidate.setDeletedBy(deletedBy);

        candidateRepository.save(candidate);
        log.info("Candidate soft-deleted: {} by {}", candidateId, deletedBy);
    }

    private CandidateListResponse toListResponse(Candidate candidate) {
        return CandidateListResponse.builder()
                .candidateId(candidate.getCandidateId())
                .fullName(candidate.getFirstName() + " " + candidate.getLastName())
                .email(candidate.getEmail())
                .phoneNumber(candidate.getPhone())
                .status(candidate.getStatus())
                .createdAt(candidate.getCreatedAt())
                .updatedAt(candidate.getUpdatedAt())
                .build();
    }

    private CandidateProfileResponse toProfileResponse(Candidate c) {
        return CandidateProfileResponse.builder()
                .candidateId(c.getCandidateId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .address(c.getAddress())
                .appliedRole(c.getAppliedRole())
                .resumeUrl(c.getResumeUrl())
                .notes(c.getNotes())
                .highestEducation(c.getHighestEducation())
                .fieldOfStudy(c.getFieldOfStudy())
                .workAuthorization(c.getWorkAuthorization())
                .toolsTechnologies(c.getToolsTechnologies())
                .accommodationNeeded(c.getAccommodationNeeded())
                .status(c.getStatus())
                .active(c.isActive())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
