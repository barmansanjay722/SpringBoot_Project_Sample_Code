package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.StageType;
import com.algotic.data.entities.BrokerCustomerDetails;
import com.algotic.data.entities.BrokerSessionDetails;
import com.algotic.data.entities.Brokers;
import com.algotic.data.entities.Users;
import com.algotic.data.repositories.BrokerCustomerDetailsRepo;
import com.algotic.data.repositories.BrokerSessionDetailsRepo;
import com.algotic.data.repositories.BrokersRepo;
import com.algotic.data.repositories.UsersRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.response.BrokerResponse;
import com.algotic.utils.AlgoticUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
@Primary
public class CommonBrokerMgmtServiceImpl {

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private BrokersRepo brokersRepo;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private BrokerSessionDetailsRepo brokerSessionDetailsRepo;

    @Autowired
    private BrokerCustomerDetailsRepo brokerCustomerDetailsRepo;

    /**
     * @param limit
     * @param offset
     * @return
     */
    public ResponseEntity<List<BrokerResponse>> getBrokers(int limit, int offset) {
        String userId = jwtHelper.getUserId();
        try {
            int limitValue = limit;
            int offsetValue = offset;
            if (limit == 0 && offset == 0) {
                limitValue = 10;
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
            List<BrokerResponse> brokerResponseList;
            List<Brokers> brokersList = brokersRepo.findAllBrokers(limitValue, offsetValue);

            if (brokersList.isEmpty()) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_NOT_EXISTS;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            brokerResponseList = brokerResponses(brokersList);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Broker details ",
                            AlgoticUtils.objectToJsonString(brokerResponseList),
                            userId,
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(brokerResponseList, HttpStatus.OK);
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

    private List<BrokerResponse> brokerResponses(List<Brokers> brokersList) {
        List<BrokerResponse> brokerResponseList = new ArrayList<>();
        brokersList.forEach(brokers -> {
            BrokerResponse brokerResponse = new BrokerResponse();
            Optional<Brokers> brokerOptional = brokersRepo.findById(brokers.getId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Brokers", AlgoticUtils.objectToJsonString(brokerOptional), jwtHelper.getUserId(), null));

            brokerResponse.setId(brokers.getId());
            if (!brokerOptional.isPresent()) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_NOT_EXISTS;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            brokerResponse.setName(brokers.getName());
            brokerResponse.setLogo(brokers.getLogo());
            brokerResponse.setAuthUrl(brokers.getAuthUrl());
            brokerResponse.setOnboardingUrl(brokers.getOnboardingUrl());
            brokerResponseList.add(brokerResponse);
        });

        return brokerResponseList;
    }

    protected void validateBrokerAndCustomer(Integer brokerId, String userId) {
        // find and set brokerId
        Brokers brokerDetails = brokersRepo.findById(brokerId).orElseThrow(() -> {
            BusinessErrorCode errorCode = BusinessErrorCode.BROKER_NOT_EXISTS;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        });
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Brokers", AlgoticUtils.objectToJsonString(brokerDetails), jwtHelper.getUserId(), null));

        Users customer = usersRepo.findByID(userId);
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Users Details", AlgoticUtils.objectToJsonString(customer), jwtHelper.getUserId(), null));
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
    }

    protected BrokerCustomerDetails saveBrokerCustomerDetails(Integer brokerId, String userRefId, String userId) {
        BrokerCustomerDetails brokerCustomerDetails = new BrokerCustomerDetails();
        Users users = usersRepo.findByID(userId);
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Users Details", AlgoticUtils.objectToJsonString(users), jwtHelper.getUserId(), null));
        if (users != null) {
            users.setStage(StageType.BROKERCONNECTED.name());
            users.setModifiedAt(new Date());
            usersRepo.save(users);

            brokerCustomerDetails.setBrokerId(brokerId);
            brokerCustomerDetails.setUserId(userId);
            brokerCustomerDetails.setReferenceID(userRefId);
            brokerCustomerDetails.setIsActive(true);
            brokerCustomerDetails.setCreatedAt(new Date());
            brokerCustomerDetailsRepo.save(brokerCustomerDetails);
        }
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Broker customer details",
                        AlgoticUtils.objectToJsonString(brokerCustomerDetails),
                        jwtHelper.getUserId(),
                        null));
        return brokerCustomerDetails;
    }

    protected void setOldSessionIdInactive(String userId) {
        BrokerSessionDetails brokerSessionDetails = brokerSessionDetailsRepo.getSessionId(userId);
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Broker Sessions Details",
                        AlgoticUtils.objectToJsonString(brokerSessionDetails),
                        jwtHelper.getUserId(),
                        null));
        if (brokerSessionDetails != null) {
            brokerSessionDetails.setIsActive(false);
            brokerSessionDetails.setModifiedAt(new Date());
            brokerSessionDetailsRepo.save(brokerSessionDetails);
        }
    }

    protected BrokerSessionDetails saveBrokerSessionDetails(
            String authCode, String sessionId, String userId, Integer brokerCustomerDetailsId) {
        BrokerSessionDetails brokerSessionDetails = new BrokerSessionDetails();
        brokerSessionDetails.setAuthCode(authCode);
        brokerSessionDetails.setSessionId(sessionId);
        brokerSessionDetails.setUserId(userId);
        brokerSessionDetails.setBrokerCustomerDetailID(brokerCustomerDetailsId);
        brokerSessionDetails.setIsActive(true);
        brokerSessionDetails.setCreatedAt(new Date());
        brokerSessionDetailsRepo.save(brokerSessionDetails);
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Broker Sessions Details",
                        AlgoticUtils.objectToJsonString(brokerSessionDetails),
                        jwtHelper.getUserId(),
                        null));
        return brokerSessionDetails;
    }
}
