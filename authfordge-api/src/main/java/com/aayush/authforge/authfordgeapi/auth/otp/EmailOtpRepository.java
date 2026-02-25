package com.aayush.authforge.authfordgeapi.auth.otp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, UUID> {
    void deleteByEmail(String email);

    Optional<EmailOtp> findByEmail(String email);
}
