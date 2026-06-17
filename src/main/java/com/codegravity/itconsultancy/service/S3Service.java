package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.dto.response.PresignedUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.repository.CandidateRepository;

import java.time.Duration;

@Service
public class S3Service {

    private final S3Presigner presigner;
    private final CandidateRepository candidateRepository;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3Service(S3Presigner presigner,
                     CandidateRepository candidateRepository) {

        this.presigner = presigner;
        this.candidateRepository = candidateRepository;
    }

    public PresignedUploadResponse generateUploadUrl(Long candidateId, String fileType) {

        String s3Key = "candidates/" + candidateId + "/" + fileType;

        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .putObjectRequest(putObjectRequest)
                        .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(presignRequest);

        return new PresignedUploadResponse(
                presignedRequest.url().toString(),
                s3Key,
                900
        );
    }

    public String generateDownloadUrl(String s3Key) {

        return presigner.presignGetObject(getObjectPresignRequest ->
                getObjectPresignRequest
                        .signatureDuration(Duration.ofMinutes(15))
                        .getObjectRequest(getObjectRequest ->
                                getObjectRequest
                                        .bucket(bucketName)
                                        .key(s3Key)
                        )
        ).url().toString();
    }
    public void confirmUpload(Long candidateId,
                              String fileType,
                              String s3Key) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));

        if (fileType.equalsIgnoreCase("resume")) {
            candidate.setResumeUrl(s3Key);
        } else if (fileType.equalsIgnoreCase("ead")) {
            candidate.setEadUrl(s3Key);
        }

        candidateRepository.save(candidate);
    }

}