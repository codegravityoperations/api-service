package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class S3ServiceTest {

    @Mock
    private S3Presigner presigner;

    @Mock
    private CandidateRepository candidateRepository;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        Field bucketField = S3Service.class.getDeclaredField("bucketName");
        bucketField.setAccessible(true);
        bucketField.set(s3Service, "test-bucket");
    }

    @Test
    void confirmUpload_shouldSaveResumeUrl() {

        Candidate candidate = new Candidate();
        candidate.setId(21L);

        when(candidateRepository.findById(21L))
                .thenReturn(Optional.of(candidate));

        s3Service.confirmUpload(
                21L,
                "resume",
                "candidates/21/resume"
        );

        assertEquals(
                "candidates/21/resume",
                candidate.getResumeUrl()
        );

        verify(candidateRepository, times(1))
                .save(candidate);
    }

    @Test
    void confirmUpload_shouldSaveEadUrl() {

        Candidate candidate = new Candidate();
        candidate.setId(21L);

        when(candidateRepository.findById(21L))
                .thenReturn(Optional.of(candidate));

        s3Service.confirmUpload(
                21L,
                "ead",
                "candidates/21/ead"
        );

        assertEquals(
                "candidates/21/ead",
                candidate.getEadUrl()
        );

        verify(candidateRepository, times(1))
                .save(candidate);
    }
}