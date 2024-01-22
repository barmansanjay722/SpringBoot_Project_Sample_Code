package com.algotic.services.impl;

import static java.lang.System.currentTimeMillis;

import com.algotic.adapter.AliceBlueWebSocketProvider;
import com.algotic.base.LogHandlerConfiguration;
import com.algotic.broker.api.AliceBlueBrokerApi;
import com.algotic.config.JwtHelper;
import com.algotic.constants.BrokerEnum;
import com.algotic.constants.Exchange;
import com.algotic.constants.InstrumentType;
import com.algotic.data.entities.BrokerCustomerDetails;
import com.algotic.data.entities.Brokers;
import com.algotic.data.entities.InstrumentWatchLists;
import com.algotic.data.entities.Instruments;
import com.algotic.data.repositories.*;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.AlgoticInstrumentRequest;
import com.algotic.model.request.aliceblue.EncApiRequest;
import com.algotic.model.request.aliceblue.EncApiSessionIdRequest;
import com.algotic.model.request.aliceblue.InstrumentHistoryRequest;
import com.algotic.model.response.AlgoticResultResponse;
import com.algotic.model.response.InstrumentSearchAllStockResponse;
import com.algotic.model.response.WebSocketResponse;
import com.algotic.model.response.aliceblue.*;
import com.algotic.services.InstrumentService;
import com.algotic.services.UserService;
import com.algotic.utils.AlgoticUtils;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InstrumentServiceImpl implements InstrumentService {

    @Autowired
    private AliceBlueBrokerApi aliceBlueProvider;

    @Autowired
    private BrokerCustomerDetailsRepo brokerCustomerDetailsRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private AliceBlueWebSocketProvider aliceblueWebSocketProvider;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private BrokerSessionDetailsRepo brokerSessionDetailsRepo;

    @Autowired
    private InstrumentWatchListsRepo instrumentWatchListsRepo;

    @Autowired
    private InstrumentsRepo instrumentsRepo;

    @Autowired
    private BrokersRepo brokersRepo;

    @Value("${paperTradeUserId}")
    private String paperTradeUserId;

    @Value("${apiKey}")
    private String apiKey;

    @Override
    public ResponseEntity<List<InstrumentSearchAllStockResponse>> getStockNames(
            String search, String instrumentType, String exchange, Integer limit) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "List of instrument",
                            "List of Instrumnet According to filter",
                            jwtHelper.getUserId(),
                            null));
            List<InstrumentSearchAllStockResponse> instrumentSearchAllStockResponseList = new ArrayList<>();
            String userId = jwtHelper.getUserId();
            String[] exchanges = searchFilter(instrumentType, exchange);

            List<InstrumentSearchResponse> instrumentSearchResponseList =
                    instrumentSearchResponseListData(search, exchanges, limit);

            List<InstrumentWatchLists> watchLists = instrumentWatchListsRepo.getAllInstrumentData(userId);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Watch List", AlgoticUtils.objectToJsonString(watchLists), jwtHelper.getUserId(), null));
            for (InstrumentSearchResponse instrumentSearchResponse : instrumentSearchResponseList) {
                InstrumentSearchAllStockResponse instrumentSearchAllStockResponse =
                        new InstrumentSearchAllStockResponse();
                instrumentSearchAllStockResponse.setExchange(instrumentSearchResponse.getExchange());
                instrumentSearchAllStockResponse.setTradingSymbol(instrumentSearchResponse.getTradingSymbol());
                instrumentSearchAllStockResponse.setFormattedInsName(instrumentSearchResponse.getFormattedInsName());
                instrumentSearchAllStockResponse.setExchangeSegment(instrumentSearchResponse.getExchangeSegment());
                instrumentSearchAllStockResponse.setToken(instrumentSearchResponse.getToken());
                instrumentSearchAllStockResponse.setLotSize(instrumentSearchResponse.getLotSize());
                instrumentSearchAllStockResponse.setTicSize(instrumentSearchResponse.getTicSize());
                instrumentSearchAllStockResponse.setExpiry(instrumentSearchResponse.getExpiry());
                Boolean checkFavourite = tokenCheckOnWatchlist(instrumentSearchResponse.getToken(), watchLists);
                if (checkFavourite.equals(true)) {
                    instrumentSearchAllStockResponse.setIsFavourite(true);
                } else {
                    instrumentSearchAllStockResponse.setIsFavourite(false);
                }
                instrumentSearchAllStockResponseList.add(instrumentSearchAllStockResponse);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "All Stock Response List",
                            AlgoticUtils.objectToJsonString(instrumentSearchResponseList),
                            jwtHelper.getUserId(),
                            "200"));
            return new ResponseEntity<>(instrumentSearchAllStockResponseList, HttpStatus.OK);
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

    private String[] searchFilter(String instrumentType, String exchange) {
        String[] exchanges;
        if (InstrumentType.STOCK.name().equalsIgnoreCase(instrumentType)) {
            if (Exchange.NSE.name().equalsIgnoreCase(exchange)) {
                exchanges = new String[] {Exchange.NSE.name()};
            } else if (Exchange.BSE.name().equalsIgnoreCase(exchange)) {
                exchanges = new String[] {Exchange.BSE.name()};
            } else {
                exchanges = new String[] {Exchange.NSE.name(), Exchange.BSE.name()};
            }
        } else if (InstrumentType.OPTIONS.name().equalsIgnoreCase(instrumentType)
                || InstrumentType.FUTURES.name().equalsIgnoreCase(instrumentType)) {
            if (Exchange.NSE.name().equalsIgnoreCase(exchange)) {
                exchanges = new String[] {Exchange.NFO.name(), Exchange.INDICES.name()};
            } else if (Exchange.BSE.name().equalsIgnoreCase(exchange)) {
                exchanges = new String[] {Exchange.BFO.name(), Exchange.INDICES.name()};
            } else {
                exchanges = new String[] {Exchange.NFO.name(), Exchange.BFO.name(), Exchange.INDICES.name()};
            }
        } else {
            if (Exchange.NSE.name().equalsIgnoreCase(exchange)) {
                exchanges = new String[] {Exchange.NSE.name(), Exchange.NFO.name(), Exchange.INDICES.name()};
            } else if (Exchange.BSE.name().equalsIgnoreCase(exchange)) {
                exchanges = new String[] {Exchange.BSE.name(), Exchange.BFO.name(), Exchange.INDICES.name()};
            } else {
                exchanges = new String[] {
                    Exchange.NSE.name(),
                    Exchange.NFO.name(),
                    Exchange.BSE.name(),
                    Exchange.BFO.name(),
                    Exchange.INDICES.name()
                };
            }
        }
        return exchanges;
    }

    private List<InstrumentSearchResponse> instrumentSearchResponseListData(
            String search, String[] exchanges, Integer limit) {

        //        InstrumentSearchRequest instrumentSearchRequest = new InstrumentSearchRequest();
        //        if (search.isEmpty()) {
        //            instrumentSearchRequest.setSearch("NIFTY");
        //        } else {
        //            instrumentSearchRequest.setSearch(search);
        //        }
        //        instrumentSearchRequest.setExchange(exchanges);
        //        List<InstrumentSearchResponse> instrumentSearchResponseList =
        //                aliceBlueProvider.getInstrumentNameBySearch(instrumentSearchRequest);
        List<InstrumentSearchResponse> instrumentSearchResponseList = new ArrayList<>();

        List<Instruments> instruemmentList = instrumentsRepo.getInstruments(search);

        for (Instruments instruments : instruemmentList) {
            InstrumentSearchResponse instrumentSearchResponse = new InstrumentSearchResponse();
            instrumentSearchResponse.setExchange(instruments.getExchange());
            instrumentSearchResponse.setToken(instruments.getToken());
            instrumentSearchResponse.setTradingSymbol(instruments.getTradingSymbol());
            instrumentSearchResponse.setExchangeSegment(instruments.getExchangeSegment());
            instrumentSearchResponse.setFormattedInsName(instruments.getFormatedInsName());
            instrumentSearchResponseList.add(instrumentSearchResponse);
        }

        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Instrument Response list from alice blue",
                        AlgoticUtils.objectToJsonString(instrumentSearchResponseList),
                        jwtHelper.getUserId(),
                        null));
        if (limit == null) {
            limit = 10;
        }
        if (limit > instrumentSearchResponseList.size()) {
            limit = instrumentSearchResponseList.size();
        }
        instrumentSearchResponseList =
                instrumentSearchResponseList.stream().limit(limit).toList();
        return instrumentSearchResponseList;
    }

    private Boolean tokenCheckOnWatchlist(String token, List<InstrumentWatchLists> watchLists) {
        try {
            InstrumentWatchLists insWatchListData = watchLists.stream()
                    .filter(v -> v.getToken().equals(token))
                    .findAny()
                    .orElse(null);
            if (insWatchListData != null) {
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "All Stock Response List",
                                AlgoticUtils.objectToJsonString(insWatchListData),
                                jwtHelper.getUserId(),
                                null));
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public ResponseEntity<WebSocketResponse> getWebsocketData() {
        try {
            log.info(logConfig.getLogHandler().getInfoLog("WebSocket Data", "Request for web socket data", null, null));

            String sessionId = "";
            String customerDetailsReference = "";
            String brokerName = null;
            WebSocketResponse webSocketResponse = new WebSocketResponse();
            String brokerSessionId = sesseionIdbybroker();

            if (brokerSessionId != null) {
                try {
                    sessionId = brokerSessionId;
                    BrokerCustomerDetails brokerCustomerDetails =
                            brokerCustomerDetailsRepo.findBrokerUserById(jwtHelper.getUserId());

                    if (brokerCustomerDetails != null) {
                        customerDetailsReference = brokerCustomerDetails.getReferenceID();
                        brokerName = getBrokerName(brokerCustomerDetails.getBrokerId());
                    }

                    if (BrokerEnum.ALICE_BLUE.getBrokerName().equals(brokerName)) {
                        String brokerToken = userService.getAliceBlueToken(jwtHelper.getUserId());

                        log.info(logConfig
                                .getLogHandler()
                                .getInfoLog(
                                        "Broker token",
                                        AlgoticUtils.objectToJsonString(brokerToken),
                                        jwtHelper.getUserId(),
                                        null));
                        aliceblueWebSocketProvider.getWebsocketSession(brokerToken);

                    } else if (StringUtils.equalsAny(
                            brokerName,
                            BrokerEnum.MOTILAL_OSWAL.getBrokerName(),
                            BrokerEnum.PAYTM_MONEY.getBrokerName())) {

                        sessionId = encrypSessionId(paperTradeUserId);
                        customerDetailsReference = paperTradeUserId;
                    }

                    webSocketResponse.setBrokerName(brokerName);
                    webSocketResponse.setPaperTrade(false);
                } catch (AlgoticException ex) {
                    log.error(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    ex.getErrorCode().getErrorCode(), ex.getMessage(), jwtHelper.getUserId(), null));
                    sessionId = encrypSessionId(paperTradeUserId);
                    customerDetailsReference = paperTradeUserId;
                    webSocketResponse.setPaperTrade(true);
                }
            } else {
                sessionId = encrypSessionId(paperTradeUserId);
                customerDetailsReference = paperTradeUserId;
                webSocketResponse.setPaperTrade(true);
                webSocketResponse.setBrokerConnected(false);
            }

            webSocketResponse.setTick("c");
            webSocketResponse.setActId(customerDetailsReference + "_API");
            webSocketResponse.setUserId(customerDetailsReference + "_API");
            webSocketResponse.setSource("API");
            if (sessionId != null) {
                webSocketResponse.setWebsocketSession(AlgoticUtils.getSHA(AlgoticUtils.getSHA(sessionId)));
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Web Socket Data",
                            AlgoticUtils.objectToJsonString(webSocketResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(webSocketResponse, HttpStatus.OK);

        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception ex) {
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
    }

    private String getBrokerName(Integer brokerId) {
        return brokersRepo.findById(brokerId).orElse(new Brokers()).getName();
    }

    private String sesseionIdbybroker() {
        try {
            String sesseionIdData = userService.getBrokerSessionId(jwtHelper.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "sessionIdbyBroker",
                            AlgoticUtils.objectToJsonString(sesseionIdData),
                            jwtHelper.getUserId(),
                            null));

            if (StringUtils.isNotEmpty(sesseionIdData)) {
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                " return sessionIdbyBroker",
                                AlgoticUtils.objectToJsonString(sesseionIdData),
                                null,
                                null));

                return sesseionIdData;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private String encrypSessionId(String brokerUserId) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Encryption Session id", "Request for encryption session id", null, null));

            EncApiRequest encApiRequest = new EncApiRequest();
            encApiRequest.setUserId(brokerUserId);
            EncApiResponse encApiResponse = aliceBlueProvider.encryptionApiKey(encApiRequest);

            String userKey = brokerUserId + apiKey + encApiResponse.getEncKey();
            String userDataSha = AlgoticUtils.getSHA(userKey);

            EncApiSessionIdRequest encApiSessionIdRequest = new EncApiSessionIdRequest();
            encApiSessionIdRequest.setUserId(encApiRequest.getUserId());
            encApiSessionIdRequest.setUserData(userDataSha);
            EncApiSessionIdResponse encApiSessionIdResponse =
                    aliceBlueProvider.encrypApiSessionId(encApiSessionIdRequest);
            String encrypSessionIdValue = encApiSessionIdResponse.getSessionId();

            if (!encrypSessionIdValue.isEmpty()) {
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "encrypApiSessionId",
                                AlgoticUtils.objectToJsonString(encrypSessionIdValue),
                                jwtHelper.getUserId(),
                                null));
                return encrypSessionIdValue;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String aliceBlueToken(String userId) {
        String userID = brokerCustomerDetailsRepo.findBrokerUserById(userId).getReferenceID();
        log.info(logConfig.getLogHandler().getInfoLog("User Id", AlgoticUtils.objectToJsonString(userID), null, null));
        String sessionId = brokerSessionDetailsRepo.getSessionId(userId).getSessionId();
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Session ID", AlgoticUtils.objectToJsonString(sessionId), null, null));
        if (sessionId == null) {
            BusinessErrorCode errorCode = BusinessErrorCode.SESSION_ID_NOT_EXISTS;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
        return userID + " " + sessionId;
    }

    @Override
    public Result instrumentHistory(AlgoticInstrumentRequest algoticInstrumentRequest) {
        try {
            log.info(logConfig.getLogHandler().getInfoLog("Instrument History", "Instrument History ", null, null));
            String userId = jwtHelper.getUserId();
            String token = aliceBlueToken(userId);

            InstrumentHistoryRequest instrumentHistoryRequest = new InstrumentHistoryRequest();
            List<InstrumentHistoryResponse> instrumentHistoryResponse = new ArrayList<>();

            instrumentHistoryRequest.setExchange(algoticInstrumentRequest.getExchange());
            instrumentHistoryRequest.setToken(algoticInstrumentRequest.getToken());
            instrumentHistoryRequest.setFrom(String.valueOf(currentTimeMillis() - (60 * 60 * 24 * 1000)));
            instrumentHistoryRequest.setTo(String.valueOf(currentTimeMillis()));
            instrumentHistoryRequest.setResolution("d");
            InstrumentHistoryResponse instrumentHistoryList =
                    aliceBlueProvider.instrumentHistory(instrumentHistoryRequest, token);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "instrument History List",
                            AlgoticUtils.objectToJsonString(instrumentHistoryList),
                            jwtHelper.getUserId(),
                            null));
            Result result = instrumentHistoryList.getResult().get(0);
            instrumentHistoryResponse.add(instrumentHistoryList);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Instrument History ",
                            AlgoticUtils.objectToJsonString(instrumentHistoryResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return result;
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

    @Override
    public ResponseEntity<List<AlgoticResultResponse>> getInstrumentList(
            List<AlgoticInstrumentRequest> algoticInstrumentRequest) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("get Instrument List", "Instrumnet List", jwtHelper.getUserId(), null));
            List<AlgoticResultResponse> resultList = new ArrayList<>();

            InstrumentHistoryRequest instrumentHistoryRequest = new InstrumentHistoryRequest();

            for (AlgoticInstrumentRequest algoticInstrument : algoticInstrumentRequest) {
                instrumentHistoryRequest.setExchange(algoticInstrument.getExchange());
                instrumentHistoryRequest.setToken(algoticInstrument.getToken());
                instrumentHistoryRequest.setFrom(String.valueOf(currentTimeMillis() - (60 * 60 * 24 * 1000)));
                instrumentHistoryRequest.setTo(String.valueOf(currentTimeMillis()));
                instrumentHistoryRequest.setResolution("d");
                String userId = jwtHelper.getUserId();
                String token = aliceBlueToken(userId);
                InstrumentHistoryResponse instrumentHistoryList =
                        aliceBlueProvider.getInstrumentList(instrumentHistoryRequest, token);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "get Instrument List",
                                AlgoticUtils.objectToJsonString(instrumentHistoryList),
                                jwtHelper.getUserId(),
                                null));

                for (Result result : instrumentHistoryList.getResult()) {
                    AlgoticResultResponse algoticResultResponse = new AlgoticResultResponse();
                    algoticResultResponse.setClose(result.getClose());
                    algoticResultResponse.setHigh(result.getHigh());
                    algoticResultResponse.setLow(result.getLow());
                    algoticResultResponse.setVolume(result.getVolume());
                    algoticResultResponse.setOpen(result.getOpen());
                    algoticResultResponse.setTime(result.getTime());
                    resultList.add(algoticResultResponse);
                }
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Instrument history list",
                            AlgoticUtils.objectToJsonString(resultList),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(resultList, HttpStatus.OK);

        } catch (AlgoticException ex) {
            throw ex;
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
