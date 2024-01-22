package com.algotic.services;

import com.algotic.model.response.PaymentLinkResponse;
import com.algotic.model.response.PaymentStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {
    ResponseEntity<PaymentLinkResponse> paymentLink();

    ResponseEntity<PaymentStatusResponse> paymentStatus();
}
