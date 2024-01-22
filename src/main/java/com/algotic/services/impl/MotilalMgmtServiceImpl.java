package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.broker.api.MotilalBrokerApi;
import com.algotic.config.JwtHelper;
import com.algotic.constants.AlgoticMessages;
import com.algotic.constants.BrokerEnum;
import com.algotic.data.entities.BrokerCustomerDetails;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.exception.MotilalBrokerErrorCode;
import com.algotic.model.request.IBrokerSessionRequest;
import com.algotic.model.request.motilal.MotilalSessionRequest;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.model.response.motilal.ProfileInfoResponse;
import com.algotic.services.BrokerMgmtService;
import com.algotic.utils.AlgoticUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service(value = "Motilal")
@Slf4j
public class MotilalMgmtServiceImpl extends CommonBrokerMgmtServiceImpl implements BrokerMgmtService {

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private MotilalBrokerApi brokerApi;

    @Value("${motilal.apiSecret}")
    private String apiSecret;

    @Override
    public ResponseEntity<GlobalMessageResponse> saveCustomerBrokerDetails(IBrokerSessionRequest commonBrokerRequest) {
        log.info("Method start -> saveCustomerBrokerDetails for broker motilal...");
        String userId = jwtHelper.getUserId();
        log.info("saving broker details for user {}", userId);
        MotilalSessionRequest motilalBrokerReq = (MotilalSessionRequest) commonBrokerRequest;
        validateBrokerAndCustomer(motilalBrokerReq.brokerId(), userId);
        String userRefID = getMotilalUserId(motilalBrokerReq.authToken());
        BrokerCustomerDetails brokerCustomerDetails =
                saveBrokerCustomerDetails(motilalBrokerReq.brokerId(), userRefID, userId);
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Broker Customer Details",
                        AlgoticUtils.objectToJsonString(brokerCustomerDetails),
                        userId,
                        null));
        setOldSessionIdInactive(userId);
        saveBrokerSessionDetails(apiSecret, motilalBrokerReq.authToken(), userId, brokerCustomerDetails.getId());
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Data Save Successfully", AlgoticMessages.SAVED_SUCCESSFULLY, userId, "201"));
        return new ResponseEntity<>(new GlobalMessageResponse(AlgoticMessages.SAVED_SUCCESSFULLY), HttpStatus.CREATED);
    }

    private String getMotilalUserId(String token) {
        try {
            ProfileInfoResponse profileInfo = brokerApi
                    .getUserProfileData(token)
                    .orElseThrow(() -> new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR));

            if (StringUtils.equals("ERROR", profileInfo.status())) {
                log.error("Some error occurred while fetching profile information [" + profileInfo.message() + "]");
                throw new AlgoticException(MotilalBrokerErrorCode.getErrorCodeByRefId(profileInfo.errorcode()));
            }

            return profileInfo.data().clientcode();
        } catch (Exception e) {
            log.info("Exception occured fetching  broker user id", e);
            return null;
        }
    }

    @Override
    public BrokerEnum getBrokerName() {
        return BrokerEnum.MOTILAL_OSWAL;
    }
}
