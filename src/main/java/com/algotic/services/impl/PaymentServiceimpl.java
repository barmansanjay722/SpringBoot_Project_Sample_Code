package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.AlgoticStatus;
import com.algotic.data.entities.SubscriptionTransactions;
import com.algotic.data.repositories.SubscriptionTransactionsRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.response.PaymentLinkResponse;
import com.algotic.model.response.PaymentStatusResponse;
import com.algotic.services.PaymentService;
import com.algotic.utils.AlgoticUtils;
import jakarta.transaction.Transactional;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Transactional
public class PaymentServiceimpl implements PaymentService {
    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private SubscriptionTransactionsRepo subscriptionTransactionsRepo;

    @Autowired
    private JwtHelper jwtHelper;

    @Value("${instamojoUrl}")
    private String instamojoUrl;

    @Override
    public ResponseEntity<PaymentLinkResponse> paymentLink() {
        String user = jwtHelper.getUserId();
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Payment Link", "Payment link to make Payment", jwtHelper.getUserId(), null));
        try {
            SubscriptionTransactions transactions = subscriptionTransactionsRepo.findByStatus(jwtHelper.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Transactions",
                            AlgoticUtils.objectToJsonString(transactions),
                            jwtHelper.getUserId(),
                            null));
            PaymentLinkResponse paymentLinkResponse = new PaymentLinkResponse();
            SubscriptionTransactions subscriptionTransactions = new SubscriptionTransactions();
            if (transactions == null) {

                subscriptionTransactions.setUserId(user);
                subscriptionTransactions.setStatus(AlgoticStatus.CREATED.name());
                subscriptionTransactions.setCreatedAt(new Date());
                subscriptionTransactionsRepo.save(subscriptionTransactions);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Save Subscription Purchased",
                                AlgoticUtils.objectToJsonString(subscriptionTransactions),
                                jwtHelper.getUserId(),
                                null));

                SubscriptionTransactions subscriptionTransactionId =
                        subscriptionTransactionsRepo.paymentSubscriptionTransactionId(user);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Subscription Transaction Id",
                                AlgoticUtils.objectToJsonString(subscriptionTransactionId),
                                jwtHelper.getUserId(),
                                null));
                paymentLinkResponse.setInstamojoLink(instamojoUrl);
                paymentLinkResponse.setSubscriptionTransactionId(subscriptionTransactionId.getId());
            } else {
                paymentLinkResponse.setInstamojoLink(instamojoUrl);
                paymentLinkResponse.setSubscriptionTransactionId(transactions.getId());
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Save Subscription Transaction",
                            AlgoticUtils.objectToJsonString(paymentLinkResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.CREATED.value())));
            return new ResponseEntity<>(paymentLinkResponse, HttpStatus.OK);

        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<PaymentStatusResponse> paymentStatus() {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Payment Status", "Get Payment status ", jwtHelper.getUserId(), null));
            PaymentStatusResponse statusResponse = new PaymentStatusResponse();
            SubscriptionTransactions transactions = subscriptionTransactionsRepo.findByStatus(jwtHelper.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Transactions",
                            AlgoticUtils.objectToJsonString(transactions),
                            jwtHelper.getUserId(),
                            null));
            if (transactions != null) {
                statusResponse.setStatus(transactions.getStatus());
                statusResponse.setId(transactions.getId());
            } else {
                BusinessErrorCode errorCode = BusinessErrorCode.STATUS_NOT_EXISTS;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                null,
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "get payment status",
                            AlgoticUtils.objectToJsonString(statusResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(statusResponse, HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }
}
