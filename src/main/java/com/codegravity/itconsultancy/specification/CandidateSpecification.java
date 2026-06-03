package com.codegravity.itconsultancy.specification;

import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.enums.UserStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CandidateSpecification {

    private CandidateSpecification() {}

    public static Specification<Candidate> withFilters(
            String candidateId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            UserStatus status
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (candidateId != null && !candidateId.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("candidateId")), "%" + candidateId.toLowerCase() + "%"));
            }
            if (firstName != null && !firstName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }
            if (lastName != null && !lastName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }
            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                predicates.add(cb.like(root.get("phone"), "%" + phoneNumber + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}