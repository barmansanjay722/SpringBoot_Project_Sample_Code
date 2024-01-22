package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.LoginType;
import com.algotic.data.entities.AccessTokens;
import com.algotic.data.entities.BrokerCustomerDetails;
import com.algotic.data.entities.Users;
import com.algotic.data.repositories.AccessTokensRepo;
import com.algotic.data.repositories.BrokerCustomerDetailsRepo;
import com.algotic.data.repositories.UsersRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.LoginRequest;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.model.response.LoginResponse;
import com.algotic.model.response.VerifyResponse;
import com.algotic.services.AuthService;
import com.algotic.services.OtpService;
import com.algotic.utils.AlgoticUtils;
import com.algotic.utils.JwtTokenUtil;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private OtpService otpService;

    @Autowired
    private BrokerCustomerDetailsRepo brokerCustomerDetailsRepo;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private AccessTokensRepo accessTokensRepo;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private JwtHelper jwtHelper;

    @Value("${insdusfaceTestEmail}")
    private String insdusfaceTestEmail;

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest, String type) {
        LoginResponse loginResponse = new LoginResponse();
        try {
            Users userDetails = usersRepo.findForStatus(loginRequest.getUsername());
            handleLogging("Users Details", AlgoticUtils.objectToJsonString(userDetails), null, null);
            validateUserDetails(userDetails);
            Users customer = (usersRepo.findByEmail(loginRequest.getUsername()));
            if (customer != null) {
                if (customer.getRole().equalsIgnoreCase("admin")) {
                    if (LoginType.ALGOTIC.name().equalsIgnoreCase(type)) {

                        String passwordHash =
                                AlgoticUtils.generateHash(loginRequest.getPassword(), customer.getPasswordSalt());
                        String password = customer.getPasswordHash();

                        String userId = customer.getId();
                        Users getUserRole = usersRepo.findByID(userId);
                        String token = jwtTokenUtil.generateToken(userId, getUserRole.getRole());

                        Date expireIn = jwtTokenUtil.getExpirationDateFromToken(token);
                        saveToken(userId, token, expireIn);
                        loginResponse.setToken(token);

                        if (passwordHash.equals(password)) {
                            return new ResponseEntity<>(loginResponse, HttpStatus.OK);
                        } else {
                            CommonErrorCode errorCode = CommonErrorCode.PASSWORD_NOT_VALID;
                            handleLogging(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    null,
                                    String.valueOf(errorCode.getHttpStatus().value()));
                            throw new AlgoticException(errorCode);
                        }
                    } else {
                        CommonErrorCode errorCode = CommonErrorCode.FORBIDDEN_API;
                        handleLogging(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                null,
                                String.valueOf(errorCode.getHttpStatus().value()));
                        throw new AlgoticException(errorCode);
                    }
                } else {
                    String userId = customer.getId();
                    if (loginRequest.getUsername().equalsIgnoreCase(insdusfaceTestEmail)) {
                        Users getUserRole = usersRepo.findByID(userId);
                        String token = jwtTokenUtil.generateToken(userId, getUserRole.getRole());

                        Date expireIn = jwtTokenUtil.getExpirationDateFromToken(token);
                        saveToken(userId, token, expireIn);
                        loginResponse.setToken(token);
                    } else {
                        String phoneNo = customer.getPhoneNumber();
                        if (LoginType.BROKER.name().equalsIgnoreCase(type)) {
                            loginResponse = handleBrokerLogin(userId);
                        } else if (LoginType.ALGOTIC.name().equalsIgnoreCase(type)) {
                            loginResponse = handleAlgoticLogin(userId, loginRequest.getUsername(), phoneNo);
                        } else {
                            CommonErrorCode errorCode = CommonErrorCode.TYPE_NOT_EXISTS;
                            handleLogging(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    null,
                                    String.valueOf(errorCode.getHttpStatus().value()));
                            throw new AlgoticException(errorCode);
                        }
                    }
                    handleLogging(
                            "login",
                            AlgoticUtils.objectToJsonString(loginResponse),
                            null,
                            String.valueOf(HttpStatus.OK.value()));
                    return new ResponseEntity<>(loginResponse, HttpStatus.OK);
                }
            } else {
                CommonErrorCode errorCode = CommonErrorCode.USERNAME_NOT_VALID;
                handleLogging(
                        errorCode.getErrorCode(),
                        errorCode.getErrorMessage(),
                        null,
                        String.valueOf(errorCode.getHttpStatus().value()));
                throw new AlgoticException(errorCode);
            }

        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            handleLogging(
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    null,
                    String.valueOf(errorCode.getHttpStatus().value()));
            throw new AlgoticException(errorCode);
        }
    }

    private void validateUserDetails(Users userDetails) {
        try {
            if (userDetails != null && userDetails.getStatus().equalsIgnoreCase("Blocked")) {
                CommonErrorCode errorCode = CommonErrorCode.BLOCKED_STATUS_CHECK;
                handleLogging(
                        errorCode.getErrorCode(),
                        errorCode.getErrorMessage(),
                        userDetails.getId(),
                        String.valueOf(errorCode.getHttpStatus().value()));
                throw new AlgoticException(errorCode);
            }
            if (userDetails != null && userDetails.getStatus().equalsIgnoreCase("INACTIVE")) {
                CommonErrorCode errorCode = CommonErrorCode.INACTIVE_STATUS_CHECK;
                handleLogging(
                        errorCode.getErrorCode(),
                        errorCode.getErrorMessage(),
                        userDetails.getId(),
                        String.valueOf(errorCode.getHttpStatus().value()));
                throw new AlgoticException(errorCode);
            }
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            handleLogging(
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    null,
                    String.valueOf(errorCode.getHttpStatus().value()));
            throw new AlgoticException(errorCode);
        }
    }

    private void saveToken(String userId, String token, Date expireIn) {
        try {
            AccessTokens accessTokens = new AccessTokens();
            accessTokens.setUserId(userId);
            accessTokens.setToken(token);
            accessTokens.setExpireIn(expireIn);
            accessTokens.setIsActive(true);
            accessTokens.setCreatedAt(new Date());
            handleLogging("access token data", AlgoticUtils.objectToJsonString(accessTokens), null, null);
            accessTokensRepo.save(accessTokens);
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            handleLogging(
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    null,
                    String.valueOf(errorCode.getHttpStatus().value()));
            throw new AlgoticException(errorCode);
        }
    }

    private void handleLogging(String infoMessage, String infoContext, String userId, String httpCode) {
        log.info(logConfig.getLogHandler().getInfoLog(infoMessage, infoContext, userId, httpCode));
    }

    private LoginResponse handleAlgoticLogin(String userId, String email, String phone) {
        handleLogging("Handle algotic login", "Handle the login for algotic", null, null);
        LoginResponse loginResponse = new LoginResponse();
        String otp = otpService.generateAndSendOtp(email, phone);
        String refCode = otpService.randomRefCode();
        otpService.saveOtp(otp, refCode, 300, userId, false, "Login", null);
        loginResponse.setRefCode(refCode);

        handleLogging("Login response", AlgoticUtils.objectToJsonString(loginResponse), null, null);
        return loginResponse;
    }

    private LoginResponse handleBrokerLogin(String userId) {
        BrokerCustomerDetails brokerCustomerDetails = brokerCustomerDetailsRepo.findBrokerUserById(userId);
        handleLogging("broker detail", AlgoticUtils.objectToJsonString(brokerCustomerDetails), null, null);
        if (brokerCustomerDetails == null) {
            CommonErrorCode errorCode = CommonErrorCode.BROKER_CUSTOMER_NOT_LINKED;
            handleLogging(
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    userId,
                    String.valueOf(errorCode.getHttpStatus().value()));
            throw new AlgoticException(errorCode);
        }
        LoginResponse loginResponse = new LoginResponse();
        Users getUserRole = usersRepo.findByID(userId);
        String token = jwtTokenUtil.generateToken(userId, getUserRole.getRole());
        Date expireIn = jwtTokenUtil.getExpirationDateFromToken(token);
        saveToken(userId, token, expireIn);
        loginResponse.setToken(token);
        return loginResponse;
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> logout() {
        String userId = jwtHelper.getUserId();
        try {
            List<AccessTokens> existToken = accessTokensRepo.findByUserId(userId);
            handleLogging("Token check", AlgoticUtils.objectToJsonString(existToken), null, null);
            if (existToken == null) {
                CommonErrorCode errorCode = CommonErrorCode.TOKEN_NOT_FOUND;
                handleLogging(
                        errorCode.getErrorCode(),
                        errorCode.getErrorMessage(),
                        userId,
                        String.valueOf(errorCode.getHttpStatus().value()));
                throw new AlgoticException(errorCode);
            }
            for (AccessTokens accessTokens : existToken) {
                accessTokens.setIsActive(false);
                accessTokensRepo.save(accessTokens);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Logout Done ",
                            "Customer logout Successfully",
                            userId,
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(new GlobalMessageResponse("Customer Logout successfully"), HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            handleLogging(
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    userId,
                    String.valueOf(errorCode.getHttpStatus().value()));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<VerifyResponse> termsAccepted() {
        try {

            VerifyResponse verifyResponse = new VerifyResponse();

            String userId = jwtHelper.getUserId();
            Users customer = usersRepo.findByIdAndIsDeleted(userId, false);
            if (customer != null) {
                customer.setTermsAccepted(true);
                usersRepo.save(customer);
                verifyResponse.setIsTermsAccepted(true);
            } else {
                verifyResponse.setIsTermsAccepted(false);
            }

            return new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            handleLogging(
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    null,
                    String.valueOf(errorCode.getHttpStatus().value()));
            throw new AlgoticException(errorCode);
        }
    }
}
