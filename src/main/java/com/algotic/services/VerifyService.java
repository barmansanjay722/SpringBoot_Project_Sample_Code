package com.algotic.services;

import com.algotic.model.request.VerifyRequest;
import com.algotic.model.response.VerifyResponse;
import org.springframework.http.ResponseEntity;

public interface VerifyService {
    ResponseEntity<VerifyResponse> verifyOtpAndGetToken(VerifyRequest verifyRequest);
}
