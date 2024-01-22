package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.LoginRequest;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.model.response.LoginResponse;
import com.algotic.model.response.VerifyResponse;
import com.algotic.services.AuthService;
import com.algotic.utils.AlgoticUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private JwtHelper jwtHelper;

    @CrossOrigin
    @PostMapping("/login/{type}")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest, @PathVariable String type, BindingResult errors) {
        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().get(0).getDefaultMessage());
            throw exception;
        }
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Send Request for Login ", AlgoticUtils.objectToJsonString(loginRequest), null, null));
        return authService.login(loginRequest, type);
    }

    @CrossOrigin
    @PostMapping("/logout")
    public ResponseEntity<GlobalMessageResponse> logout() {
        log.info(logConfig.getLogHandler().getInfoLog("Send Request for Logout", "Logout is in progress", null, null));
        return authService.logout();
    }

    @CrossOrigin
    @PostMapping("/termsAccepted")
    public ResponseEntity<VerifyResponse> termsAccepted() {
        return authService.termsAccepted();
    }
}
