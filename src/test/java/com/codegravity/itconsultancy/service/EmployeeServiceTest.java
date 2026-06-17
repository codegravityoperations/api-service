package com.codegravity.itconsultancy.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// @ExtendWith(MockitoExtension.class) = pure Mockito, zero Spring context.
// This test runs in ~200ms. No DB. No network. No application startup.
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    // @Mock creates a fake/stub of each dependency
    @Mock private EmployeeRepository employeeRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IdGeneratorService idGeneratorService;
    @Mock private MailService mailService;

    // @InjectMocks creates the real EmployeeServiceImpl and injects
    // the @Mock objects above into its constructor (Lombok @RequiredArgsConstructor)
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeRegisterRequest validRequest;
    private RoleEntity employeeRole;

    @BeforeEach
    void setUp() {
        // Build a valid request object used across multiple tests
        validRequest = EmployeeRegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("SecurePass123!")
                .phone("1234567890")
                .address("123 Main St")
                .build();

        employeeRole = new RoleEntity();
        employeeRole.setName(Role.ROLE_EMPLOYEE);
    }

    @Test
    @DisplayName("register() → success: returns RegistrationResponse with generated ID")
    void register_success() {
        // GIVEN — set up what each mock returns when called
        given(employeeRepository.existsByEmail(validRequest.getEmail())).willReturn(false);
        given(idGeneratorService.generateEmployeeId()).willReturn("EMP-001");
        given(roleRepository.findByName(Role.ROLE_EMPLOYEE)).willReturn(Optional.of(employeeRole));
        given(passwordEncoder.encode(anyString())).willReturn("encoded-password");
        given(employeeRepository.save(any(Employee.class))).willAnswer(inv -> inv.getArgument(0));
        given(mailService.sendRegistrationEmail(any(RegistrationEmailRequest.class)))
                .willReturn(EmailStatus.SENT);

        // WHEN — call the real method
        RegistrationResponse response = employeeService.register(validRequest);

        // THEN — assert the result
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Registration successful");
        assertThat(response.getGeneratedId()).isEqualTo("EMP-001");
        assertThat(response.getUserType()).isEqualTo(UserType.EMPLOYEE);
        assertThat(response.getEmailStatus()).isEqualTo(EmailStatus.SENT);

        // Verify the repository was actually called to save
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("register() → throws DuplicateResourceException when email already exists")
    void register_duplicateEmail_throwsException() {
        // GIVEN — email already exists in DB
        given(employeeRepository.existsByEmail(validRequest.getEmail())).willReturn(true);

        // WHEN + THEN — calling register() must throw DuplicateResourceException
        assertThatThrownBy(() -> employeeService.register(validRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("john.doe@example.com");

        // Critical: verify save() was NEVER called — no partial state
        verify(employeeRepository, never()).save(any());
        verify(mailService, never()).sendRegistrationEmail(any(RegistrationEmailRequest.class));
    }

    @Test
    @DisplayName("register() → throws RuntimeException when ROLE_EMPLOYEE missing from DB")
    void register_roleMissing_throwsException() {
        // GIVEN — role doesn't exist (bad migration / fresh DB)
        given(employeeRepository.existsByEmail(anyString())).willReturn(false);
        given(idGeneratorService.generateEmployeeId()).willReturn("EMP-002");
        given(roleRepository.findByName(Role.ROLE_EMPLOYEE)).willReturn(Optional.empty());

        // WHEN + THEN
        assertThatThrownBy(() -> employeeService.register(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ROLE_EMPLOYEE not found");
    }
}