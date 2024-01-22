package com.algotic.services;

import com.algotic.model.request.LoginRequest;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.model.response.LoginResponse;
import com.algotic.model.response.VerifyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    ResponseEntity<LoginResponse> login(LoginRequest loginRequest, String type);

    ResponseEntity<GlobalMessageResponse> logout();

    ResponseEntity<VerifyResponse> termsAccepted();
}
