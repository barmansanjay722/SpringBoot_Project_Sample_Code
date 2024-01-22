package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.services.EmailService;
import com.algotic.utils.AlgoticUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Transactional
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private JwtHelper jwtHelper;

    @Value(("${spring.mail.username}"))
    private String seviceEmailAddress;

    @Value(("${contactUsBccEmail}"))
    private String contactUsBccEmail;

    @Override
    public void sendOTPEmail(int otp, String email) throws AlgoticException {
        try {
            log.info(logConfig.getLogHandler().getInfoLog("Send Otp", "Send Otp to mail", null, null));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("Verification Otp");
            message.setFrom(seviceEmailAddress);
            message.setTo(email);
            message.setText("Here's your One Time Password (OTP) " + otp + " don't share it ");
            javaMailSender.send(message);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Mail will be send on your email", AlgoticUtils.objectToJsonString(email), null, null));
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

    @Override
    public void sendVerificationEmail(String email, String text) {

        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            " error in sending mail message will be send",
                            "Subject and message will be send to a customer email address ",
                            null,
                            null));
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("Email Verification");
            message.setFrom(seviceEmailAddress);
            message.setTo(email);
            message.setText("Your verification url is this " + " " + text);
            javaMailSender.send(message);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            " verification url send on updated email",
                            AlgoticUtils.objectToJsonString(message),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
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

    @Override
    public void sendContactUsEmail(String name, String email, String phone, String message) {
        try {

            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setSubject("Enquiry Details for Contact Us");
            simpleMailMessage.setFrom(seviceEmailAddress);
            simpleMailMessage.setTo(seviceEmailAddress);
            simpleMailMessage.setBcc(contactUsBccEmail);
            simpleMailMessage.setText("Enquery email is " + email + "\n" + "Enquery name is " + name + "\n"
                    + "Enquery phone number is " + phone + "\n" + "Enquery message is" + message);
            javaMailSender.send(simpleMailMessage);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "contact us email send successfully",
                            AlgoticUtils.objectToJsonString(message),
                            null,
                            String.valueOf(HttpStatus.OK.value())));
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
