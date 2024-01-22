package com.algotic.services;

import com.algotic.model.request.ResendOtpRequest;
import com.algotic.model.response.OtpResponse;
import java.util.Date;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface OtpService {
    String generateAndSendOtp(String email, String phone);

    String randomRefCode();

    void saveOtp(
            String setOtpValue,
            String setReferenceCodeValue,
            int setExpiresInValue,
            String setUserIdValue,
            Boolean setIsVerifiedValue,
            String setOtpTypeValue,
            Date verifiedAt);

    public ResponseEntity<OtpResponse> resendOtp(ResendOtpRequest resendOtpRequest);
}
