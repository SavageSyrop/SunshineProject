package com.shevtsov.sunshine.common;

public class SecurityConstants {
    public static final String  SECRET = "SECRET_KEY";
    public static final long DAY_IN_MILLIS = 1000*60*60*24;
    public static final long EXPIRATION_TIME = DAY_IN_MILLIS*10L; // 10 days
    public static final String TOKEN_PREFIX = "Reaper ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/sign_up";
}

