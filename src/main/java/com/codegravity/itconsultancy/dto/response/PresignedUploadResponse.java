package com.codegravity.itconsultancy.dto.response;

public class PresignedUploadResponse {

    private String uploadUrl;
    private String s3Key;
    private int expiresIn;

    public PresignedUploadResponse(String uploadUrl, String s3Key, int expiresIn) {
        this.uploadUrl = uploadUrl;
        this.s3Key = s3Key;
        this.expiresIn = expiresIn;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public String getS3Key() {
        return s3Key;
    }

    public int getExpiresIn() {
        return expiresIn;
    }
}