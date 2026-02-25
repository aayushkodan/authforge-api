package com.aayush.authforge.authfordgeapi.auth.otp;

import org.springframework.stereotype.Service;

public interface OtpService {

    void generateAndSendOtp(String email);

    boolean verifyOtp(String email, String otp);
}
