package com.algotic.services;

import com.algotic.model.request.SubscriptionTransactionRequest;
import com.algotic.model.response.*;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface SubscriptionService {
    ResponseEntity<List<SubscriptionResponse>> getSubscription(Integer limit, Integer offset);

    ResponseEntity<SubscriptionResponse> getSubscriptionDetails(int id);

    ResponseEntity<GlobalMessageResponse> saveSubscriptionTransaction(
            SubscriptionTransactionRequest subscriptionTransactionRequest);

    ResponseEntity<List<SubscriptionPurchasedResponse>> getSubscriptionPurchased(
            String id, Integer limit, Integer offset);

    ResponseEntity<SubscriptionRenewalResponse> subscriptionRenewal(Integer limit, Integer offset);
}
