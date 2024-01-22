package com.algotic.broker.api;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.AlgoticConstants;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.aliceblue.CancelOrderRequest;
import com.algotic.model.request.aliceblue.ChartHistoryRequest;
import com.algotic.model.request.aliceblue.EncApiRequest;
import com.algotic.model.request.aliceblue.EncApiSessionIdRequest;
import com.algotic.model.request.aliceblue.InstrumentHistoryRequest;
import com.algotic.model.request.aliceblue.InstrumentSearchRequest;
import com.algotic.model.request.aliceblue.ModifyOrderRequest;
import com.algotic.model.request.aliceblue.OrderHistoryRequest;
import com.algotic.model.request.aliceblue.PlaceOrderRequest;
import com.algotic.model.request.aliceblue.PositionBookRequest;
import com.algotic.model.request.aliceblue.SessionIDRequest;
import com.algotic.model.request.aliceblue.SquareOffRequest;
import com.algotic.model.response.aliceblue.*;
import com.algotic.utils.AlgoticUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * This class is responsible to call actual alice blue api and it take alice blue specific request & return a response
 *
 */
@Component
@Slf4j
public class AliceBlueBrokerApi {

    @Value("${sessionIdUrl}")
    private String sessionIdUrl;

    @Value("${placeOrderUrl}")
    private String placeOrderUrl;

    @Value("${aliceBlue.instrument.contract.master.base.url}")
    private String contractMasterBaseUrl;

    @Value("${aliceBlue.instrument.contract.master.query}")
    private String exchangeQuery;

    @Value("${positionUrl}")
    private String positionUrl;

    @Value("${holdingsUrl}")
    private String holdingsUrl;

    @Value("${orderHistoryUrl}")
    private String orderHistoryUrl;

    @Value("${orderBookUrl}")
    private String orderBookUrl;

    @Value("${tradeBookUrl}")
    private String tradeBookUrl;

    @Value("${cancelOrderUrl}")
    private String cancelOrderUrl;

    @Value("${squareOffAllUrl}")
    private String squareOffAllUrl;

    @Value("${squareOffUrl}")
    private String squareOffUrl;

    @Value("${instrumentHistoryUrl}")
    private String instrumentHistoryUrl;

    @Value("${modifyOrderUrl}")
    private String modifyOrderUrl;

    @Value("${searchInstrumentUrl}")
    private String searchInstrumentUrl;

    @Value("${encpKeyUrl}")
    private String encpKeyUrl;

    @Value("${encKeySessionIdUrl}")
    private String encKeySessionIdUrl;

    @Value("${chartHistoryUrl}")
    private String chartHistoryUrl;

    @Value("${paperTradeUserId}")
    private String paperTradeUserId;

    @Value("${nseUrl}")
    private String nseContractUrl;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private RestTemplate restTemplate;

    public String getSessionId(@RequestBody SessionIDRequest sessionIDRequest) throws NoSuchAlgorithmException {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("SessionId", "get userSession from vendor api", jwtHelper.getUserId(), null));
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<SessionIDRequest> entity = new HttpEntity<>(sessionIDRequest, headers);
            ResponseEntity<SessionIDResponse> responseEntity =
                    restTemplate.exchange(sessionIdUrl, HttpMethod.POST, entity, SessionIDResponse.class);
            SessionIDResponse result = responseEntity.getBody();

            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Result", AlgoticUtils.objectToJsonString(result), jwtHelper.getUserId(), null));

            if (result == null) {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else if (result.getSessionID() == null) {
                BusinessErrorCode errorCode = BusinessErrorCode.USER_ID_NOT_VALID;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            return result.getSessionID();
        } catch (AlgoticException ex) {
            throw ex;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "alice blue session id response error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public NSEResponse getNSEInstruments() {
        return getInstruments("NSE", NSEResponse.class);
    }

    public BSEResponse getBSEInstruments() {
        return getInstruments("BSE", BSEResponse.class);
    }

    public BFOResponse getBFOInstruments() {
        return getInstruments("BFO", BFOResponse.class);
    }

    public NFOResponse getNFOInstruments() {
        return getInstruments("NFO", NFOResponse.class);
    }

    public IndicesResponse getIndicesInstruments() {
        return getInstruments("INDICES", IndicesResponse.class);
    }

    private <T> T getInstruments(String exchange, Class<T> responseClass) {
        try {
            log.info(logConfig.getLogHandler().getInfoLog("instruments", "get instrument api call", null, null));
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<NSEResponse> entity = new HttpEntity<>(headers);
            String url = contractMasterBaseUrl + exchangeQuery + exchange;
            ResponseEntity<T> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, responseClass);
            return responseEntity.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "error in Indices instrument api call",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public PlaceOrderResponse placeOrder(PlaceOrderRequest orderRequest, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));
            List<PlaceOrderRequest> orders = new ArrayList<>();
            orders.add(orderRequest);
            HttpEntity<List<PlaceOrderRequest>> entity = new HttpEntity<>(orders, headers);
            ResponseEntity<List<PlaceOrderResponse>> responseEntity = restTemplate.exchange(
                    placeOrderUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});
            List<PlaceOrderResponse> result = responseEntity.getBody();
            if (result == null) {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                throw new AlgoticException(errorCode);
            }
            return result.get(0);
        } catch (AlgoticException ex) {
            throw ex;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
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

    public HoldingResponse holdings(String token) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("holdings", "holding alice blue api call", jwtHelper.getUserId(), "200"));
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<HoldingValue> entity = new HttpEntity<>(headers);
            ResponseEntity<HoldingResponse> responseEntity =
                    restTemplate.exchange(holdingsUrl, HttpMethod.GET, entity, HoldingResponse.class);
            HoldingResponse result = responseEntity.getBody();
            if (result == null
                    || result.getHoldingValue() == null
                    || result.getHoldingValue().isEmpty()) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            return result;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
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
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "holding alice blue api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public List<PositionBookResponse> bookPosition(PositionBookRequest positionBookRequest, String token) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Position book", "Get All Position books", jwtHelper.getUserId(), "200"));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));
            HttpEntity<PositionBookRequest> entity = new HttpEntity<>(positionBookRequest, headers);
            ResponseEntity<Object> responseEntity =
                    restTemplate.exchange(positionUrl, HttpMethod.POST, entity, Object.class);

            Object result = responseEntity.getBody();

            if (result instanceof Collection<?>) {
                List<PositionBookResponse> bookPositionList = new ArrayList<>();
                for (int i = 0; i < ((Collection<?>) result).size(); i++) {
                    bookPositionList.add(new ObjectMapper()
                            .convertValue(((ArrayList<?>) result).get(i), PositionBookResponse.class));
                }

                return bookPositionList;
            } else {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
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
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "book position alice blue api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public List<OrderHistoryResponse> orderHistory(OrderHistoryRequest orderHistoryRequest, String token) {
        try {

            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("order history", "get order history from alice blue", jwtHelper.getUserId(), "200"));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));
            List<OrderHistoryRequest> orderHistory = new ArrayList<>();
            orderHistory.add(orderHistoryRequest);
            HttpEntity<List<OrderHistoryRequest>> entity = new HttpEntity<>(orderHistory, headers);
            ResponseEntity<Object> responseEntity =
                    restTemplate.exchange(orderHistoryUrl, HttpMethod.POST, entity, Object.class);
            Object result = responseEntity.getBody();

            if (result == null
                    || (((LinkedHashMap<?, ?>) result).containsKey("stat")
                            && (((LinkedHashMap<?, ?>) result).get("stat").equals("Not_Ok")))) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            if (result instanceof Collection<?>) {
                List<OrderHistoryResponse> orderHistorylist = new ArrayList<>();
                for (int i = 0; i < ((Collection<?>) result).size(); i++) {
                    orderHistorylist.add(new ObjectMapper()
                            .convertValue(((ArrayList<?>) result).get(i), OrderHistoryResponse.class));
                }
                return orderHistorylist;
            } else {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
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
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "Order History alice blue api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public List<OrderBookResponse> getOrderBook(String token) {
        try {
            log.info(logConfig.getLogHandler().getInfoLog("get order book", "alice blue order book", null, "200"));
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));
            HttpEntity entity = new HttpEntity<>(headers);
            ResponseEntity<Object> responseEntity =
                    restTemplate.exchange(orderBookUrl, HttpMethod.GET, entity, Object.class);
            Object result = responseEntity.getBody();
            if (result instanceof Collection<?>) {
                List<OrderBookResponse> orderBookResponseList = new ArrayList<>();
                for (int i = 0; i < ((Collection<?>) result).size(); i++) {
                    orderBookResponseList.add(
                            new ObjectMapper().convertValue(((ArrayList<?>) result).get(i), OrderBookResponse.class));
                }

                return orderBookResponseList;

            } else {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "Order Book alice blue api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public List<TradeBookResponse> getTradeBook(String token) {
        try {
            log.info(logConfig.getLogHandler().getInfoLog("Trade book", "alice blue trade book", null, "200"));
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));
            HttpEntity entity = new HttpEntity<>(headers);
            ResponseEntity<Object> responseEntity =
                    restTemplate.exchange(tradeBookUrl, HttpMethod.GET, entity, Object.class);
            Object result = responseEntity.getBody();
            if (result instanceof Collection<?>) {
                List<TradeBookResponse> tradeBookResponseList = new ArrayList<>();
                for (int i = 0; i < ((Collection<?>) result).size(); i++) {
                    tradeBookResponseList.add(
                            new ObjectMapper().convertValue(((ArrayList<?>) result).get(i), TradeBookResponse.class));
                }

                return tradeBookResponseList;
            } else {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "Trade Book alice blue api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public CancelOrderResponse cancelOrder(CancelOrderRequest cancelOrderRequest, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));
            HttpEntity<CancelOrderRequest> entity = new HttpEntity<>(cancelOrderRequest, headers);
            ResponseEntity<CancelOrderResponse> responseEntity =
                    restTemplate.exchange(cancelOrderUrl, HttpMethod.POST, entity, CancelOrderResponse.class);
            CancelOrderResponse result = responseEntity.getBody();
            if (result == null) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                throw new AlgoticException(errorCode);
            }
            return result;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                throw new AlgoticException(errorCode);
            }
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            throw new AlgoticException(errorCode);
        }
    }

    public SquareOffResponse squareOffAll(String token) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("squareOffAll", "squareOffAll alice blue api call", jwtHelper.getUserId(), "200"));
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));
            HttpEntity entity = new HttpEntity(headers);
            ResponseEntity<SquareOffResponse> responseEntity =
                    restTemplate.exchange(squareOffAllUrl, HttpMethod.POST, entity, SquareOffResponse.class);
            SquareOffResponse result = responseEntity.getBody();

            if (result == null) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            return result;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
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
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "square Off All alice blue api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public InstrumentHistoryResponse instrumentHistory(InstrumentHistoryRequest historyRequest, String token) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("instrument History ", "Get instrument History", jwtHelper.getUserId(), null));
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));

            HttpEntity<InstrumentHistoryRequest> entity = new HttpEntity<>(historyRequest, headers);
            ResponseEntity<InstrumentHistoryResponse> responseEntity = restTemplate.exchange(
                    instrumentHistoryUrl, HttpMethod.POST, entity, InstrumentHistoryResponse.class);
            InstrumentHistoryResponse result = responseEntity.getBody();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "instrument History Result",
                            AlgoticUtils.objectToJsonString(result),
                            jwtHelper.getUserId(),
                            null));

            if (result == null) {
                throw new AlgoticException(CommonErrorCode.DATA_NOT_FOUND);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("instrument History Result", AlgoticUtils.objectToJsonString(result), null, null));

            return result;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
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
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "Instrument history alice blue api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public InstrumentHistoryResponse getInstrumentList(
            InstrumentHistoryRequest instrumentHistoryRequest, String token) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Instrumnet List", "Instrumnet list by alice blue api call", jwtHelper.getUserId(), null));
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<InstrumentHistoryRequest> entity = new HttpEntity<>(instrumentHistoryRequest, headers);
            ResponseEntity<InstrumentHistoryResponse> responseEntity = restTemplate.exchange(
                    instrumentHistoryUrl, HttpMethod.POST, entity, InstrumentHistoryResponse.class);
            InstrumentHistoryResponse result = responseEntity.getBody();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Instrumnet List", AlgoticUtils.objectToJsonString(result), jwtHelper.getUserId(), null));
            if (result == null) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            return result;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.error(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.error(logConfig
                        .getLogHandler()
                        .getInfoLog(
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
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "get Instrument List alice blue api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public ModifyOrderResponse modifyOrder(ModifyOrderRequest modifyOrderRequest, String token) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Modify order", "modify order by alice blue api call", jwtHelper.getUserId(), "200"));
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));
            List<ModifyOrderRequest> orders = new ArrayList<>();
            orders.add(modifyOrderRequest);
            HttpEntity<List<ModifyOrderRequest>> entity = new HttpEntity<>(orders, headers);
            ResponseEntity<List<ModifyOrderResponse>> responseEntity = restTemplate.exchange(
                    modifyOrderUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});
            List<ModifyOrderResponse> result = responseEntity.getBody();

            if (result == null) {
                throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }
            return result.get(0);
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "Modify order alice blue api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public List<InstrumentSearchResponse> getInstrumentNameBySearch(InstrumentSearchRequest instrumentSearchRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<InstrumentSearchRequest> entity = new HttpEntity<>(instrumentSearchRequest, headers);
            ResponseEntity<Object> responseEntity =
                    restTemplate.exchange(searchInstrumentUrl, HttpMethod.POST, entity, Object.class);

            Object result = responseEntity.getBody();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Alice Blue Result", AlgoticUtils.objectToJsonString(result), jwtHelper.getUserId(), null));

            if (result instanceof Collection<?>) {
                List<InstrumentSearchResponse> instrumentSearchResponseList = new ArrayList<>();
                for (int i = 0; i < ((Collection<?>) result).size(); i++) {
                    instrumentSearchResponseList.add(new ObjectMapper()
                            .convertValue(((ArrayList<?>) result).get(i), InstrumentSearchResponse.class));
                }
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Instrument search list",
                                AlgoticUtils.objectToJsonString(instrumentSearchResponseList),
                                jwtHelper.getUserId(),
                                null));
                return instrumentSearchResponseList;
            } else {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
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
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "get instrument by name alice blue api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public SquareOffResponse squareOff(SquareOffRequest squareOffRequest, String token) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Square off", "square off by alice blue api call", jwtHelper.getUserId(), "200"));
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));
            List<SquareOffRequest> squareOff = new ArrayList<>();
            squareOff.add(squareOffRequest);
            HttpEntity<List<SquareOffRequest>> entity = new HttpEntity<>(squareOff, headers);
            ResponseEntity<SquareOffResponse> responseEntity =
                    restTemplate.exchange(squareOffUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});
            SquareOffResponse result = responseEntity.getBody();

            if (result == null) {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            return result;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "Alice Blue square off order api response error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public EncApiResponse encryptionApiKey(EncApiRequest encApiRequest) {
        try {
            log.info(logConfig.getLogHandler().getInfoLog("encryption key api", "encryption key api call", null, null));
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<EncApiRequest> entity = new HttpEntity<>(encApiRequest, headers);
            ResponseEntity<EncApiResponse> responseEntity =
                    restTemplate.exchange(encpKeyUrl, HttpMethod.POST, entity, EncApiResponse.class);
            EncApiResponse result = responseEntity.getBody();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Encryption key", AlgoticUtils.objectToJsonString(result), null, null));

            if (result == null) {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            return result;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "Alice Blue encryption Api Key  response error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public EncApiSessionIdResponse encrypApiSessionId(EncApiSessionIdRequest encApiSessionIdRequest) {
        try {
            log.info(logConfig.getLogHandler().getInfoLog("encrypApiSessionId", "encrypApiSessionId", null, null));

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<EncApiSessionIdRequest> entity = new HttpEntity<>(encApiSessionIdRequest, headers);
            ResponseEntity<EncApiSessionIdResponse> responseEntity =
                    restTemplate.exchange(encKeySessionIdUrl, HttpMethod.POST, entity, EncApiSessionIdResponse.class);
            EncApiSessionIdResponse result = responseEntity.getBody();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("encrypApiSessionId", AlgoticUtils.objectToJsonString(result), null, null));
            if (result == null) {
                CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            return result;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "encryp Api Session Id alice blue api call error",
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    public ChartHistoryResponse chartHistory(ChartHistoryRequest chartHistoryRequest, String token) {
        try {
            String userIdToken = paperTradeUserId + " " + token;
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + userIdToken));
            HttpEntity<ChartHistoryRequest> entity = new HttpEntity<>(chartHistoryRequest, headers);
            ResponseEntity<ChartHistoryResponse> responseEntity =
                    restTemplate.exchange(chartHistoryUrl, HttpMethod.POST, entity, ChartHistoryResponse.class);
            ChartHistoryResponse result = responseEntity.getBody();
            if (result == null) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            return result;
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }
}
