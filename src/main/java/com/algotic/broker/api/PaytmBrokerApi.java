package com.algotic.broker.api;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.paytm.PaytmHeader;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.paytm.PaytmAccessTokenRequest;
import com.algotic.model.request.paytm.PaytmCancelOrderRequest;
import com.algotic.model.request.paytm.PlaceOrderRequest;
import com.algotic.model.response.paytm.OrderBookData;
import com.algotic.model.response.paytm.PaytmCancelOrderResponse;
import com.algotic.model.response.paytm.PaytmGeneralResponse;
import com.algotic.model.response.paytm.PaytmHoldingResponse;
import com.algotic.model.response.paytm.PaytmHoldingResult;
import com.algotic.model.response.paytm.PaytmLTPData;
import com.algotic.model.response.paytm.PaytmLTPResponse;
import com.algotic.model.response.paytm.PaytmPositionData;
import com.algotic.model.response.paytm.PaytmPositionResponse;
import com.algotic.model.response.paytm.PaytmProfileInfoResponse;
import com.algotic.model.response.paytm.PaytmSessionResponse;
import com.algotic.model.response.paytm.PlaceOrderData;
import com.algotic.model.response.paytm.TradeBookData;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class PaytmBrokerApi {

    @Value("${paytm.placeorderurl}")
    private String placeOrderUrl;

    @Value("${paytm.userProfileUrl}")
    private String profileDataUrl;

    @Value("${paytm.apiAuthenticationUrl}")
    private String authenticationUrl;

    @Value("${paytm.positionUrl}")
    private String positionUrl;

    @Value("${paytm.orderbookurl}")
    private String orderBookUrl;

    @Value("${paytm.apiSecretKey}")
    private String apiSecretKey;

    @Value("${paytm.holdingUrl}")
    private String holdingUrl;

    @Value("${paytm.LTPDataUrl}")
    private String ltpDataUrl;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private LogHandlerConfiguration config;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${paytm.tradebookurl}")
    private String tradeBookUrl;

    @Value("${paytm.cancelOrder}")
    private String cancelOrderUrl;

    public PaytmGeneralResponse<PlaceOrderData> placeOrder(PlaceOrderRequest placeOrderReq, String token) {

        try {
            HttpEntity<PlaceOrderRequest> entity = new HttpEntity<>(placeOrderReq, getHeaders(token));
            log.debug("Placing order via {} using entity", placeOrderUrl, entity);
            PaytmGeneralResponse<PlaceOrderData> result = restTemplate
                    .exchange(
                            placeOrderUrl,
                            HttpMethod.POST,
                            entity,
                            new ParameterizedTypeReference<PaytmGeneralResponse<PlaceOrderData>>() {})
                    .getBody();

            if (result == null) {
                log.error("Getting result null after calling from paytm api");
                throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }

            return result;
        } catch (AlgoticException ex) {
            throw ex;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Excepption occurred -> ", ex);
            if (HttpStatus.UNAUTHORIZED.equals(ex.getStatusCode())) {
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
            log.error("Exception occurred -> ", e);
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            throw new AlgoticException(errorCode);
        }
    }

    public Optional<PaytmProfileInfoResponse> getUserProfile(String sessionToken) {
        try {
            log.info(config.getLogHandler()
                    .getInfoLog("Method start -> getUserProfile", "uri -> " + profileDataUrl, null, null));
            HttpEntity<?> entity = new HttpEntity<>(getHeaders(sessionToken));

            return restTemplate
                    .exchange(
                            profileDataUrl,
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<Optional<PaytmProfileInfoResponse>>() {})
                    .getBody();

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Http exception occure while getting userProfile for paytm broker. Exception -> [{}]", ex);
            if (HttpStatus.UNAUTHORIZED.equals(ex.getStatusCode())) {
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
            log.error("Exception occurred while fetching profile information of broker customer -> {[{}]", e);
            return Optional.empty();
        }
    }

    public Optional<PaytmSessionResponse> getSessionToken(PaytmAccessTokenRequest tokenRequest) {
        try {
            log.info(config.getLogHandler()
                    .getInfoLog("Method start -> getSessionId", "uri -> " + authenticationUrl, null, null));
            HttpHeaders header = new HttpHeaders();
            header.add("Content-Type", "application/json");
            HttpEntity<PaytmAccessTokenRequest> entity = new HttpEntity<>(tokenRequest, header);
            return restTemplate
                    .exchange(
                            authenticationUrl,
                            HttpMethod.POST,
                            entity,
                            new ParameterizedTypeReference<Optional<PaytmSessionResponse>>() {})
                    .getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Http exception occure while getting session token for paytm broker. Exception -> [{}]", ex);
            if (HttpStatus.UNAUTHORIZED.equals(ex.getStatusCode())) {
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
            log.error("Exception occurred while fetching profile information of broker customer -> ", e);
            return Optional.empty();
        }
    }

    /**
     * @param sessionToken
     * @return Paytm money order position list
     * If Response data is empty it throws exception
     */
    public List<PaytmPositionData> position(String sessionToken) {
        try {
            log.info(config.getLogHandler()
                    .getInfoLog("Position book", "Get All Position books", jwtHelper.getUserId(), "200"));
            HttpEntity<?> entity = new HttpEntity<>(getHeaders(sessionToken));

            ResponseEntity<PaytmPositionResponse> paytmPositionResponse =
                    restTemplate.exchange(positionUrl, HttpMethod.GET, entity, PaytmPositionResponse.class);

            List<PaytmPositionData> paytmPositionDataList =
                    paytmPositionResponse.getBody().getData();
            if (paytmPositionDataList.isEmpty()) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(config.getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            return paytmPositionDataList;
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
                            "book position paytm api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public HttpHeaders getHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(PaytmHeader.PAYTM_JWT_TOKEN, token);
        return headers;
    }

    public PaytmGeneralResponse<OrderBookData> getOrderBook(String token) {

        try {
            HttpEntity<?> entity = new HttpEntity<>(getHeaders(token));
            PaytmGeneralResponse<OrderBookData> result = restTemplate
                    .exchange(
                            orderBookUrl,
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<PaytmGeneralResponse<OrderBookData>>() {})
                    .getBody();

            if (result == null) {
                throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }

            return result;
        } catch (AlgoticException ex) {
            throw ex;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (HttpStatus.UNAUTHORIZED.equals(ex.getStatusCode())) {
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
            throw new AlgoticException(errorCode);
        }
    }

    /**
     * @param sessionToken
     * @return list of paytm holding data
     * If Holding response is than it throws an exception
     */
    public List<PaytmHoldingResult> holding(String sessionToken) {
        try {
            log.info(config.getLogHandler()
                    .getInfoLog("holdings", "holding Paytm Money api call", jwtHelper.getUserId(), "200"));
            HttpEntity<?> entity = new HttpEntity<>(getHeaders(sessionToken));

            ResponseEntity<PaytmHoldingResponse> paytmHoldingResponse =
                    restTemplate.exchange(holdingUrl, HttpMethod.GET, entity, PaytmHoldingResponse.class);
            List<PaytmHoldingResult> holdingDataList =
                    paytmHoldingResponse.getBody().getData().getResults();

            if (holdingDataList.isEmpty()) {
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
                            "holding paytm money api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    /**
     * @param exchange
     * @param scripId
     * @param scripType
     * @param sessionToken
     * @return paytm LTP data
     * If not found any data than throws an exception
     */
    public List<PaytmLTPData> getLTPData(String exchange, String scripId, String scripType, String sessionToken) {
        try {
            log.info(config.getLogHandler()
                    .getInfoLog("getLTPData", "LTP Paytm Money api call", jwtHelper.getUserId(), "200"));
            HttpEntity<?> entity = new HttpEntity<>(getHeaders(sessionToken));

            log.info("Generating param URL for getting LTP data");
            String url = UriComponentsBuilder.fromUriString(ltpDataUrl)
                    .queryParam("mode", "LTP")
                    .queryParam("pref", exchange + ":" + scripId + ":" + scripType)
                    .toUriString();

            ResponseEntity<PaytmLTPResponse> paytmLTPResponse =
                    restTemplate.exchange(url, HttpMethod.GET, entity, PaytmLTPResponse.class);
            List<PaytmLTPData> ltpData = paytmLTPResponse.getBody().getData();

            if (!ltpData.get(0).isFound()) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(config.getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            return ltpData;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Http exception occure while getting LTP data for paytm broker. Exception -> [{}]", ex);
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
                            "LTP paytm money api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public PaytmGeneralResponse<TradeBookData> getTradeBook(
            String orderNo, String legNo, String segment, String token) {

        try {
            UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(tradeBookUrl)
                    .queryParam("order_no", orderNo)
                    .queryParam("leg_no", legNo)
                    .queryParam("segment", segment);
            HttpEntity<?> entity = new HttpEntity<>(getHeaders(token));
            PaytmGeneralResponse<TradeBookData> result = restTemplate
                    .exchange(
                            uri.toUriString(),
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<PaytmGeneralResponse<TradeBookData>>() {})
                    .getBody();

            if (result == null) {
                throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }

            return result;
        } catch (AlgoticException ex) {
            throw ex;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (HttpStatus.UNAUTHORIZED.equals(ex.getStatusCode())) {
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
                throw new AlgoticException(errorCode);
            }
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            throw new AlgoticException(errorCode);
        }
    }

    /**
     * @param cancelOrderRequest
     * @param sessionToken
     * @return paytmCancelOrder specific response
     */
    public PaytmCancelOrderResponse cancelOrder(PaytmCancelOrderRequest cancelOrderRequest, String sessionToken) {
        try {
            log.info("Method start -> cancelOrder(), where cancelOrderUrl -> [{}]", cancelOrderUrl);
            HttpEntity<?> entity = new HttpEntity<>(cancelOrderRequest, getHeaders(sessionToken));

            PaytmCancelOrderResponse cancelOrderRes = restTemplate
                    .exchange(cancelOrderUrl, HttpMethod.POST, entity, PaytmCancelOrderResponse.class)
                    .getBody();
            if (cancelOrderRes == null) {
                log.error("Getting result null after calling from paytm api");
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
                log.info(config.getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
        } catch (Exception e) {
            log.error("Exception occurred -> ", e);
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            throw new AlgoticException(errorCode);
        }
    }
}
