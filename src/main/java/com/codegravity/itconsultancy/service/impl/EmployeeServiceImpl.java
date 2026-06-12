package com.codegravity.itconsultancy.service.impl;

import com.codegravity.itconsultancy.dto.request.EmployeeRegisterRequest;
import com.codegravity.itconsultancy.dto.request.RegistrationEmailRequest;
import com.codegravity.itconsultancy.dto.response.RegistrationResponse;
import com.codegravity.itconsultancy.entity.Employee;
import com.codegravity.itconsultancy.entity.RoleEntity;
import com.codegravity.itconsultancy.enums.EmailStatus;
import com.codegravity.itconsultancy.enums.Role;
import com.codegravity.itconsultancy.enums.UserType;
import com.codegravity.itconsultancy.exception.DuplicateResourceException;
import com.codegravity.itconsultancy.repository.EmployeeRepository;
import com.codegravity.itconsultancy.repository.RoleRepository;
import com.codegravity.itconsultancy.service.EmployeeService;
import com.codegravity.itconsultancy.service.IdGeneratorService;
import com.codegravity.itconsultancy.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdGeneratorService idGeneratorService;
    private final MailService mailService;

    @Override
    @Transactional
    public RegistrationResponse register(EmployeeRegisterRequest request) {

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        String generatedId = idGeneratorService.generateEmployeeId();

        RoleEntity employeeRole = roleRepository.findByName(Role.ROLE_EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("ROLE_EMPLOYEE not found in DB — check V2 migration"));

        Employee employee = Employee.builder()
                .employeeId(generatedId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .isActive(true)
                .roles(Set.of(employeeRole))
                .build();

        employeeRepository.save(employee);
        log.info("Employee registered: {} | id: {}", request.getEmail(), generatedId);

        EmailStatus emailStatus = mailService.sendRegistrationEmail(
                RegistrationEmailRequest.builder()
                        .toEmail(request.getEmail())
                        .firstName(request.getFirstName())
                        .userType(UserType.EMPLOYEE)
                        .generatedId(generatedId)
                        .submissionDate(LocalDate.now())
                        .build()
        );

        return RegistrationResponse.builder()
                .message("Registration successful")
                .userType(UserType.EMPLOYEE)
                .generatedId(generatedId)
                .emailStatus(emailStatus)
                .build();
    }
}