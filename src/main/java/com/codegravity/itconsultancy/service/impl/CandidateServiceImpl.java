package com.codegravity.itconsultancy.service.impl;

import com.codegravity.itconsultancy.dto.request.CandidateRegisterRequest;
import com.codegravity.itconsultancy.dto.response.CandidateListResponse;
import com.codegravity.itconsultancy.dto.response.RegistrationResponse;
import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.entity.RoleEntity;
import com.codegravity.itconsultancy.enums.EmailStatus;
import com.codegravity.itconsultancy.enums.Role;
import com.codegravity.itconsultancy.enums.UserStatus;
import com.codegravity.itconsultancy.enums.UserType;
import com.codegravity.itconsultancy.exception.DuplicateResourceException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .notes(request.getNotes())
                .isActive(true)
                .roles(Set.of(candidateRole))
                .build();

        candidateRepository.save(candidate);
        log.info("Candidate registered: {} | id: {}", request.getEmail(), generatedId);

        EmailStatus emailStatus = mailService.sendRegistrationEmail(
                request.getEmail(),
                request.getFirstName(),
                UserType.CANDIDATE,
                generatedId
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
}