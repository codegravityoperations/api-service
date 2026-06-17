package com.codegravity.itconsultancy.controller;

import com.codegravity.itconsultancy.dto.response.PresignedUploadResponse;
import com.codegravity.itconsultancy.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
public class CandidateUploadController {

    private final S3Service s3Service;

    public CandidateUploadController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/{id}/upload/presign")
    public ResponseEntity<PresignedUploadResponse> generateUploadUrl(
            @PathVariable Long id,
            @RequestParam String fileType
    ) {
        return ResponseEntity.ok(
                s3Service.generateUploadUrl(id, fileType)
        );
    }
    @PutMapping ("/{id}/upload/confirm")
    public ResponseEntity<String> confirmUpload(
            @PathVariable Long id,
            @RequestParam String fileType,
            @RequestParam String s3Key
    ) {
        s3Service.confirmUpload(id, fileType, s3Key);
        return ResponseEntity.ok("Upload confirmed");
    }
}


