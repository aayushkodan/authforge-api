package com.aayush.authforge.authfordgeapi.common.exceptions;

public class TooManyOtpAttemptsException extends RuntimeException {
    public TooManyOtpAttemptsException(String tooManyAttempts) {
        super(tooManyAttempts);
    }
}
