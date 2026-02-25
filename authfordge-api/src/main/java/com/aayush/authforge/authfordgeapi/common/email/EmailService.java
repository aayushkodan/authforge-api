package com.aayush.authforge.authfordgeapi.common.email;


public interface EmailService {

    void sendOtpEmail(String to, String otp);
}
