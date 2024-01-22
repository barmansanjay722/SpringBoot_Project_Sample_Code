package com.algotic.broker.api;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.motilal.CancelOrderRequest;
import com.algotic.model.request.motilal.MotilalLTPRequest;
import com.algotic.model.request.motilal.PlaceOrderRequest;
import com.algotic.model.response.motilal.CancelOrderResponse;
import com.algotic.model.response.motilal.MotilalHoldingData;
import com.algotic.model.response.motilal.MotilalHoldingResponse;
import com.algotic.model.response.motilal.MotilalLTPData;
import com.algotic.model.response.motilal.MotilalLTPResponse;
import com.algotic.model.response.motilal.MotilalPositionData;
import com.algotic.model.response.motilal.MotilalPositionResponse;
import com.algotic.model.response.motilal.OrderBookResponse;
import com.algotic.model.response.motilal.PlaceOrderResponse;
import com.algotic.model.response.motilal.ProfileInfoResponse;
import com.algotic.model.response.motilal.TradeBookResponse;
import com.algotic.utils.MotilalUtils;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * This class is responsible to call actual motilal api and it take motilal
 * specific request & return a response
 *
 */
@Component
@Slf4j
public class MotilalBrokerApi {

    @Value("${motilal.placeOrderUrl}")
    private String placeOrderUrl;

    @Value("${motilal.apiSecret}")
    private String apiKey;

    @Value("${motilal.position.url}")
    private String positionUrl;

    @Value("${motilal.holding.url}")
    private String holdingUrl;

    @Value("${motilal.profileDataUrl}")
    private String profileDataUrl;

    @Value("${motilal.cancelOrderUrl}")
    private String cancelOrderUrl;

    @Value("${motilal.orderbookurl}")
    private String orderBookUrl;

    @Value("${motilal.tradebookurl}")
    private String tradeBookUrl;

    @Value("${motilal.ltpDataUrl}")
    private String ltpDataUrl;

    @Autowired
    private LogHandlerConfiguration config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtHelper jwtHelper;

    /**
     * @param placeOrderReq
     * @param token
     * @return
     */
    public PlaceOrderResponse placeOrder(PlaceOrderRequest placeOrderReq, String token) {
        try {
            HttpEntity<PlaceOrderRequest> entity =
                    new HttpEntity<>(placeOrderReq, MotilalUtils.getMotilalHeader(token, apiKey));
            log.info("Placing order via {} using entity", placeOrderUrl, entity);
            PlaceOrderResponse result = restTemplate
                    .exchange(placeOrderUrl, HttpMethod.POST, entity, PlaceOrderResponse.class)
                    .getBody();
            if (result == null) {
                log.error("Getting result null after calling from motilal api");
                throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }
            return result;
        } catch (AlgoticException ex) {
            throw ex;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Excepption occurred -> ", ex);
            if (HttpStatus.UNAUTHORIZED.equals(ex.getStatusCode())) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                throw new AlgoticException(errorCode);
            }
        } catch (Exception e) {
            log.error("Excepption occurred -> ", e);
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            throw new AlgoticException(errorCode);
        }
    }

    public CancelOrderResponse cancelOrder(CancelOrderRequest cancelOrderRequest, String token) {
        try {
            HttpEntity<CancelOrderRequest> entity =
                    new HttpEntity<>(cancelOrderRequest, MotilalUtils.getMotilalHeader(token, apiKey));
            log.info("Cancel order via {} using entity", cancelOrderUrl, entity);
            CancelOrderResponse cancelOrderRes = restTemplate
                    .exchange(cancelOrderUrl, HttpMethod.POST, entity, CancelOrderResponse.class)
                    .getBody();
            if (cancelOrderRes == null) {
                log.error("Getting result null after calling from motilal api");
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                throw new AlgoticException(errorCode);
            }
            return cancelOrderRes;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                throw new AlgoticException(errorCode);
            }
        } catch (Exception e) {
            log.error("Excepption occurred -> ", e);
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            throw new AlgoticException(errorCode);
        }
    }

    /**
     * @param token
     * @return list of motilal holding data object
     */
    public List<MotilalHoldingData> holding(String token) {
        try {
            log.info(config.getLogHandler()
                    .getInfoLog("holdings", "holding Motilal Oswal api call", jwtHelper.getUserId(), "200"));

            HttpEntity<Object> entity = new HttpEntity<>(MotilalUtils.getMotilalHeader(token, apiKey));

            ResponseEntity<MotilalHoldingResponse> motilalHoldingResponse =
                    restTemplate.exchange(holdingUrl, HttpMethod.POST, entity, MotilalHoldingResponse.class);
            List<MotilalHoldingData> holdingDataList =
                    motilalHoldingResponse.getBody().getData();

            if (holdingDataList == null) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(config.getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            return holdingDataList;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.info(config.getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(config.getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);

        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(config.getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "holding motilal oswal api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    /**
     *
     * @param token
     * @return list of motilal position data object
     */
    public List<MotilalPositionData> position(String token) {
        try {
            log.info(config.getLogHandler()
                    .getInfoLog("Position book", "Get All Position books", jwtHelper.getUserId(), "200"));

            HttpEntity<Object> entity = new HttpEntity<>(MotilalUtils.getMotilalHeader(token, apiKey));
            ResponseEntity<MotilalPositionResponse> motilalPositionResponse =
                    restTemplate.exchange(positionUrl, HttpMethod.POST, entity, MotilalPositionResponse.class);

            List<MotilalPositionData> motilalPositionDataList =
                    motilalPositionResponse.getBody().getData();
            if (motilalPositionDataList == null) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(config.getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            return motilalPositionDataList;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.info(config.getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(config.getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(config.getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "book position motilal oswal api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public Optional<ProfileInfoResponse> getUserProfileData(String token) {
        try {
            log.info(config.getLogHandler()
                    .getInfoLog("Method start -> getUserProfileData", "uri -> " + profileDataUrl, null, null));
            HttpEntity<?> entity = new HttpEntity<>(MotilalUtils.getMotilalHeader(token, apiKey));
            ProfileInfoResponse profileInfoRes = restTemplate
                    .exchange(profileDataUrl, HttpMethod.POST, entity, ProfileInfoResponse.class)
                    .getBody();
            return Optional.ofNullable(profileInfoRes);
        } catch (Exception e) {
            log.error("Exception occurred while fetching profile information of broker customer -> ", e);
            return Optional.empty();
        }
    }

    public OrderBookResponse getOrderBook(String token) {
        try {
            log.info(config.getLogHandler()
                    .getInfoLog("Method start -> getOrderBook", "uri -> " + orderBookUrl, null, null));
            HttpEntity<CancelOrderRequest> entity = new HttpEntity<>(MotilalUtils.getMotilalHeader(token, apiKey));
            OrderBookResponse orderBookRes = restTemplate
                    .exchange(orderBookUrl, HttpMethod.POST, entity, OrderBookResponse.class)
                    .getBody();
            if (orderBookRes == null) {
                log.warn("Getting response form broker is empty");
                throw new AlgoticException(CommonErrorCode.DATA_NOT_FOUND);
            }
            return orderBookRes;
        } catch (Exception e) {
            log.error("Excepption occurred -> ", e);
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     *This method help to fetch the complete information of the Trades of the Users
     *
     */
    public TradeBookResponse getTradeBook(String token) {
        try {
            log.info(config.getLogHandler()
                    .getInfoLog("Method start -> getTradeBook", "uri -> " + tradeBookUrl, null, null));
            HttpEntity<?> entity = new HttpEntity<>(MotilalUtils.getMotilalHeader(token, apiKey));
            TradeBookResponse tradeBookres = restTemplate
                    .exchange(tradeBookUrl, HttpMethod.POST, entity, TradeBookResponse.class)
                    .getBody();
            if (tradeBookres == null) {
                log.warn("Getting response form broker is empty");
                throw new AlgoticException(CommonErrorCode.DATA_NOT_FOUND);
            }
            return tradeBookres;
        } catch (Exception e) {
            log.error("Excepption occurred -> ", e);
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public MotilalLTPData getLtpData(MotilalLTPRequest ltpRequest, String token) {
        try {
            HttpEntity<MotilalLTPRequest> entity =
                    new HttpEntity<>(ltpRequest, MotilalUtils.getMotilalHeader(token, apiKey));

            MotilalLTPResponse ltpDataResponse = restTemplate
                    .exchange(ltpDataUrl, HttpMethod.POST, entity, MotilalLTPResponse.class)
                    .getBody();

            if (ltpDataResponse == null) {
                throw new AlgoticException(CommonErrorCode.DATA_NOT_FOUND);
            }

            if (StringUtils.equals("ERROR", ltpDataResponse.getStatus())) {
                log.warn("Some error occurred [" + ltpDataResponse.getMessage() + "]");
            }

            return ltpDataResponse.getMotilalLtpData().orElse(new MotilalLTPData());

        } catch (Exception e) {
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
