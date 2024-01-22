package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.constants.AlgoticMessages;
import com.algotic.data.entities.ContactUs;
import com.algotic.data.repositories.ContactUsDetailsRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.ContactUsRequest;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.services.ContactUsDetailsService;
import com.algotic.services.EmailService;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ContactUsDetailsServiceImpl implements ContactUsDetailsService {

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private ContactUsDetailsRepo contactUsDetailsRepo;

    @Autowired
    private EmailService emailService;

    @Override
    public ResponseEntity<GlobalMessageResponse> saveContactUsDetails(ContactUsRequest contactUsRequest) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Save contactUs details", "Saving the message and subject", null, "200"));

            if (contactUsRequest.getEmail().isEmpty()
                    || contactUsRequest.getPhone().isEmpty()
                    || contactUsRequest.getName().isEmpty()) {
                throw new AlgoticException(CommonErrorCode.INVALID_CONTACTUS);
            }

            ContactUs contactUs = new ContactUs();
            contactUs.setName(contactUsRequest.getName());
            contactUs.setEmail(contactUsRequest.getEmail());
            contactUs.setPhone(contactUsRequest.getPhone());
            contactUs.setMessage(contactUsRequest.getMessage());
            contactUs.setCreatedAt(new Date());
            contactUsDetailsRepo.save(contactUs);
            emailService.sendContactUsEmail(
                    contactUsRequest.getName(),
                    contactUsRequest.getEmail(),
                    contactUsRequest.getPhone(),
                    contactUsRequest.getMessage());
            return new ResponseEntity<>(
                    new GlobalMessageResponse(AlgoticMessages.SAVED_SUCCESSFULLY), HttpStatus.CREATED);
        } catch (AlgoticException e) {
            throw new AlgoticException(e.getErrorCode());
        } catch (Exception e) {
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog("error in message and subject", "Error while saving details", null, "404"));

            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
