package com.codegravity.itconsultancy.entity;

import com.codegravity.itconsultancy.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "candidates")
public class Candidate extends BaseEntity {

    @Column(name = "candidate_id", nullable = false, unique = true, length = 20)
    private String candidateId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "applied_role", length = 150)
    private String appliedRole;

    @Column(name = "resume_url", length = 500)
    private String resumeUrl;

    @Column(name = "ead_url", length = 500)
    private String eadUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "candidate_roles",
            joinColumns = @JoinColumn(name = "candidate_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();
}