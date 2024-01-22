package com.algotic.services;

import com.algotic.model.request.ContactUsRequest;
import com.algotic.model.response.GlobalMessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface ContactUsDetailsService {
    ResponseEntity<GlobalMessageResponse> saveContactUsDetails(ContactUsRequest contactUsRequest);
}
