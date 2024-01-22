package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.data.entities.SubscriptionTransactions;
import com.algotic.data.entities.Users;
import com.algotic.data.repositories.SubscriptionTransactionsRepo;
import com.algotic.data.repositories.UsersRepo;
import com.algotic.data.repositories.WebhooksRepo;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.response.WebhookPaymentResponse;
import com.algotic.services.WebhookAliceBlue;
import com.algotic.utils.AlgoticUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebhookAliceBlueImpl implements WebhookAliceBlue {

    @Autowired
    private WebhooksRepo webhooksRepo;

    @Autowired
    private SubscriptionTransactionsRepo subscriptionTransactionsRepo;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private JwtHelper jwtHelper;

    @Override
    public ResponseEntity saveWebhook(Object webhookRequest) {

        try {
            log.info(logConfig.getLogHandler().getInfoLog("Save Webhook", "Save webhook for instamojo", null, null));

            WebhookPaymentResponse paymentlist =
                    new ObjectMapper().convertValue(webhookRequest, WebhookPaymentResponse.class);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "PaymentList", AlgoticUtils.objectToJsonString(paymentlist), jwtHelper.getUserId(), null));

            if (paymentlist != null) {

                Users buyerEmail =
                        usersRepo.findForStatus(paymentlist.getBuyer().getEmail());
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "BuyerEmail",
                                AlgoticUtils.objectToJsonString(buyerEmail),
                                jwtHelper.getUserId(),
                                null));

                if (buyerEmail != null) {
                    SubscriptionTransactions subscriptionTransactions = new SubscriptionTransactions();
                    subscriptionTransactions.setUserId(buyerEmail.getId());
                    subscriptionTransactions.setPaymentId(
                            paymentlist.getPayment().getPayment_id());
                    subscriptionTransactions.setStatus(paymentlist.getPayment().getStatus());
                    subscriptionTransactions.setAmount(paymentlist.getPayment().getAmount());
                    subscriptionTransactions.setCurrency(
                            paymentlist.getPayment().getCurrency());
                    subscriptionTransactions.setPaymentCompletedAt(
                            paymentlist.getPayment().getCompleted_at());
                    subscriptionTransactions.setResourceUri(
                            paymentlist.getPayment().getResource_uri());
                    subscriptionTransactions.setBuyerName(paymentlist.getBuyer().getName());
                    subscriptionTransactions.setBuyerEmail(
                            paymentlist.getBuyer().getEmail());
                    subscriptionTransactions.setMessageAuthenticationCode(paymentlist.getMessage_authentication_code());
                    subscriptionTransactions.setCreatedAt(new Date());
                    subscriptionTransactionsRepo.save(subscriptionTransactions);

                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    "Verify otp",
                                    AlgoticUtils.objectToJsonString(subscriptionTransactions),
                                    null,
                                    String.valueOf(HttpStatus.OK.value())));
                }
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            null,
                            String.valueOf(errorCode.getHttpStatus().value())));
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}
