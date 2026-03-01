package com.aayush.authforge.authfordgeapi.common.exceptions;

public class OtpExpiredException extends RuntimeException {
    public OtpExpiredException(String otpExpired) {
        super(otpExpired);
    }
}
