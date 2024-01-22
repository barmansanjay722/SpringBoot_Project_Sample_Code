package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.*;
import com.algotic.data.entities.*;
import com.algotic.data.repositories.*;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.TradeRequest;
import com.algotic.model.response.*;
import com.algotic.services.TradeService;
import com.algotic.utils.AlgoticUtils;
import jakarta.transaction.Transactional;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TradeServiceImpl implements TradeService {
    @Autowired
    private TradesRepo tradesRepo;

    @Autowired
    private TradeExecutionRepo tradeExecutionRepo;

    @Autowired
    private StrategiesRepo strategiesRepo;

    @Autowired
    private BrokerCustomerDetailsRepo brokerCustomerDetailsRepo;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Value("${webhookUrl}")
    private String webhookUrl;

    @Value("${algoticEncryptionSalt}")
    private String algoticEncryptionSalt;

    @Value("${algoticEncryptionSecretKey}")
    private String algoticEncryptionSecretKey;

    @Override
    public ResponseEntity<TradeWebhookResponse> saveTrade(TradeRequest tradeRequest) {
        TradeWebhookResponse tradeWebhookResponse = new TradeWebhookResponse();
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Save Trade",
                            "Save the Trades",
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            BrokerCustomerDetails brokerCustomerDetails =
                    brokerCustomerDetailsRepo.findBrokerUserById(jwtHelper.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Broker Customer Details",
                            AlgoticUtils.objectToJsonString(brokerCustomerDetails),
                            jwtHelper.getUserId(),
                            null));
            if (brokerCustomerDetails == null && tradeRequest.getTradeType().equalsIgnoreCase(TradeType.LIVE.name())) {
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
            if (tradeRequest.getStrategyId() != null) {
                Strategies strategy = strategiesRepo.findByIdAndIsActive(tradeRequest.getStrategyId());
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Strategy", AlgoticUtils.objectToJsonString(strategy), jwtHelper.getUserId(), null));
                if (strategy == null) {
                    BusinessErrorCode errorCode = BusinessErrorCode.STRATEGY_NOT_EXISTS;
                    log.error(logConfig
                            .getLogHandler()
                            .getErrorLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    jwtHelper.getUserId(),
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                }
                tradeWebhookResponse.setStrategyScript(strategy.getScript());
            } else {
                tradeWebhookResponse.setStrategyScript(null);
            }

            Trades trades = new Trades();
            String tradeId = AlgoticUtils.generateUUID();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Trade Id", AlgoticUtils.objectToJsonString(tradeId), jwtHelper.getUserId(), null));
            trades.setId(tradeId);

            if (tradeRequest.getTradeType().equalsIgnoreCase(TradeType.LIVE.name())) {
                trades.setTradeType(AlgoticUtils.convertToPascalCase(TradeType.LIVE.name()));
            } else {
                trades.setTradeType(AlgoticUtils.convertToPascalCase(TradeType.PAPER.name()));
            }

            if (tradeRequest.getStockType().equalsIgnoreCase("Stock")) {
                trades.setStockType(InstrumentType.STOCK.name());
            } else if (tradeRequest.getStockType().equalsIgnoreCase("Options")) {
                trades.setStockType(InstrumentType.OPTIONS.name());
            } else {
                trades.setStockType(InstrumentType.FUTURES.name());
            }

            trades.setInstrumentName(tradeRequest.getInstrumentName());
            trades.setTradingSymbol(tradeRequest.getTradingSymbol());
            trades.setExchange(tradeRequest.getExchange());

            if (tradeRequest.getStrategyId() != null) {
                trades.setStrategyId(tradeRequest.getStrategyId());
            }
            trades.setLotSize(tradeRequest.getLotSize());
            trades.setStopLossPrice(tradeRequest.getStopLossPrice());
            trades.setTargetProfit(tradeRequest.getTargetProfit());

            if (tradeRequest.getOrderType().equalsIgnoreCase("MIS")) {
                trades.setOrderType(AlgoticProductCode.MIS.name());
            } else if (tradeRequest.getOrderType().equalsIgnoreCase("CNC")) {
                trades.setOrderType(AlgoticProductCode.CNC.name());
            } else {
                if (tradeRequest.getOrderType().equalsIgnoreCase(AlgoticProductCode.NORMAL.name())) {
                    trades.setOrderType("NRML");
                }
            }

            trades.setUserId(jwtHelper.getUserId());
            trades.setBrokerCustomerDetailId(brokerCustomerDetails == null ? null : brokerCustomerDetails.getId());
            trades.setToken(tradeRequest.getToken());
            trades.setIsActive(true);
            trades.setIsDeleted(false);
            trades.setCreatedAt(new Date());
            tradesRepo.save(trades);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Trade Save in Database",
                            AlgoticUtils.objectToJsonString(trades),
                            jwtHelper.getUserId(),
                            null));

            String hookUrl = webhookUrlGeneration(tradeId, jwtHelper.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Webhook Url", AlgoticUtils.objectToJsonString(hookUrl), jwtHelper.getUserId(), null));

            tradeWebhookResponse.setBuyAlertMessage("{\n" + "    \"transactionType\": \"Buy\"\n" + "}");
            tradeWebhookResponse.setWebhookURL(hookUrl);
            tradeWebhookResponse.setSellAlertMessage("{\n" + "    \"transactionType\": \"Sell\"\n" + "}");
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Save Trade",
                            AlgoticUtils.objectToJsonString(tradeWebhookResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(tradeWebhookResponse, HttpStatus.OK);
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

    public String webhookUrlGeneration(String tradeId, String userId) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Webhook Url", "Generate Webhook Url", jwtHelper.getUserId(), null));
        String webhookUrlValue = userId + "@" + tradeId;
        String encryptWebhookData =
                AlgoticUtils.encrypt(webhookUrlValue, algoticEncryptionSecretKey, algoticEncryptionSalt);
        String encodedUrl = Base64.getUrlEncoder().encodeToString(encryptWebhookData.getBytes());
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Encoded Url", AlgoticUtils.objectToJsonString(encodedUrl), jwtHelper.getUserId(), null));
        return webhookUrl + encodedUrl;
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> tradeActiveInactive(String id, String type) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Trade Active Inactive", "Get the Trade Active Inactive ", jwtHelper.getUserId(), null));
            GlobalMessageResponse globalMessageResponse = new GlobalMessageResponse();
            Trades trade = tradesRepo.findTradeById(id);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Trade", AlgoticUtils.objectToJsonString(trade), jwtHelper.getUserId(), null));
            if (trade == null) {
                BusinessErrorCode errorCode = BusinessErrorCode.TRADE_NOT_EXISTS;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            } else if (AlgoticStatus.ACTIVE.name().equalsIgnoreCase(type)) {
                trade.setIsActive(true);
                tradesRepo.save(trade);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog("Save Trade", AlgoticUtils.objectToJsonString(trade), jwtHelper.getUserId(), null));
                globalMessageResponse.setMessage("Trade Active");
                return new ResponseEntity<>(globalMessageResponse, HttpStatus.OK);
            } else if (AlgoticStatus.INACTIVE.name().equalsIgnoreCase(type)) {
                trade.setIsActive(false);
                tradesRepo.save(trade);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog("Save Trade", AlgoticUtils.objectToJsonString(trade), jwtHelper.getUserId(), null));
                globalMessageResponse.setMessage("Trade Inactive");
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Trade Active Inactive",
                                AlgoticUtils.objectToJsonString(globalMessageResponse),
                                jwtHelper.getUserId(),
                                String.valueOf(HttpStatus.OK.value())));
                return new ResponseEntity<>(globalMessageResponse, HttpStatus.OK);
            } else {
                BusinessErrorCode errorCode = BusinessErrorCode.STATUS_NOT_EXISTS;
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

    @Override
    public ResponseEntity<TradeSetupResponse> getTrades(int limit, int offset) {
        String userId = jwtHelper.getUserId();
        try {
            log.info(logConfig.getLogHandler().getInfoLog("Trades", "Get Trades", jwtHelper.getUserId(), null));
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
            List<TradesResponse> tradeResponseList = new ArrayList<>();
            TradeSetupResponse response = new TradeSetupResponse();
            Integer count = tradesRepo.getAllTradesCount(userId);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Trades Count", AlgoticUtils.objectToJsonString(count), jwtHelper.getUserId(), null));
            response.setTotal(count);
            List<Trades> tradesList = tradesRepo.findAllTrades(userId, limitValue, offsetValue);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Trades List", AlgoticUtils.objectToJsonString(tradesList), jwtHelper.getUserId(), null));
            if (!tradesList.isEmpty()) {
                for (Trades trades : tradesList) {
                    TradesResponse tradeResponse = new TradesResponse();
                    Strategies strategy = strategiesRepo.findByIdAndIsDeleted(trades.getStrategyId(), false);
                    tradeResponse.setId(trades.getId());
                    tradeResponse.setInstrument(trades.getInstrumentName());
                    tradeResponse.setTradeType(trades.getTradeType());
                    tradeResponse.setStockType(trades.getStockType());
                    tradeResponse.setExchange(trades.getExchange());
                    tradeResponse.setIsActive(trades.getIsActive());
                    tradeResponse.setLotSize(trades.getLotSize());
                    if (strategy == null) {
                        tradeResponse.setStrategyName(null);
                        tradeResponse.setStragegyIsActive(false);
                    } else {
                        tradeResponse.setStrategyName(strategy.getName());
                        tradeResponse.setStragegyIsActive(strategy.getIsActive());
                    }
                    tradeResponse.setOrderType(trades.getOrderType());
                    tradeResponse.setStopLossPrice(trades.getStopLossPrice());
                    tradeResponse.setTargetProfit(trades.getTargetProfit());
                    tradeResponseList.add(tradeResponse);
                }
                response.setResult(tradeResponseList);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Trades Details ",
                                AlgoticUtils.objectToJsonString(tradeResponseList),
                                userId,
                                String.valueOf(HttpStatus.OK.value())));
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                BusinessErrorCode errorCode = BusinessErrorCode.TRADE_NOT_EXISTS;
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
    public ResponseEntity<GlobalMessageResponse> deleteTrade(String id) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Delete Trade", "Delete Trade by Id", jwtHelper.getUserId(), null));
            GlobalMessageResponse globalMessageResponse = new GlobalMessageResponse();
            Trades trades = tradesRepo.findTradeById(id);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Trade by ID", AlgoticUtils.objectToJsonString(trades), jwtHelper.getUserId(), null));
            if (trades != null) {
                trades.setIsActive(false);
                trades.setIsDeleted(true);
                tradesRepo.save(trades);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Trade delete change in database",
                                AlgoticUtils.objectToJsonString(trades),
                                jwtHelper.getUserId(),
                                null));
                globalMessageResponse.setMessage("User deleted");
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Trades Details ",
                                AlgoticUtils.objectToJsonString(globalMessageResponse),
                                id,
                                String.valueOf(HttpStatus.OK.value())));
                return new ResponseEntity<>(globalMessageResponse, HttpStatus.OK);
            }
            BusinessErrorCode errorCode = BusinessErrorCode.TRADE_NOT_EXISTS;
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
    @Transactional
    public ResponseEntity<TradeExecutionResponse> tradeExecution(Integer limit, Integer offset) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Trade Execution Reports",
                            "Get Trades Execution reports data",
                            jwtHelper.getUserId(),
                            null));
            int limitValue = limit == null ? 5 : limit;
            int offsetValue = offset == null ? 0 : offset;
            if (limitValue < 0 || offsetValue < 0) {
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
            List<TradeExecutions> tradeExecutions = tradeExecutionRepo.tradeExecution(limitValue, offsetValue);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Trade Executions",
                            AlgoticUtils.objectToJsonString(tradeExecutions),
                            jwtHelper.getUserId(),
                            null));
            Object[] tradeCount = tradeExecutionRepo.getTradesExecutionCount();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Trade Count", AlgoticUtils.objectToJsonString(tradeCount), jwtHelper.getUserId(), null));
            TradeExecutionResponse response = new TradeExecutionResponse();
            response.setTotal(Integer.parseInt(tradeCount[0].toString()));
            response.setResult(tradeExecutions);
            if (tradeExecutions.isEmpty()) {
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
                            "Active user history",
                            AlgoticUtils.objectToJsonString(response),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(response, HttpStatus.OK);

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
