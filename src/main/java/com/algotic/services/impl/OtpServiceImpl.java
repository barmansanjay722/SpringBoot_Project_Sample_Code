package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.data.entities.Otps;
import com.algotic.data.entities.Users;
import com.algotic.data.repositories.OtpsRepo;
import com.algotic.data.repositories.UsersRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.ResendOtpRequest;
import com.algotic.model.response.OtpResponse;
import com.algotic.services.EmailService;
import com.algotic.services.MobileService;
import com.algotic.services.OtpService;
import com.algotic.utils.AlgoticUtils;
import java.util.Date;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@ComponentScan
@Slf4j
public class OtpServiceImpl implements OtpService {
    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpsRepo otpsRepo;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private MobileService mobileService;

    @Value("${mobileOtpEnable}")
    private String mobileOtpEnable;

    @Override
    public String generateAndSendOtp(String email, String phone) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Generate And send Otp", "Generate And send Otp to mail", null, null));
        int number = new Random().nextInt(100000, 999999);
        emailService.sendOTPEmail(number, email);
        if (mobileOtpEnable.equalsIgnoreCase("true")) {
            mobileService.sendOTPMobile(number, phone);
        }
        log.info(logConfig.getLogHandler().getInfoLog("Otp ", AlgoticUtils.objectToJsonString(number), null, null));
        return Integer.toString(number);
    }

    @Override
    public String randomRefCode() {
        log.info(logConfig.getLogHandler().getInfoLog(" Generate Random number", "Generate Number", null, null));
        String rand1 = RandomStringUtils.random(3, true, false).toLowerCase();
        String rand2 = RandomStringUtils.random(3, true, false).toLowerCase();
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(" Result", AlgoticUtils.objectToJsonString(rand1 + "-" + rand2), null, null));
        return rand1 + "-" + rand2;
    }

    @Override
    public void saveOtp(
            String setOtpValue,
            String setReferenceCodeValue,
            int setExpiresInValue,
            String setUserIdValue,
            Boolean setIsVerifiedValue,
            String setOtpTypeValue,
            Date verifiedAt) {
        try {
            log.info(logConfig.getLogHandler().getInfoLog("save otp", "save otp", null, null));
            Otps otps = new Otps();
            otps.setOtp(setOtpValue);
            otps.setReferenceCode(setReferenceCodeValue);
            otps.setExpiresIn(setExpiresInValue);
            otps.setUserId(setUserIdValue);
            otps.setIsVerified(setIsVerifiedValue);
            otps.setOtpType(setOtpTypeValue);
            otps.setVerifiedAt(verifiedAt);
            otps.setCreatedAt(new Date());
            otps.setAttempts(0);
            Otps otps1 = otpsRepo.save(otps);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Save otp", AlgoticUtils.objectToJsonString(otps1), null, null));
        } catch (Exception e) {
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

    @Override
    public ResponseEntity<OtpResponse> resendOtp(ResendOtpRequest resendOtpRequest) {
        OtpResponse otpResponse = new OtpResponse();
        try {
            log.info(logConfig.getLogHandler().getInfoLog("Resend  otp", "Resend the Otp", null, null));

            Otps otp = otpsRepo.findByRefCode(resendOtpRequest.getReferenceCode());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Resend  otp", AlgoticUtils.objectToJsonString(otp), null, null));
            if (otp != null) {
                String userId = otp.getUserId();
                Users customer = usersRepo.findByID(userId);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog("Customer By ID", AlgoticUtils.objectToJsonString(customer), null, null));
                if (customer != null) {
                    String email = customer.getEmail();
                    String phoneNo = customer.getPhoneNumber();
                    String generateOtp = generateAndSendOtp(email, phoneNo);
                    String refCode = randomRefCode();
                    otp.setIsVerified(false);
                    otpsRepo.save(otp);
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog("Save Otp", AlgoticUtils.objectToJsonString(otp), null, null));
                    Otps otpData = new Otps();
                    otpData.setOtp(generateOtp);
                    otpData.setReferenceCode(refCode);
                    otpData.setExpiresIn(300);
                    otpData.setUserId(userId);
                    otpData.setIsVerified(false);
                    otpData.setOtpType("Registers");
                    otpData.setAttempts(0);
                    otpData.setVerifiedAt(null);
                    otpData.setCreatedAt(new Date());
                    otpsRepo.save(otpData);
                    otpResponse.setRefCode(refCode);
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    "Resend Otp",
                                    AlgoticUtils.objectToJsonString(otpResponse),
                                    null,
                                    String.valueOf(HttpStatus.OK.value())));
                    return new ResponseEntity<>(otpResponse, HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (AlgoticException ex) {

            throw ex;
        } catch (Exception e) {
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            e.getMessage(),
                            "Error while sending the new otp and receiving new reference code ",
                            null,
                            CommonErrorCode.INTERNAL_SERVER_ERROR.getErrorCode()));
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
