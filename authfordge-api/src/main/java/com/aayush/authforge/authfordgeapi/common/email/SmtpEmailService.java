package com.aayush.authforge.authfordgeapi.common.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String to, String otp) {

        String subject = "Your Verification Code";

        String htmlContent = buildOtpTemplate(otp);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    private String buildOtpTemplate(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="background:#f5f7fa;font-family:Arial;">
                  <div style="max-width:500px;margin:40px auto;
                              background:#fff;padding:40px;
                              border-radius:12px;text-align:center;">
                
                    <h2>Verify Your Email</h2>
                    <p>Use the code below to continue</p>
                
                    <div style="
                        font-size:32px;
                        letter-spacing:8px;
                        font-weight:bold;
                        color:#4f46e5;
                        background:#f0f4ff;
                        padding:20px;
                        border-radius:10px;
                        margin:20px 0;">
                """ + otp + """
                    </div>
                
                    <p style="color:#888;font-size:14px;">
                        Expires in 5 minutes
                    </p>
                  </div>
                </body>
                </html>
                """;
    }
}