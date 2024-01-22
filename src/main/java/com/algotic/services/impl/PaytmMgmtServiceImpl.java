package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.broker.api.PaytmBrokerApi;
import com.algotic.config.JwtHelper;
import com.algotic.constants.AlgoticMessages;
import com.algotic.constants.BrokerEnum;
import com.algotic.data.entities.BrokerCustomerDetails;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.IBrokerSessionRequest;
import com.algotic.model.request.paytm.PaytmAccessTokenRequest;
import com.algotic.model.request.paytm.PaytmSessionRequest;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.model.response.paytm.PaytmProfileInfoResponse;
import com.algotic.model.response.paytm.PaytmSessionResponse;
import com.algotic.services.BrokerMgmtService;
import com.algotic.utils.AlgoticUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaytmMgmtServiceImpl extends CommonBrokerMgmtServiceImpl implements BrokerMgmtService {

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private PaytmBrokerApi brokerApi;

    @Value("${paytm.apiSecretKey}")
    private String apiSecret;

    @Value("${paytm.apiKey}")
    private String apiKey;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Override
    public ResponseEntity<GlobalMessageResponse> saveCustomerBrokerDetails(
            IBrokerSessionRequest customerBrokerRequest) {
        try {
            log.info("Method start -> saveCustomerBrokerDetails for broker paytm...");
            String userId = jwtHelper.getUserId();
            log.info("saving broker details for user {}", userId);
            PaytmSessionRequest paytmSessionRequest = (PaytmSessionRequest) customerBrokerRequest;

            validateBrokerAndCustomer(paytmSessionRequest.brokerId(), userId);

            log.info("Fetching sessionToken for paytm broker.");
            String sessionToken = getSessionToken(paytmSessionRequest.authToken());

            log.info("Fetching userRefId from the user profile");
            String userRefId = getPaytmUserId(sessionToken);

            BrokerCustomerDetails brokerCustomerDetails =
                    saveBrokerCustomerDetails(paytmSessionRequest.brokerId(), userRefId, userId);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Broker Customer Details",
                            AlgoticUtils.objectToJsonString(brokerCustomerDetails),
                            userId,
                            null));

            setOldSessionIdInactive(userId);

            saveBrokerSessionDetails(
                    paytmSessionRequest.authToken(), sessionToken, userId, brokerCustomerDetails.getId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Data Save Successfully", AlgoticMessages.SAVED_SUCCESSFULLY, userId, "201"));

            return new ResponseEntity<>(
                    new GlobalMessageResponse(AlgoticMessages.SAVED_SUCCESSFULLY), HttpStatus.CREATED);
        } catch (AlgoticException e) {
            throw e;
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

    private String getSessionToken(String authToken) {
        try {
            PaytmAccessTokenRequest accessTokenRequest = new PaytmAccessTokenRequest(apiKey, apiSecret, authToken);
            PaytmSessionResponse sessionResponse = brokerApi
                    .getSessionToken(accessTokenRequest)
                    .orElseThrow(() -> new AlgoticException(CommonErrorCode.ACCESS_TOKEN_NOT_FOUND));
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Session id ",
                            AlgoticUtils.objectToJsonString(sessionResponse),
                            jwtHelper.getUserId(),
                            null));
            return sessionResponse.getAccessToken();
        } catch (AlgoticException e) {
            log.error("Some error occure while getting session token for paytm broker. Exception -> [{}]", e);
            throw e;
        } catch (Exception ex) {
            BusinessErrorCode errorCode = BusinessErrorCode.USER_ID_NOT_VALID;
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

    /**
     * @param sessionToken
     * @return paytm broker userId using session token
     */
    private String getPaytmUserId(String sessionToken) {
        try {
            PaytmProfileInfoResponse profileInfo = brokerApi
                    .getUserProfile(sessionToken)
                    .orElseThrow(() -> new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR));

            if (StringUtils.equalsIgnoreCase("ERROR", profileInfo.getMeta().getMessage())) {
                log.error("Some error occurred while fetching profile information ["
                        + profileInfo.getMeta().getMessage() + "]");
                throw new AlgoticException(CommonErrorCode.DATA_NOT_FOUND);
            }

            return profileInfo.getData().getUserId();
        } catch (AlgoticException e) {
            log.error("Error occure while getting paytmUserId. Exeception -> [{}]", e);
            throw e;
        } catch (Exception e) {
            log.info("Exception occured fetching  broker user id", e);
            return null;
        }
    }

    @Override
    public BrokerEnum getBrokerName() {
        return BrokerEnum.PAYTM_MONEY;
    }
}
