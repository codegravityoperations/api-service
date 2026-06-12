package com.codegravity.itconsultancy.service.impl;

import com.codegravity.itconsultancy.dto.request.CandidateRegisterRequest;
import com.codegravity.itconsultancy.dto.response.RegistrationResponse;
import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.entity.RoleEntity;
import com.codegravity.itconsultancy.enums.EmailStatus;
import com.codegravity.itconsultancy.enums.Role;
import com.codegravity.itconsultancy.enums.UserType;
import com.codegravity.itconsultancy.exception.DuplicateResourceException;
import com.codegravity.itconsultancy.repository.CandidateRepository;
import com.codegravity.itconsultancy.repository.RoleRepository;
import com.codegravity.itconsultancy.service.CandidateService;
import com.codegravity.itconsultancy.service.IdGeneratorService;
import com.codegravity.itconsultancy.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                .degree(request.getDegree())
                .major(request.getMajor())
                .university(request.getUniversity())
                .workAuthorization(request.getWorkAuthorization())
                .needsAccommodation(request.getNeedsAccommodation())
                // Optional: If you want to save tools to your DB later, add .tools(request.getTools()) here
                .isActive(true)
                .roles(Set.of(candidateRole))
                .build();

        candidateRepository.save(candidate);
        log.info("Candidate registered: {} | id: {}", request.getEmail(), generatedId);

        // Added request.getTools() as the 10th parameter here
        EmailStatus emailStatus = mailService.sendRegistrationEmail(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                UserType.CANDIDATE,
                generatedId,
                request.getDegree(),
                request.getMajor(),
                request.getUniversity(),
                request.getWorkAuthorization(),
                request.getNeedsAccommodation(),
                request.getTools()
        );

        return RegistrationResponse.builder()
                .message("Registration successful")
                .userType(UserType.CANDIDATE)
                .generatedId(generatedId)
                .emailStatus(emailStatus)
                .build();
    }
}