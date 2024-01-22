package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.Exchange;
import com.algotic.data.entities.InstrumentWatchLists;
import com.algotic.data.repositories.InstrumentWatchListsRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.WatchList;
import com.algotic.model.response.InstrumentWatchlistResponse;
import com.algotic.model.response.WatchListRemoveResponse;
import com.algotic.model.response.WatchListStatusResponse;
import com.algotic.services.WatchlistService;
import com.algotic.utils.AlgoticUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InstrumentWatchlistServiceImpl implements WatchlistService {
    @Autowired
    private InstrumentWatchListsRepo instrumentWatchlistsRepo;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private JwtHelper jwtHelper;

    @Override
    public ResponseEntity<List<InstrumentWatchlistResponse>> saveWatchlist(List<WatchList> watchList) {
        String userId = jwtHelper.getUserId();
        try {
            log.info(
                    logConfig.getLogHandler().getInfoLog("Watch List", "Save Watch List", jwtHelper.getUserId(), null));
            if (watchList.isEmpty()) {
                CommonErrorCode errorCode = CommonErrorCode.BAD_REQUEST;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            List<InstrumentWatchLists> watchListInDb = instrumentWatchlistsRepo.findByUserId(jwtHelper.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Watch list in Database",
                            AlgoticUtils.objectToJsonString(watchListInDb),
                            jwtHelper.getUserId(),
                            null));
            List<WatchList> newWatchList = getWatchListToInsert(watchList, watchListInDb);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "New Watch List",
                            AlgoticUtils.objectToJsonString(newWatchList),
                            jwtHelper.getUserId(),
                            null));
            saveInstrumentWatchlist(newWatchList);
            List<InstrumentWatchLists> existingWatchList = getExistingWatchList(watchList, watchListInDb);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Get Existing watch list",
                            AlgoticUtils.objectToJsonString(existingWatchList),
                            jwtHelper.getUserId(),
                            null));
            updateInstrumentWatchList(existingWatchList);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "save instrument watchlist",
                            AlgoticUtils.objectToJsonString(generateWatchListResponse()),
                            userId,
                            String.valueOf(HttpStatus.CREATED.value())));
            return new ResponseEntity<>(generateWatchListResponse(), HttpStatus.CREATED);

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

    private List<InstrumentWatchlistResponse> generateWatchListResponse() {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Generate Watch List", "Generate Watch list Response", jwtHelper.getUserId(), null));
        List<InstrumentWatchlistResponse> instrumentWatchlistResponseList = new ArrayList<>();
        List<InstrumentWatchLists> watchlistRequest =
                instrumentWatchlistsRepo.getAllInstrumentData(jwtHelper.getUserId());
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Watch List Request",
                        AlgoticUtils.objectToJsonString(watchlistRequest),
                        jwtHelper.getUserId(),
                        null));
        if (!watchlistRequest.isEmpty()) {
            watchlistRequest.forEach(instrumentWatchLists -> {
                InstrumentWatchlistResponse watchlistResponse = new InstrumentWatchlistResponse();
                watchlistResponse.setId(instrumentWatchLists.getId());
                watchlistResponse.setTradingSymbol(instrumentWatchLists.getInstrumentTradingSymbol());
                watchlistResponse.setInstrumentName(instrumentWatchLists.getInstrumentName());
                watchlistResponse.setToken(instrumentWatchLists.getToken());
                watchlistResponse.setExchange(instrumentWatchLists.getExchange());
                watchlistResponse.setExpiry(instrumentWatchLists.getExpiry());
                if (instrumentWatchLists.getExchange().equalsIgnoreCase(Exchange.NSE.name())
                        || instrumentWatchLists.getExchange().equalsIgnoreCase(Exchange.BSE.name())) {
                    watchlistResponse.setIsStock(true);
                } else {
                    watchlistResponse.setIsStock(false);
                }
                instrumentWatchlistResponseList.add(watchlistResponse);
            });
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Instrument Watch List",
                            AlgoticUtils.objectToJsonString(instrumentWatchlistResponseList),
                            jwtHelper.getUserId(),
                            "200"));
            return instrumentWatchlistResponseList;
        }
        BusinessErrorCode errorCode = BusinessErrorCode.WATCHLIST_NOT_FOUND;
        log.error(logConfig
                .getLogHandler()
                .getErrorLog(
                        errorCode.getErrorCode(),
                        errorCode.getErrorMessage(),
                        jwtHelper.getUserId(),
                        String.valueOf(errorCode.getHttpStatus().value())));
        throw new AlgoticException(errorCode);
    }

    private List<InstrumentWatchLists> getExistingWatchList(
            List<WatchList> watchList, List<InstrumentWatchLists> watchListOld) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Existing watch list", "get Existing watch list", jwtHelper.getUserId(), null));
        return watchListOld.stream()
                .filter(oldInstrument -> watchList.stream()
                        .anyMatch(instrument -> oldInstrument.getToken().equals(instrument.getToken())))
                .toList();
    }

    private List<WatchList> getWatchListToInsert(List<WatchList> watchList, List<InstrumentWatchLists> watchListOld) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Get Watch List to insert", "Watch list to insert", jwtHelper.getUserId(), null));
        return watchList.stream()
                .filter(o -> watchListOld.stream().noneMatch(b -> b.getToken().equalsIgnoreCase(o.getToken())))
                .toList();
    }

    private void updateInstrumentWatchList(List<InstrumentWatchLists> watchList) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Update Instrumnet Watch List", "Update the Watch List", jwtHelper.getUserId(), null));
            if (!watchList.isEmpty()) {
                watchList.forEach(instrumentWatchLists -> {
                    watchList.forEach(instrumentWatchListsData -> {
                        instrumentWatchLists.setInstrumentTradingSymbol(
                                instrumentWatchLists.getInstrumentTradingSymbol());
                        instrumentWatchLists.setUserId(instrumentWatchLists.getUserId());
                        instrumentWatchLists.setIsActive(true);
                    });
                    instrumentWatchlistsRepo.save(instrumentWatchLists);
                });
            }
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

    private void saveInstrumentWatchlist(List<WatchList> instrumentWatchlist) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Instrument Watch List", "Save Instrument watch list", jwtHelper.getUserId(), null));
            for (WatchList watchList : instrumentWatchlist) {
                InstrumentWatchLists instrumentWatchlists = new InstrumentWatchLists();
                instrumentWatchlists.setUserId(jwtHelper.getUserId());
                instrumentWatchlists.setInstrumentTradingSymbol(watchList.getTradingSymbol());
                instrumentWatchlists.setInstrumentName(watchList.getInstrumentName());
                instrumentWatchlists.setExchange(watchList.getExchange());
                instrumentWatchlists.setToken(watchList.getToken());
                instrumentWatchlists.setExpiry(watchList.getExpiry());
                instrumentWatchlists.setIsActive(true);
                instrumentWatchlists.setCreatedAt(new Date());
                instrumentWatchlistsRepo.save(instrumentWatchlists);
            }

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
    public ResponseEntity<List<InstrumentWatchlistResponse>> getWatchlist() {
        String userId = jwtHelper.getUserId();
        try {
            log.info(logConfig.getLogHandler().getInfoLog("Watch List", "Get Watch List", jwtHelper.getUserId(), null));
            List<InstrumentWatchlistResponse> instrumentWatchlistResponseList = generateWatchListResponse();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Instrument Watch list",
                            AlgoticUtils.objectToJsonString(instrumentWatchlistResponseList),
                            userId,
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(instrumentWatchlistResponseList, HttpStatus.OK);
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
    public ResponseEntity<WatchListRemoveResponse> deleteWatchlist(List<Integer> watchListRequest) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Remove Watch List", "Remove the watch list", jwtHelper.getUserId(), null));
            WatchListRemoveResponse watchListRemoveResponse = new WatchListRemoveResponse();
            List<WatchListStatusResponse> removeResponses = new ArrayList<>();
            for (Integer watchlistId : watchListRequest) {
                WatchListStatusResponse watchListStatusResponse = new WatchListStatusResponse();
                Optional<InstrumentWatchLists> watchListData = instrumentWatchlistsRepo.findById(watchlistId);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Watch List Data",
                                AlgoticUtils.objectToJsonString(watchListData),
                                jwtHelper.getUserId(),
                                null));
                watchListStatusResponse.setId(watchlistId);
                if (watchListData.isPresent()) {
                    watchListData.get().setIsActive(false);
                    instrumentWatchlistsRepo.save(watchListData.get());
                    watchListStatusResponse.setDeleted(true);
                    watchListStatusResponse.setMessage("Remove from watchlist");
                } else {
                    watchListStatusResponse.setDeleted(false);
                    watchListStatusResponse.setMessage("Id not found in watchList");
                    CommonErrorCode errorCode = CommonErrorCode.BAD_REQUEST;
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    jwtHelper.getUserId(),
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                }
                removeResponses.add(watchListStatusResponse);
            }
            watchListRemoveResponse.setWatchList(removeResponses);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "watchlist Deleted",
                            AlgoticUtils.objectToJsonString(watchListRemoveResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(watchListRemoveResponse, HttpStatus.OK);
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
