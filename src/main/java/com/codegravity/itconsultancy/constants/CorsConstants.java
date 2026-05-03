package com.codegravity.itconsultancy.constants;

public final class CorsConstants {

    private CorsConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    public static final long MAX_AGE_SECONDS = 3600L;

    public static final String[] ALLOWED_METHODS = {
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    };

    public static final String[] ALLOWED_HEADERS = {
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "Cache-Control"
    };

    public static final String[] EXPOSED_HEADERS = {
            "Authorization"
    };
}