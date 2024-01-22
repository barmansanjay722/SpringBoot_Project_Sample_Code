package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.broker.api.AliceBlueBrokerApi;
import com.algotic.config.JwtHelper;
import com.algotic.constants.AlgoticMessages;
import com.algotic.constants.BrokerEnum;
import com.algotic.data.entities.BrokerCustomerDetails;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.IBrokerSessionRequest;
import com.algotic.model.request.aliceblue.AliceBlueSessionRequest;
import com.algotic.model.request.aliceblue.SessionIDRequest;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.services.BrokerMgmtService;
import com.algotic.utils.AlgoticUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service(value = "ALICE_BLUE")
public class AliceBlueMgmtServiceImpl extends CommonBrokerMgmtServiceImpl implements BrokerMgmtService {
    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private AliceBlueBrokerApi brokerApi;

    @Value("${appcode}")
    private String appcode;

    @Value("${apiSecret}")
    private String apiSecret;

    @Override
    public ResponseEntity<GlobalMessageResponse> saveCustomerBrokerDetails(IBrokerSessionRequest commonBrokerRequest) {
        AliceBlueSessionRequest customerBrokerRequest = (AliceBlueSessionRequest) commonBrokerRequest;
        String userId = jwtHelper.getUserId();
        Integer brokerId = customerBrokerRequest.getBrokerID();
        String userRefId = customerBrokerRequest.getUserID();
        try {

            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Get App Code", AlgoticUtils.objectToJsonString(appcode), userId, null));
            if (!appcode.equals(customerBrokerRequest.getAppCode())) {
                BusinessErrorCode errorCode = BusinessErrorCode.INVALID_APP_CODE;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            validateBrokerAndCustomer(customerBrokerRequest.getBrokerID(), userId);
            String sessionId = getSessionId(customerBrokerRequest);
            BrokerCustomerDetails brokerCustomerDetails = saveBrokerCustomerDetails(brokerId, userRefId, userId);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Broker Customer Details",
                            AlgoticUtils.objectToJsonString(brokerCustomerDetails),
                            userId,
                            null));
            setOldSessionIdInactive(userId);
            saveBrokerSessionDetails(
                    customerBrokerRequest.getAuthCode(), sessionId, userId, brokerCustomerDetails.getId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Data Save Successfully", AlgoticMessages.SAVED_SUCCESSFULLY, userId, "201"));
            return new ResponseEntity<>(
                    new GlobalMessageResponse(AlgoticMessages.SAVED_SUCCESSFULLY), HttpStatus.CREATED);

        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception de) {
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

    private String getSessionId(AliceBlueSessionRequest aliceBlueSessionReq) {
        try {
            SessionIDRequest sessionIDRequest = new SessionIDRequest();
            String shaString = aliceBlueSessionReq.getUserID() + aliceBlueSessionReq.getAuthCode() + apiSecret;
            String shaValue = AlgoticUtils.getSHA(shaString);
            sessionIDRequest.setCheckSum(shaValue);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Session id ",
                            AlgoticUtils.objectToJsonString(sessionIDRequest),
                            jwtHelper.getUserId(),
                            null));
            return brokerApi.getSessionId(sessionIDRequest);
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

    @Override
    public BrokerEnum getBrokerName() {
        return BrokerEnum.ALICE_BLUE;
    }
}
