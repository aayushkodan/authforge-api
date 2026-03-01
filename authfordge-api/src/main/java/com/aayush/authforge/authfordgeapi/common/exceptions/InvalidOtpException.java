package com.aayush.authforge.authfordgeapi.common.exceptions;

public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException(String invalidOtp) {
        super(invalidOtp);
    }
}
