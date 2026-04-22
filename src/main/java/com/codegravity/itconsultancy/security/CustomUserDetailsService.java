package com.codegravity.itconsultancy.security;

import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.entity.Employee;
import com.codegravity.itconsultancy.repository.CandidateRepository;
import com.codegravity.itconsultancy.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final CandidateRepository candidateRepository;

    /**
     * Username format: "email::USERTYPE"
     * e.g. "john@example.com::EMPLOYEE"
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] parts = username.split("::");
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Invalid username format: " + username);
        }

        String email = parts[0];
        String userType = parts[1];

        return switch (userType) {
            case "EMPLOYEE" -> loadEmployee(email);
            case "CANDIDATE" -> loadCandidate(email);
            default -> throw new UsernameNotFoundException("Unknown user type: " + userType);
        };
    }

    private UserDetails loadEmployee(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found: " + email));

        var authorities = employee.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());

        return new User(employee.getEmail(), employee.getPassword(), authorities);
    }

    private UserDetails loadCandidate(String email) {
        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Candidate not found: " + email));

        var authorities = candidate.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());

        return new User(candidate.getEmail(), candidate.getPassword(), authorities);
    }
}