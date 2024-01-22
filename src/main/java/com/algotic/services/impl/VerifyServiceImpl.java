package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.constants.AlgoticStatus;
import com.algotic.data.entities.AccessTokens;
import com.algotic.data.entities.Otps;
import com.algotic.data.entities.Users;
import com.algotic.data.repositories.AccessTokensRepo;
import com.algotic.data.repositories.OtpsRepo;
import com.algotic.data.repositories.UsersRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.VerifyRequest;
import com.algotic.model.response.VerifyResponse;
import com.algotic.services.VerifyService;
import com.algotic.utils.AlgoticUtils;
import com.algotic.utils.JwtTokenUtil;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VerifyServiceImpl implements VerifyService {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private OtpsRepo otpsRepo;

    @Autowired
    private AccessTokensRepo accessTokensRepo;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private UsersRepo usersRepo;

    @Override
    public ResponseEntity<VerifyResponse> verifyOtpAndGetToken(VerifyRequest verifyRequest) {
        VerifyResponse verifyResponse = new VerifyResponse();
        try {
            log.info(logConfig.getLogHandler().getInfoLog("Verify otp", "Get Otp Verify", null, null));

            Otps otp = otpsRepo.findOtpByRefCodeWithoutOtp(verifyRequest.getReferenceCode());
            log.info(logConfig.getLogHandler().getInfoLog("Get Otp", AlgoticUtils.objectToJsonString(otp), null, null));
            if (!otp.getOtp().equalsIgnoreCase(verifyRequest.getOtp())) {
                otp.setAttempts(otp.getAttempts() + 1);
                if (otp.getAttempts() > 5) {
                    CommonErrorCode errorCode = CommonErrorCode.RESEND_OTP;
                    log.error(logConfig
                            .getLogHandler()
                            .getErrorLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    null,
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                }
                otpsRepo.save(otp);
                CommonErrorCode errorCode = CommonErrorCode.INVALID;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                null,
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                if (otp.getIsVerified().equals(true)) {
                    CommonErrorCode errorCode = CommonErrorCode.INVALID;
                    log.error(logConfig
                            .getLogHandler()
                            .getErrorLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    null,
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                }
                String userId = otp.getUserId();
                Users getUserRole = usersRepo.findByID(userId);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog("Get USer Role", AlgoticUtils.objectToJsonString(getUserRole), null, null));
                String token = jwtTokenUtil.generateToken(userId, getUserRole.getRole());
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog("Get Token", AlgoticUtils.objectToJsonString(token), null, null));

                Date expireIn = jwtTokenUtil.getExpirationDateFromToken(token);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog("Expire In", AlgoticUtils.objectToJsonString(expireIn), null, null));

                otp.setIsVerified(true);
                otp.setVerifiedAt(new Date());

                Otps saveOtp = otpsRepo.save(otp);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog("Save otp in database", AlgoticUtils.objectToJsonString(saveOtp), null, null));
                Boolean istermsAccepted = false;
                if (saveOtp.getIsVerified().equals(true)) {

                    Users customer = usersRepo.findByIdAndIsDeleted(userId, false);
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog("Customer", AlgoticUtils.objectToJsonString(customer), null, null));

                    if (customer == null) {
                        CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                        log.error(logConfig
                                .getLogHandler()
                                .getErrorLog(
                                        errorCode.getErrorCode(),
                                        errorCode.getErrorMessage(),
                                        null,
                                        String.valueOf(errorCode.getHttpStatus().value())));
                        throw new AlgoticException(errorCode);
                    }
                    istermsAccepted = customer.getTermsAccepted();
                    customer.setStatus(AlgoticStatus.ACTIVE.name());
                    usersRepo.save(customer);
                }

                List<AccessTokens> existToken = accessTokensRepo.findByUserId(userId);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog("Token Check", AlgoticUtils.objectToJsonString(existToken), null, null));

                if (existToken == null) {
                    CommonErrorCode errorCode = CommonErrorCode.TOKEN_NOT_FOUND;
                    log.error(logConfig
                            .getLogHandler()
                            .getErrorLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    null,
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                }
                for (AccessTokens accessTokens : existToken) {
                    accessTokens.setIsActive(false);
                    accessTokensRepo.save(accessTokens);
                }

                AccessTokens accessTokens = new AccessTokens();
                accessTokens.setUserId(userId);
                accessTokens.setToken(token);
                accessTokens.setExpireIn(expireIn);
                accessTokens.setIsActive(true);
                accessTokens.setCreatedAt(new Date());
                accessTokensRepo.save(accessTokens);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog("Access token save", AlgoticUtils.objectToJsonString(accessTokens), null, null));

                verifyResponse.setToken(token);
                verifyResponse.setIsTermsAccepted(istermsAccepted);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Verify otp",
                                AlgoticUtils.objectToJsonString(verifyResponse),
                                null,
                                String.valueOf(HttpStatus.OK.value())));
            }

            return new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            null,
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }
}
