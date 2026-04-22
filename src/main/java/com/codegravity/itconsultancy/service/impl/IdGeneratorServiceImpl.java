package com.codegravity.itconsultancy.service.impl;

import com.codegravity.itconsultancy.constants.AppConstants;
import com.codegravity.itconsultancy.repository.CandidateRepository;
import com.codegravity.itconsultancy.repository.EmployeeRepository;
import com.codegravity.itconsultancy.service.IdGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class IdGeneratorServiceImpl implements IdGeneratorService {

    private final EmployeeRepository employeeRepository;
    private final CandidateRepository candidateRepository;

    @Override
    public String generateEmployeeId() {
        String year = String.valueOf(Year.now().getValue());
        String prefix = AppConstants.EMPLOYEE_ID_PREFIX
                + AppConstants.ID_SEPARATOR
                + year
                + AppConstants.ID_SEPARATOR;

        long count = employeeRepository.countByEmployeeIdStartingWith(prefix);
        return prefix + formatSequence(count + 1);
    }

    @Override
    public String generateCandidateId() {
        String year = String.valueOf(Year.now().getValue());
        String prefix = AppConstants.CANDIDATE_ID_PREFIX
                + AppConstants.ID_SEPARATOR
                + year
                + AppConstants.ID_SEPARATOR;

        long count = candidateRepository.countByCandidateIdStartingWith(prefix);
        return prefix + formatSequence(count + 1);
    }

    private String formatSequence(long sequence) {
        return String.format("%0" + AppConstants.ID_SEQUENCE_LENGTH + "d", sequence);
    }
}