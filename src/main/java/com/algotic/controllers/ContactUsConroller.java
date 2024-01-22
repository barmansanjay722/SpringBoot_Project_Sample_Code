package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.ContactUsRequest;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.services.ContactUsDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class ContactUsConroller {

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private ContactUsDetailsService contactUsDetailsService;

    @PostMapping("/contactUs")
    public ResponseEntity<GlobalMessageResponse> contactDetailsUs(
            @RequestBody ContactUsRequest contactUsRequest, BindingResult errors) {

        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().get(0).getDefaultMessage());
            throw exception;
        }

        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Save contactUs details", "Saving all the contact information", null, null));

        return contactUsDetailsService.saveContactUsDetails(contactUsRequest);
    }
}
