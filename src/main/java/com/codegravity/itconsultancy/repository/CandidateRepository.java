package com.codegravity.itconsultancy.repository;

import com.codegravity.itconsultancy.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long>, JpaSpecificationExecutor<Candidate> {

    Optional<Candidate> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Candidate> findByCandidateId(String candidateId);

    long countByCandidateIdStartingWith(String prefix);
}