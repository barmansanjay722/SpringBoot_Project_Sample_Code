package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.StageType;
import com.algotic.data.entities.*;
import com.algotic.data.repositories.SubscriptionRenewalRepo;
import com.algotic.data.repositories.SubscriptionTransactionsRepo;
import com.algotic.data.repositories.SubscriptionsRepo;
import com.algotic.data.repositories.UsersRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.SubscriptionTransactionRequest;
import com.algotic.model.response.*;
import com.algotic.services.SubscriptionService;
import com.algotic.utils.AlgoticUtils;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    @Autowired
    private SubscriptionsRepo subscriptionsRepo;

    @Autowired
    private SubscriptionRenewalRepo subscriptionRenewalRepo;

    @Autowired
    private SubscriptionTransactionsRepo subscriptionTransactionsRepo;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private JwtHelper jwtHelper;

    @Transactional
    @Override
    public ResponseEntity<List<SubscriptionResponse>> getSubscription(Integer limit, Integer offset) {

        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Get Subscription List", "Get Subscription List", jwtHelper.getUserId(), null));

            int limitValue = 0;
            int offsetValue = 0;

            if (limit == 0 && offset == 0) {
                limitValue = 10;
                offsetValue = 0;
            } else if (limit < 0 || offset < 0) {
                CommonErrorCode errorCode = CommonErrorCode.LIMIT;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                limitValue = limit;
                offsetValue = offset;
            }
            List<SubscriptionResponse> subscriptionResponseList = new ArrayList<>();
            List<Subscriptions> subscriptionsList = subscriptionsRepo.findAllSubscription(limitValue, offsetValue);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Subscription List",
                            AlgoticUtils.objectToJsonString(subscriptionsList),
                            jwtHelper.getUserId(),
                            null));
            if (!subscriptionsList.isEmpty()) {
                subscriptionsList.forEach(subscriptions -> {
                    SubscriptionResponse subscriptionResponse = new SubscriptionResponse();
                    subscriptionResponse.setName(subscriptions.getName());
                    subscriptionResponse.setId(subscriptions.getId());
                    subscriptionResponse.setDuration(subscriptions.getDuration());
                    subscriptionResponse.setPrice(subscriptions.getPrice());
                    subscriptionResponseList.add(subscriptionResponse);
                });
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "get subscription list",
                            AlgoticUtils.objectToJsonString(subscriptionResponseList),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(subscriptionResponseList, HttpStatus.OK);
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
    public ResponseEntity<SubscriptionResponse> getSubscriptionDetails(int id) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Subscription Details", "Subscription Details", jwtHelper.getUserId(), null));
            SubscriptionResponse subscriptionResponse = new SubscriptionResponse();
            Subscriptions subscriptionDetails = subscriptionsRepo.findByIdAndIsActive(id, true);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Subscription Details",
                            AlgoticUtils.objectToJsonString(subscriptionDetails),
                            jwtHelper.getUserId(),
                            null));
            if (subscriptionDetails != null) {
                subscriptionResponse.setName(subscriptionDetails.getName());
                subscriptionResponse.setId(subscriptionDetails.getId());
                subscriptionResponse.setPrice(subscriptionDetails.getPrice());
                subscriptionResponse.setDuration(subscriptionDetails.getDuration());
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "get subscriber details",
                                AlgoticUtils.objectToJsonString(subscriptionResponse),
                                jwtHelper.getUserId(),
                                String.valueOf(HttpStatus.OK.value())));
                return new ResponseEntity<>(subscriptionResponse, HttpStatus.OK);
            }

            BusinessErrorCode errorCode = BusinessErrorCode.ID_NOT_EXISTS;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
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
    public ResponseEntity<GlobalMessageResponse> saveSubscriptionTransaction(
            SubscriptionTransactionRequest subscriptionTransactionRequest) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Save Subscription Transactions Details",
                        "Save Subscription Transactions Details",
                        jwtHelper.getUserId(),
                        null));
        GlobalMessageResponse subscriptionTransactionResponse = new GlobalMessageResponse();
        try {
            Users customer = usersRepo.findByID(jwtHelper.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("user by Id", AlgoticUtils.objectToJsonString(customer), jwtHelper.getUserId(), null));
            if (customer == null) {
                CommonErrorCode errorCode = CommonErrorCode.CUSTOMER_NOT_EXISTS;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            SubscriptionTransactions subscriptionTransactions = new SubscriptionTransactions();
            subscriptionTransactions.setPaymentId(subscriptionTransactionRequest.getPaymentId());
            subscriptionTransactions.setTransactionId(subscriptionTransactionRequest.getTransactionId());
            subscriptionTransactions.setUserId(jwtHelper.getUserId());
            subscriptionTransactions.setSubscriptionId(subscriptionTransactionRequest.getSubscriptionId());
            subscriptionTransactions.setStatus(subscriptionTransactionRequest.getStatus());
            subscriptionTransactions.setAmount(subscriptionTransactionRequest.getAmount());
            subscriptionTransactions.setBrokerId(subscriptionTransactionRequest.getBrokerId());
            subscriptionTransactions.setCreatedAt(new Date());
            subscriptionTransactionsRepo.save(subscriptionTransactions);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Save data",
                            AlgoticUtils.objectToJsonString(subscriptionTransactions),
                            jwtHelper.getUserId(),
                            null));
            customer.setStage(StageType.SUBSCRIBE.name());
            customer.setModifiedAt(new Date());
            usersRepo.save(customer);
            generatePdf(subscriptionTransactions);
            subscriptionTransactionResponse.setMessage("Data Save Successfully");
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Save Subscription Transaction",
                            AlgoticUtils.objectToJsonString(subscriptionTransactionResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.CREATED.value())));
            return new ResponseEntity<>(subscriptionTransactionResponse, HttpStatus.CREATED);
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

    public CompletableFuture<Void> generatePdf(SubscriptionTransactions subscriptionTransactions)
            throws InterruptedException {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Pdf generated", "Request for pdf", jwtHelper.getUserId(), null));
            userService
                    .pdfCreate(subscriptionTransactions.getUserId(), subscriptionTransactions)
                    .getBody();
            Thread.sleep(1000L);
            log.info(logConfig.getLogHandler().getInfoLog("user by Id", "Pdf generated", jwtHelper.getUserId(), null));
            return null;
        } catch (AlgoticException e) {
            throw new AlgoticException(e.getErrorCode());
        }
    }

    @Override
    public ResponseEntity<List<SubscriptionPurchasedResponse>> getSubscriptionPurchased(
            String id, Integer limit, Integer offset) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Subscription Purchased", "Get Subscription Purchased", jwtHelper.getUserId(), null));
            int limitValue = 0;
            int offsetValue = 0;

            if (limit == 0 && offset == 0) {
                limitValue = 10;
                offsetValue = 0;
            } else if (limit < 0 || offset < 0) {
                CommonErrorCode errorCode = CommonErrorCode.LIMIT;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                limitValue = limit;
                offsetValue = offset;
            }
            List<SubscriptionPurchasedResponse> subscriptionPurchasedResponseList = new ArrayList<>();
            List<SubscriptionTransactions> subscriptionTransactionsList =
                    subscriptionTransactionsRepo.findByUserID(id, limitValue, offsetValue);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Subscription Purchased List",
                            AlgoticUtils.objectToJsonString(subscriptionTransactionsList),
                            jwtHelper.getUserId(),
                            null));
            if (!subscriptionTransactionsList.isEmpty()) {
                subscriptionTransactionsList.forEach(subscriptionTransactions -> {
                    SubscriptionPurchasedResponse subscriptionPurchasedResponse = new SubscriptionPurchasedResponse();
                    subscriptionPurchasedResponse.setId(subscriptionTransactions.getId());
                    subscriptionPurchasedResponse.setTransactionId(subscriptionTransactions.getTransactionId());
                    subscriptionPurchasedResponse.setUserId(subscriptionTransactions.getUserId());
                    subscriptionPurchasedResponse.setStatus(subscriptionTransactions.getStatus());
                    subscriptionPurchasedResponse.setAmount(subscriptionTransactions.getAmount());
                    subscriptionPurchasedResponseList.add(subscriptionPurchasedResponse);
                });
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "get subscription list",
                                AlgoticUtils.objectToJsonString(subscriptionPurchasedResponseList),
                                jwtHelper.getUserId(),
                                String.valueOf(HttpStatus.OK.value())));
                return new ResponseEntity<>(subscriptionPurchasedResponseList, HttpStatus.OK);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
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
    @Transactional
    public ResponseEntity<SubscriptionRenewalResponse> subscriptionRenewal(Integer limit, Integer offset) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Subscription renewal",
                            "Details of a particular subscriber for the renewal",
                            jwtHelper.getUserId(),
                            null));
            int limitValue = limit;
            int offsetValue = offset;
            if (limit == 0 && offset == 0) {
                limitValue = 5;
            } else if (limit < 0 || offset < 0) {
                CommonErrorCode errorCode = CommonErrorCode.LIMIT;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            List<SubscriptionRenewalDatas> subscriptionRenewal =
                    subscriptionRenewalRepo.subscriptionRenewal(limitValue, offsetValue);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Subscription renewal",
                            AlgoticUtils.objectToJsonString(subscriptionRenewal),
                            jwtHelper.getUserId(),
                            null));
            Object[] renewalCount = subscriptionRenewalRepo.subscriptionrenewalCount();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Subscription renewal",
                            AlgoticUtils.objectToJsonString(renewalCount),
                            jwtHelper.getUserId(),
                            null));
            SubscriptionRenewalResponse response = new SubscriptionRenewalResponse();
            response.setTotal(Integer.parseInt(renewalCount[0].toString()));
            response.setResult(subscriptionRenewal);

            if (subscriptionRenewal.isEmpty()) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Renewal details of user",
                            AlgoticUtils.objectToJsonString(response),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
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
