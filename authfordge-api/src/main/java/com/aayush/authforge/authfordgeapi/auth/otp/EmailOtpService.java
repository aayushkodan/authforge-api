package com.aayush.authforge.authfordgeapi.auth.otp;

import com.aayush.authforge.authfordgeapi.common.email.EmailService;
import com.aayush.authforge.authfordgeapi.common.exceptions.*;
import com.aayush.authforge.authfordgeapi.entities.User;
import com.aayush.authforge.authfordgeapi.user.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailOtpService implements OtpService {

    private final EmailOtpRepository emailOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final jakarta.persistence.EntityManager entityManager;


    @Override
    @Transactional
    public void generateAndSendOtp(String email) {

        emailOtpRepository.deleteByEmail(email);
        entityManager.flush();

        int otp = new SecureRandom().nextInt(900000) + 100000;
        String otpString = String.valueOf(otp);

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(email);
        emailOtp.setOtpHash(passwordEncoder.encode(otpString));
        emailOtp.setType(OtpType.EMAIL_VERIFICATION);
        emailOtpRepository.save(emailOtp);

        emailService.sendOtpEmail(email, otpString);
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String otp) {

        EmailOtp emailOtp = emailOtpRepository.findByEmail(email)
                .orElseThrow(() -> new OtpNotFoundException("OTP not found"));


        if (emailOtp.getExpiresAt() == null ||
                emailOtp.getExpiresAt().isBefore(Instant.now())) {
            throw new OtpExpiredException("OTP expired");
        }

        if (emailOtp.getAttempts() >= 5) {
            throw new TooManyOtpAttemptsException("Too many attempts");
        }

        if (!passwordEncoder.matches(otp, emailOtp.getOtpHash())) {
            emailOtp.setAttempts(emailOtp.getAttempts() + 1);
            emailOtpRepository.save(emailOtp);
            throw new InvalidOtpException("Invalid OTP");
        }

        // success
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        user.setEnabled(true);

        emailOtpRepository.delete(emailOtp);
    }
}
