package com.algotic.services;

import org.springframework.stereotype.Component;

@Component
public interface EmailService {
    void sendOTPEmail(int otp, String email);

    void sendVerificationEmail(String email, String text);

    void sendContactUsEmail(String name, String email, String phone, String message);
}
