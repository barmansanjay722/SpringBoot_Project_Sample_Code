package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.broker.api.AliceBlueBrokerApi;
import com.algotic.config.JwtHelper;
import com.algotic.constants.*;
import com.algotic.constants.aliceblue.Complexity;
import com.algotic.data.entities.*;
import com.algotic.data.repositories.*;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.OrderRequest;
import com.algotic.model.request.aliceblue.ChartHistoryRequest;
import com.algotic.model.request.aliceblue.EncApiRequest;
import com.algotic.model.request.aliceblue.EncApiSessionIdRequest;
import com.algotic.model.response.AlgoticSquareOffResponse;
import com.algotic.model.response.BookPositionResponse;
import com.algotic.model.response.HoldingAlgoticResponse;
import com.algotic.model.response.OrderAndTradeBookResponse;
import com.algotic.model.response.aliceblue.*;
import com.algotic.services.PaperOrderService;
import com.algotic.utils.AlgoticUtils;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PaperOrderImpl implements PaperOrderService {
    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private PaperOrderRepo paperOrderRepo;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Value("${paperTradeUserId}")
    private String paperTradeUserId;

    @Value("${apiKey}")
    private String apiKey;

    @Value("${nseUrl}")
    private String nseContractUrl;

    @Value(("${nfoUrl}"))
    private String nfoContractUrl;

    @Value(("${bseUrl}"))
    private String bseContractUrl;

    @Value(("${bfoUrl}"))
    private String bfoContractUrl;

    @Value(("${indicesUrl}"))
    private String indicesContractUrl;

    @Autowired
    private AliceBlueBrokerApi brokerApi;

    @Autowired
    private PaperHoldingDetailsRepo paperHoldingDetailsRepo;

    @Autowired
    private PaperPositionRepo paperPositionRepo;

    @Autowired
    private InstrumentWatchListsRepo instrumentWatchListsRepo;

    @Autowired
    private InstrumentsRepo instrumentsRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public PaperOrders paperOrder(OrderRequest orderRequest, String schedulerUserId) {
        try {
            Integer qty = 0;
            PaperOrders paperOrders = new PaperOrders();
            String id = AlgoticUtils.generateUUID();

            String customerUserId = "";
            Boolean holdingHandled = false;

            if (schedulerUserId != null) {
                customerUserId = schedulerUserId;
            } else {
                customerUserId = jwtHelper.getUserId();
            }
            paperOrders.setExpiry(orderRequest.getExpiry());
            if (Boolean.TRUE.equals(orderRequest.getIsHolding())
                    && orderRequest.getProductCode().equalsIgnoreCase(AlgoticProductCode.CNC.name())
                    && orderRequest.getTransactionType().equalsIgnoreCase(TransactionType.SELL.name())) {
                holdingHandled = handleHolding(
                        orderRequest.getTradingSymbol(),
                        orderRequest.getToken(),
                        customerUserId,
                        orderRequest.getQuantity());

                paperOrders.setIsHolding(true);
            } else {
                paperOrders.setIsHolding(false);
                orderRequest.setIsHolding(false);
            }

            paperOrders.setUserId(customerUserId);
            paperOrders.setExchange(orderRequest.getExchange());

            paperOrders.setToken(orderRequest.getToken());
            Double sellPrice = chartHistoryResponseData(orderRequest.getToken(), orderRequest.getExchange());
            if (sellPrice == null) {
                if (orderRequest.getPrice() == null) {
                    throw new AlgoticException(CommonErrorCode.PAPER_TRADE_ERROR);
                }
                paperOrders.setPrice(orderRequest.getPrice());
            } else {
                paperOrders.setPrice(sellPrice);
            }
            paperOrders.setId(id);
            paperOrders.setTradingSymbol(orderRequest.getTradingSymbol());
            paperOrders.setInstrumentName(orderRequest.getInstrumentName());
            paperOrders.setPriceType(PriceType.MARKET.name());
            paperOrders.setQuantity(orderRequest.getQuantity());
            if (orderRequest.getProductCode().equalsIgnoreCase(AlgoticProductCode.NORMAL.name())) {
                orderRequest.setProductCode("NRML");
            }
            paperOrders.setProductCode(orderRequest.getProductCode());
            paperOrders.setTransactionType(orderRequest.getTransactionType());
            String userId = schedulerUserId == null ? jwtHelper.getUserId() : schedulerUserId;
            if (Boolean.TRUE.equals(holdingHandled)) {
                qty = orderRequest.getQuantity();
            } else {
                qty = paperOrderRepo.paperOrderQty(userId, orderRequest.getTradingSymbol());
                if (qty == null) {
                    qty = 0;
                }
            }
            if (Boolean.TRUE.equals(orderRequest.getIsHolding())) {
                if (Boolean.FALSE.equals(holdingHandled)
                        && orderRequest.getTransactionType().equalsIgnoreCase(TransactionType.SELL.name())) {
                    paperOrders.setStatus(PaperOrderStatus.REJECTED.name());
                } else {
                    paperOrders.setStatus(PaperOrderStatus.COMPLETE.name());
                }
            } else {
                if (orderRequest.getTransactionType().equalsIgnoreCase(TransactionType.SELL.name())) {
                    if (orderRequest.getProductCode().equalsIgnoreCase(AlgoticProductCode.MIS.name())
                            || orderRequest.getProductCode().equalsIgnoreCase("NRML")) {
                        paperOrders.setStatus(PaperOrderStatus.COMPLETE.name());
                    }
                    if (orderRequest.getProductCode().equalsIgnoreCase(AlgoticProductCode.CNC.name())) {
                        if (orderRequest.getQuantity() < qty) {
                            paperOrders.setStatus(PaperOrderStatus.COMPLETE.name());
                        } else {
                            paperOrders.setStatus(PaperOrderStatus.REJECTED.name());
                        }
                    }
                } else {
                    paperOrders.setStatus(PaperOrderStatus.COMPLETE.name());
                }
            }

            paperOrders.setTriggerPrice(orderRequest.getTriggerPrice());
            paperOrders.setPriceType(orderRequest.getPriceType());
            paperOrders.setTradeType(TradeType.PAPER.name());
            paperOrders.setCreatedAt(Instant.now());
            paperOrderRepo.save(paperOrders);

            return paperOrders;
        } catch (AlgoticException e) {
            throw e;
        } catch (Exception ex) {
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

    private Boolean handleHolding(String tradingSymbol, String token, String userId, Integer qty) {
        PaperHoldingDetails paperHoldingDetails = paperHoldingDetailsRepo.findHoldingData(tradingSymbol, token, userId);
        if (paperHoldingDetails != null) {
            if (qty <= paperHoldingDetails.getQty()) {
                Integer quantity = paperHoldingDetails.getQty() - qty;
                paperHoldingDetails.setQty(quantity);
                paperHoldingDetailsRepo.save(paperHoldingDetails);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<OrderAndTradeBookResponse> paperOrderBook(String userId, String type) {

        try {

            List<OrderAndTradeBookResponse> paperBookResponseList = new ArrayList<>();
            if ("tradeBook".equalsIgnoreCase(type) || ("executed".equalsIgnoreCase(type))) {

                List<PaperOrders> orderBook = paperOrderRepo.getOrders(userId, PaperOrderStatus.COMPLETE.name());
                if ("executed".equalsIgnoreCase(type)) {
                    orderBook.addAll(paperOrderRepo.getOrders(userId, PaperOrderStatus.REJECTED.name()));
                }
                for (PaperOrders orders : orderBook) {
                    if (!orderBook.isEmpty()) {
                        OrderAndTradeBookResponse paperOrder = new OrderAndTradeBookResponse();

                        paperOrder.setTime(AlgoticUtils.UTCtoIST(orders.getCreatedAt()));
                        if (orders.getTransactionType().equalsIgnoreCase(TransactionType.SQUAREOFF.name())) {
                            orders.setTransactionType(TransactionType.SELL.name());
                        }
                        paperOrder.setType(AlgoticUtils.convertToPascalCase(orders.getTransactionType()));
                        paperOrder.setTradingSymbol(orders.getTradingSymbol());
                        paperOrder.setInstrument(orders.getInstrumentName());
                        if (orders.getPriceType().equalsIgnoreCase(PriceType.MARKET.name())) {
                            orders.setPriceType("MKT");
                        }
                        if (orders.getProductCode().equalsIgnoreCase(AlgoticProductCode.NORMAL.name())) {
                            orders.setProductCode("NRML");
                        }
                        paperOrder.setProduct(orders.getProductCode() + " / " + orders.getPriceType());
                        paperOrder.setStatus(AlgoticUtils.convertToPascalCase(orders.getStatus()));

                        paperOrder.setExchange(orders.getExchange());
                        paperOrder.setQuantity(orders.getQuantity());
                        paperOrder.setTradeType(AlgoticUtils.convertToPascalCase(orders.getTradeType()));
                        paperOrder.setPrice(AlgoticUtils.commaFilter(String.valueOf(orders.getPrice())));
                        paperOrder.setNestOrderNumber(orders.getId());
                        paperBookResponseList.add(paperOrder);
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
                }
            } else if ("pending".equalsIgnoreCase(type)) {
                List<PaperOrders> orderBook = paperOrderRepo.getOrders(userId, PaperOrderStatus.OPEN.name());
                if (!orderBook.isEmpty()) {
                    for (PaperOrders orders : orderBook) {
                        OrderAndTradeBookResponse paperOrder = new OrderAndTradeBookResponse();
                        InstrumentWatchLists instrumentWatchLists =
                                instrumentWatchListsRepo.tokenbyUserId(userId, orders.getToken());

                        paperOrder.setTime(AlgoticUtils.UTCtoIST(orders.getCreatedAt()));
                        if (orders.getTransactionType().equalsIgnoreCase(TransactionType.SQUAREOFF.name())) {
                            orders.setTransactionType(TransactionType.SELL.name());
                        }

                        paperOrder.setType(AlgoticUtils.convertToPascalCase(orders.getTransactionType()));
                        paperOrder.setInstrument(instrumentWatchLists.getInstrumentName());
                        if (orders.getPriceType().equalsIgnoreCase(PriceType.MARKET.name())) {
                            orders.setPriceType("MKT");
                        }
                        if (orders.getProductCode().equalsIgnoreCase(AlgoticProductCode.NORMAL.name())) {

                            orders.setProductCode("NRML");
                        }
                        paperOrder.setProduct(orders.getProductCode() + " / " + orders.getPriceType());
                        paperOrder.setStatus(AlgoticUtils.convertToPascalCase(orders.getStatus()));
                        paperOrder.setExchange(orders.getExchange());
                        paperOrder.setTradeType(AlgoticUtils.convertToPascalCase(orders.getTradeType()));
                        paperOrder.setQuantity(orders.getQuantity());
                        paperOrder.setPrice(AlgoticUtils.commaFilter(String.valueOf(orders.getPrice())));
                        paperOrder.setNestOrderNumber(orders.getId());
                        paperBookResponseList.add(paperOrder);
                    }
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
            } else {
                List<PaperOrders> paperOrdersList = paperOrderRepo.findAllData(userId);
                for (PaperOrders paperOrders : paperOrdersList) {
                    OrderAndTradeBookResponse orderBook = new OrderAndTradeBookResponse();

                    orderBook.setTime(AlgoticUtils.UTCtoIST(paperOrders.getCreatedAt()));
                    if (paperOrders.getTransactionType().equalsIgnoreCase(TransactionType.SQUAREOFF.name())) {
                        paperOrders.setTransactionType(TransactionType.SELL.name());
                    }

                    orderBook.setType(AlgoticUtils.convertToPascalCase(paperOrders.getTransactionType()));

                    orderBook.setInstrument(paperOrders.getInstrumentName());
                    orderBook.setTradingSymbol(paperOrders.getTradingSymbol());

                    if (paperOrders.getPriceType().equalsIgnoreCase(PriceType.MARKET.name())) {
                        paperOrders.setPriceType("MKT");
                    }
                    if (paperOrders.getProductCode().equalsIgnoreCase(AlgoticProductCode.NORMAL.name())) {
                        paperOrders.setProductCode("NRML");
                    }

                    orderBook.setProduct(paperOrders.getProductCode() + " / " + paperOrders.getPriceType());
                    orderBook.setStatus(AlgoticUtils.convertToPascalCase(paperOrders.getStatus()));
                    orderBook.setExchange(paperOrders.getExchange());
                    orderBook.setTradeType(AlgoticUtils.convertToPascalCase(paperOrders.getTradeType()));
                    orderBook.setQuantity(paperOrders.getQuantity());
                    orderBook.setPrice(AlgoticUtils.commaFilter(String.valueOf(paperOrders.getPrice())));
                    orderBook.setNestOrderNumber(paperOrders.getId());
                    paperBookResponseList.add(orderBook);
                }
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Position book ",
                            AlgoticUtils.objectToJsonString(paperBookResponseList),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return paperBookResponseList;
        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
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

    @Override
    public List<HoldingAlgoticResponse> holdingsPaperTrade(String userId) {
        try {
            List<String> userIds = paperHoldingDetailsRepo.findByUserIdAndPdc(TransactionType.BUY.name(), userId);
            List<HoldingAlgoticResponse> holdingAlgoticResponseList = new ArrayList<>();
            for (String userID : userIds) {
                List<PaperHoldingDetails> paperOrderList = paperHoldingDetailsRepo.findByUserIdList(userID);
                if (paperOrderList != null) {
                    List<PaperHoldingDetails> paperHoldingDetailslist =
                            paperOrderList.stream().filter(s -> s.getQty() > 0).collect(Collectors.toList());
                    for (PaperHoldingDetails paperOrder : paperHoldingDetailslist) {
                        HoldingAlgoticResponse holdingAlgoticResponse = new HoldingAlgoticResponse();
                        holdingAlgoticResponse.setQuantity(String.valueOf(paperOrder.getQty()));
                        holdingAlgoticResponse.setPrice(AlgoticUtils.commaFilter(String.valueOf(
                                paperOrder.getAvgPrice() != null
                                        ? paperOrder.getAvgPrice().toString()
                                        : "0")));
                        holdingAlgoticResponse.setInstrumentName(paperOrder.getInstrumentName());
                        holdingAlgoticResponse.setTradingSymbol(paperOrder.getTradingSymbol());
                        holdingAlgoticResponse.setTradeType(
                                AlgoticUtils.convertToPascalCase(paperOrder.getTradeType()));
                        if (paperOrder.getProductCode().equalsIgnoreCase(AlgoticProductCode.NORMAL.name())) {
                            paperOrder.setProductCode("NRML");
                        }
                        Map<String, String> pdcLtpMap = getPdcAndLtp(paperOrder.getToken(), paperOrder.getExchange());
                        if (pdcLtpMap != null) {
                            holdingAlgoticResponse.setLtp(pdcLtpMap.get("ltp"));
                            holdingAlgoticResponse.setPdc(pdcLtpMap.get("pdc"));
                        } else {
                            holdingAlgoticResponse.setLtp(null);
                            holdingAlgoticResponse.setPdc(null);
                        }
                        holdingAlgoticResponse.setExchange(paperOrder.getExchange());
                        if (paperOrder.getExchange().equalsIgnoreCase(Exchange.NSE.name())
                                || paperOrder.getExchange().equalsIgnoreCase(Exchange.BSE.name())) {
                            holdingAlgoticResponse.setIsStock(true);
                        } else {
                            holdingAlgoticResponse.setIsStock(false);
                        }
                        holdingAlgoticResponse.setToken(paperOrder.getToken());
                        if (paperOrder.getProductCode().equalsIgnoreCase(AlgoticProductCode.NORMAL.name())) {
                            paperOrder.setProductCode("NRML");
                        }
                        holdingAlgoticResponse.setOrderType(paperOrder.getProductCode());
                        holdingAlgoticResponseList.add(holdingAlgoticResponse);
                    }
                }
            }

            return holdingAlgoticResponseList;
        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
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

    private String doubleToString(Double number) {
        return number == null ? "0" : String.valueOf(AlgoticUtils.round(number, 2));
    }

    private String integerToString(Integer number) {
        return number == null ? "0" : String.valueOf(number);
    }

    private Double calcProfitAndLoss(
            Integer quantity,
            Integer sellQuantity,
            Integer buyQuantity,
            Double buyAveragePrice,
            Double sellAveragePrice,
            Double ltp) {
        if (ltp != null) {
            if (quantity < 0) {
                return (quantity * (sellAveragePrice - ltp)) * -1
                        + (buyQuantity * (sellAveragePrice - buyAveragePrice));
            } else if (quantity > 0) {
                return (quantity * (ltp - buyAveragePrice)) + (sellQuantity * (sellAveragePrice - buyAveragePrice));
            } else {
                return buyQuantity * sellAveragePrice - buyQuantity * buyAveragePrice;
            }
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public List<BookPositionResponse> positionBook(String userId) {
        try {
            List<PaperPositions> paperPositionList = paperPositionRepo.getPaperPosition(userId);
            List<BookPositionResponse> positionBookResponseList = new ArrayList<>();
            if (paperPositionList != null) {

                for (PaperPositions paperPositions : paperPositionList) {
                    Double lastTradePrice =
                            chartHistoryResponseData(paperPositions.getToken(), paperPositions.getExchange());

                    BookPositionResponse bookPositionResponse = new BookPositionResponse();
                    bookPositionResponse.setNetSellAvgPrice(
                            AlgoticUtils.commaFilter(doubleToString(paperPositions.getSellAverage())));
                    bookPositionResponse.setNetBuyAvgPrice(
                            AlgoticUtils.commaFilter(doubleToString(paperPositions.getBuyAverage())));
                    bookPositionResponse.setSellAverage(
                            AlgoticUtils.commaFilter(doubleToString(paperPositions.getSellAverage())));
                    bookPositionResponse.setBuyAverage(
                            AlgoticUtils.commaFilter(doubleToString(paperPositions.getBuyAverage())));
                    bookPositionResponse.setOrderType(paperPositions.getProductCode());
                    bookPositionResponse.setNetQuantity(integerToString(paperPositions.getQuantity()));
                    bookPositionResponse.setExchange(paperPositions.getExchange());
                    bookPositionResponse.setInstrumentName(paperPositions.getInstrumentName());
                    bookPositionResponse.setTradingSymbol(paperPositions.getTradingSymbol());
                    if (paperPositions.getExchange().equalsIgnoreCase(Exchange.NSE.name())
                            || paperPositions.getExchange().equalsIgnoreCase(Exchange.BSE.name())) {
                        bookPositionResponse.setIsStock(true);
                    } else {
                        bookPositionResponse.setIsStock(false);
                    }

                    bookPositionResponse.setProfitAndLoss(AlgoticUtils.commaFilter(doubleToString(calcProfitAndLoss(
                            paperPositions.getQuantity(),
                            paperPositions.getSellQuantity(),
                            paperPositions.getBuyQuantity(),
                            paperPositions.getBuyAverage(),
                            paperPositions.getSellAverage(),
                            lastTradePrice))));

                    bookPositionResponse.setTradeType(paperPositions.getTradeType());
                    bookPositionResponse.setBlQty(integerToString(paperPositions.getBlQuantity()));
                    bookPositionResponse.setQuantity(integerToString(paperPositions.getQuantity()));
                    bookPositionResponse.setNetBuyQty(integerToString(paperPositions.getBuyQuantity()));
                    bookPositionResponse.setNetSellQty(integerToString(paperPositions.getSellQuantity()));
                    bookPositionResponse.setToken(paperPositions.getToken());
                    if (lastTradePrice == null) {
                        bookPositionResponse.setLastTradePrice(null);
                    } else {
                        bookPositionResponse.setLastTradePrice(
                                AlgoticUtils.commaFilter(doubleToString(lastTradePrice)));
                    }
                    positionBookResponseList.add(bookPositionResponse);
                }
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Position book ",
                            AlgoticUtils.objectToJsonString(positionBookResponseList),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return positionBookResponseList;
        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
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

    @Override
    public AlgoticSquareOffResponse paperOrderSquareOff(
            String token, String productCode, Double price, String schedulerUserId) {
        try {
            String userId = "";
            Integer sqrfqty = 0;
            Integer sqrOffqty = 0;
            Double sellPrice = 0.0;

            AlgoticSquareOffResponse algoticSquareOffResponse = new AlgoticSquareOffResponse();
            if (!productCode.equalsIgnoreCase("NRML") && token != null) {
                Integer buyqtysqrOff = paperOrderRepo.countOfTokenProductCode(
                        token, TransactionType.BUY.name(), productCode, schedulerUserId);
                Integer sellqtySqrOff = paperOrderRepo.countOfTokenProductCode(
                        token, TransactionType.SELL.name(), productCode, schedulerUserId);
                sqrOffqty = paperOrderRepo.countOfTokenProductCode(
                        token, TransactionType.SQUAREOFF.name(), productCode, schedulerUserId);
                if (sqrOffqty == null) {
                    sqrOffqty = 0;
                }
                if (sellqtySqrOff == null) {
                    sellqtySqrOff = 0;
                }
                if (buyqtysqrOff == null) {
                    buyqtysqrOff = 0;
                }
                Integer qty = buyqtysqrOff - sellqtySqrOff - sqrOffqty;

                if (schedulerUserId != null) {
                    userId = schedulerUserId;
                } else {
                    userId = jwtHelper.getUserId();
                }
                PaperOrders paperOrdersData = paperOrderRepo.findDatabyToken(token, userId);
                sellPrice = chartHistoryResponseData(paperOrdersData.getToken(), paperOrdersData.getExchange());
                if (sellPrice == null) {
                    sellPrice = price;
                }
                if (paperOrdersData != null) {
                    OrderRequest orderRequest = generatePaperOrderRequest(paperOrdersData, qty, sellPrice, productCode);
                    if (qty > 0) {
                        paperOrder(orderRequest, userId);
                    } else if (qty < 0) {
                        orderRequest.setTransactionType("BUY");
                        orderRequest.setQuantity(qty * -1);
                        paperOrder(orderRequest, userId);
                    }
                }
                algoticSquareOffResponse.setStatus("PaperOrder Completed");
                algoticSquareOffResponse.setMessage("SquareOff Completed");
            } else {
                Integer buyqtysqrOff = paperOrderRepo.countOfTokenNormal(
                        token, TransactionType.BUY.name(), productCode, schedulerUserId);
                Integer sellqtySqrOff = paperOrderRepo.countOfTokenNormal(
                        token, TransactionType.SELL.name(), productCode, schedulerUserId);
                sqrOffqty = paperOrderRepo.countOfTokenNormal(
                        token, TransactionType.SQUAREOFF.name(), productCode, schedulerUserId);
                if (sqrOffqty == null) {
                    sqrOffqty = 0;
                }
                if (sellqtySqrOff == null) {
                    sellqtySqrOff = 0;
                }
                if (buyqtysqrOff == null) {
                    buyqtysqrOff = 0;
                }
                Integer qty = buyqtysqrOff - sellqtySqrOff - sqrOffqty;

                if (schedulerUserId != null) {
                    userId = schedulerUserId;
                } else {
                    userId = jwtHelper.getUserId();
                }
                PaperOrders paperOrdersData = paperOrderRepo.findDatabyNormal(token, userId);
                sellPrice = chartHistoryResponseData(paperOrdersData.getToken(), paperOrdersData.getExchange());
                if (sellPrice == null) {
                    sellPrice = price;
                }
                if (paperOrdersData != null) {
                    OrderRequest orderRequest = generatePaperOrderRequest(paperOrdersData, qty, sellPrice, productCode);
                    if (qty > 0) {
                        paperOrder(orderRequest, userId);
                    } else if (qty < 0) {
                        orderRequest.setTransactionType("BUY");
                        orderRequest.setQuantity(qty * -1);
                        paperOrder(orderRequest, userId);
                    }
                }
                algoticSquareOffResponse.setStatus("PaperOrder Completed");
                algoticSquareOffResponse.setMessage("SquareOff Completed");
            }
            return algoticSquareOffResponse;
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

    private OrderRequest generatePaperOrderRequest(
            PaperOrders paperOrders, Integer qty, Double sellPrice, String productCode) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setComplexity(Complexity.REGULAR.name());
        orderRequest.setDiscloseQuantity(0);
        orderRequest.setProductCode(productCode);
        orderRequest.setPriceType(PriceType.MARKET.name());
        orderRequest.setPrice(sellPrice);
        orderRequest.setQuantity(qty);
        orderRequest.setTradingSymbol(paperOrders.getTradingSymbol());
        orderRequest.setInstrumentName(paperOrders.getInstrumentName());
        orderRequest.setTransactionType(TransactionType.SQUAREOFF.name());
        orderRequest.setTriggerPrice(0d);
        orderRequest.setExchange(paperOrders.getExchange());
        orderRequest.setToken(paperOrders.getToken());
        orderRequest.setTradeType(TradeType.PAPER.name());
        orderRequest.setExpiry(paperOrders.getExpiry());
        orderRequest.setIsHolding(false);
        return orderRequest;
    }

    public Map<String, String> getPdcAndLtp(String token, String exchange) {
        try {
            String sessionId = encrypSessionId(paperTradeUserId);
            Map<String, String> map = new HashMap<>();
            ChartHistoryRequest chartHistoryRequest = new ChartHistoryRequest();
            chartHistoryRequest.setToken(token);
            chartHistoryRequest.setExchange(exchange);
            chartHistoryRequest.setResolution("1");
            chartHistoryRequest.setFrom(System.currentTimeMillis());
            chartHistoryRequest.setTo(System.currentTimeMillis());
            ChartHistoryResponse chartHistoryResponse = brokerApi.chartHistory(chartHistoryRequest, sessionId);

            HistoryValue historyValue = chartHistoryResponse
                    .getHistoryValue()
                    .get(chartHistoryResponse.getHistoryValue().size() - 1);
            HistoryValue openHistoryValue =
                    chartHistoryResponse.getHistoryValue().get(0);
            Double lastPrice = historyValue.getClose();
            map.put("ltp", lastPrice.toString());
            map.put("pdc", openHistoryValue.getOpen().toString());
            return map;
        } catch (Exception e) {
            return null;
        }
    }

    private Double chartHistoryResponseData(String token, String exchange) {
        try {
            String sessionId = encrypSessionId(paperTradeUserId);
            Double lastPrice = 0.0;
            ChartHistoryRequest chartHistoryRequest = new ChartHistoryRequest();
            chartHistoryRequest.setToken(token);
            chartHistoryRequest.setExchange(exchange);
            chartHistoryRequest.setResolution("1");
            chartHistoryRequest.setFrom(System.currentTimeMillis());
            chartHistoryRequest.setTo(System.currentTimeMillis());
            ChartHistoryResponse chartHistoryResponse = brokerApi.chartHistory(chartHistoryRequest, sessionId);

            if (chartHistoryResponse.getHistoryValue() != null) {
                HistoryValue historyValue = chartHistoryResponse
                        .getHistoryValue()
                        .get(chartHistoryResponse.getHistoryValue().size() - 1);

                lastPrice = historyValue.getClose();
            } else {
                lastPrice = null;
            }

            return lastPrice;
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

    private String encrypSessionId(String brokerUserId) {
        try {
            EncApiRequest encApiRequest = new EncApiRequest();
            encApiRequest.setUserId(brokerUserId);
            EncApiResponse encApiResponse = brokerApi.encryptionApiKey(encApiRequest);

            String userKey = brokerUserId + apiKey + encApiResponse.getEncKey();
            String userDataSha = AlgoticUtils.getSHA(userKey);

            EncApiSessionIdRequest encApiSessionIdRequest = new EncApiSessionIdRequest();
            encApiSessionIdRequest.setUserId(encApiResponse.getUserId());
            encApiSessionIdRequest.setUserData(userDataSha);
            EncApiSessionIdResponse encApiSessionIdResponse = brokerApi.encrypApiSessionId(encApiSessionIdRequest);
            String encrypSessionIdValue = encApiSessionIdResponse.getSessionId();

            if (encrypSessionIdValue != null) {
                return encrypSessionIdValue;
            } else if (encrypSessionIdValue == null) {
                return null;
            } else {
                throw new AlgoticException(BusinessErrorCode.SESSION_ID_NOT_EXISTS);
            }
        } catch (Exception e) {
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AlgoticSquareOffResponse paperOrderSquareOffAll() {
        try {
            String userId = jwtHelper.getUserId();
            List<String> tokens = paperOrderRepo.getAllTokensForBuyAndSell(userId);

            for (String token : tokens) {
                List<String> productCodes = paperOrderRepo.findProductCodeByUserIdAndToken(userId, token);
                for (String pCode : productCodes) {
                    paperOrderSquareOff(token, pCode, null, userId);
                }
            }

            AlgoticSquareOffResponse squareOffResponse = new AlgoticSquareOffResponse();
            squareOffResponse.setStatus("Completed");
            squareOffResponse.setMessage("Paper Trade SquareOff Completed");
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "square off  paper trade",
                            AlgoticUtils.objectToJsonString(squareOffResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));

            return squareOffResponse;
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

    @Override
    public SquareOffResponse paperOrderScheduler(String userId) {
        try {
            List<String> paperOrderData = paperOrderRepo.findByMISTokens(userId);
            for (String paperOrder : paperOrderData)
                if (paperOrder != null) {
                    paperOrderSquareOff(paperOrder, AlgoticProductCode.MIS.name(), null, userId);
                }
            SquareOffResponse squareOffResponse = new SquareOffResponse();
            squareOffResponse.setMessage("PaperOrder SquareOff Scheduler");
            squareOffResponse.setStatus("Completed");

            return squareOffResponse;
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
    public void paperOrderHoldingScheduler(String userId) {
        try {
            log.info(logConfig.getLogHandler().getInfoLog("Paper Holding ", "Paper Holding Scheduler", userId, null));
            paperHoldingDetailsRepo.getHoldingsData(userId);
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

    private AlgoticSquareOffResponse paperOrderInstrumentSquareOff(
            String token, String productCode, String schedulerUserId) {
        try {
            String userId = "";
            Integer sqrfqty = 0;
            Integer sqrOffqty = 0;

            AlgoticSquareOffResponse algoticSquareOffResponse = new AlgoticSquareOffResponse();
            if (token != null && productCode != null) {
                Integer buyqtysqrOff = paperOrderRepo.countOfInstrumentTokenProductCode(
                        token, TransactionType.BUY.name(), productCode, schedulerUserId);
                Integer sellqtySqrOff = paperOrderRepo.countOfInstrumentTokenProductCode(
                        token, TransactionType.SELL.name(), productCode, schedulerUserId);
                sqrOffqty = paperOrderRepo.countOfInstrumentTokenProductCode(
                        token, TransactionType.SQUAREOFF.name(), productCode, schedulerUserId);
                if (sqrOffqty == null) {
                    sqrOffqty = 0;
                }
                if (sellqtySqrOff == null) {
                    sellqtySqrOff = 0;
                }
                if (buyqtysqrOff == null) {
                    buyqtysqrOff = 0;
                }
                Integer qty = buyqtysqrOff - sellqtySqrOff - sqrOffqty;

                if (schedulerUserId != null) {
                    userId = schedulerUserId;
                } else {
                    userId = jwtHelper.getUserId();
                }
                PaperOrders paperOrdersData = paperOrderRepo.findInstrumentByExchange(token, userId);
                Double sellPrice = chartHistoryResponseData(paperOrdersData.getToken(), paperOrdersData.getExchange());
                if (paperOrdersData != null) {
                    OrderRequest orderRequest = generatePaperOrderRequest(paperOrdersData, qty, sellPrice, productCode);
                    if (qty > 0) {
                        paperOrder(orderRequest, userId);
                    } else if (qty < 0) {
                        orderRequest.setTransactionType("BUY");
                        orderRequest.setQuantity(qty * -1);
                        paperOrder(orderRequest, userId);
                    }
                }
                algoticSquareOffResponse.setStatus("PaperOrder Completed");
                algoticSquareOffResponse.setMessage("SquareOff Completed");
            } else {
                Integer byqty = paperOrderRepo.countOfInstrumentToken(token, TransactionType.BUY.name());
                Integer sellqty = paperOrderRepo.countOfInstrumentToken(token, TransactionType.SELL.name());
                sqrfqty = paperOrderRepo.getInstrumentQty(token, TransactionType.SQUAREOFF.name());
                if (sqrfqty == null) {
                    sqrfqty = 0;
                }

                Integer qty = byqty - sellqty - sqrfqty;

                if (schedulerUserId != null) {
                    userId = schedulerUserId;
                } else {
                    userId = jwtHelper.getUserId();
                }
                PaperOrders paperOrdersData = paperOrderRepo.findDatabyInstrumentToken(token, userId);
                Double sellPrice = chartHistoryResponseData(paperOrdersData.getToken(), paperOrdersData.getExchange());
                if (paperOrdersData != null) {
                    OrderRequest orderRequest = generatePaperOrderRequest(paperOrdersData, qty, sellPrice, productCode);
                    if (qty > 0) {
                        paperOrder(orderRequest, userId);
                    }
                }
                algoticSquareOffResponse.setStatus("PaperOrder Completed");
                algoticSquareOffResponse.setMessage("SquareOff Completed");
            }
            return algoticSquareOffResponse;
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

    @Override
    public void paperOrderInstrumentScheduler(PaperOrders paperOrders) {
        try {
            if (paperOrders != null) {
                if (paperOrders.getExchange().equalsIgnoreCase("NFO")
                        || paperOrders.getExchange().equalsIgnoreCase("BFO")) {

                    paperOrderInstrumentSquareOff(
                            paperOrders.getToken(), paperOrders.getProductCode(), paperOrders.getUserId());
                }
            }

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
