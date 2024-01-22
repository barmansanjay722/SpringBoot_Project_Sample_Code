package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.AlgoticStatus;
import com.algotic.data.entities.Strategies;
import com.algotic.data.entities.StrategiesManagements;
import com.algotic.data.repositories.StrategiesManagementRepo;
import com.algotic.data.repositories.StrategiesRepo;
import com.algotic.data.repositories.TradesRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.StrategyRequest;
import com.algotic.model.response.*;
import com.algotic.services.StrategyService;
import com.algotic.utils.AlgoticUtils;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@Slf4j
public class StrategyServiceImpl implements StrategyService {
    @Autowired
    private StrategiesRepo strategyRepo;

    @Autowired
    private TradesRepo tradesRepo;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private StrategiesManagementRepo strategiesManagementRepo;

    @Transactional
    @Override
    public ResponseEntity<List<StrategiesResponse>> getStrategiesNames() {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Strategies ",
                            "Get Strategies List",
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            List<Strategies> strategiesList = strategyRepo.getStrategyNames();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Strategy List",
                            AlgoticUtils.objectToJsonString(strategiesList),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            List<StrategiesResponse> response = new ArrayList<>();
            for (Strategies data : strategiesList) {
                StrategiesResponse strategy = new StrategiesResponse(data.getId(), data.getName());
                response.add(strategy);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "get all the strategy names",
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

    @Override
    public ResponseEntity<StrategyReportsResponse> strategyReports(Integer limit, Integer offset) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Strategy Reports", "Get Strategy reports data", jwtHelper.getUserId(), null));
            int limitValue = limit == null ? 5 : limit;
            int offsetValue = offset == null ? 0 : offset;
            if (limitValue < 0 || offsetValue < 0) {
                CommonErrorCode errorCode = CommonErrorCode.LIMIT;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            List<Strategies> strategiesList = strategyRepo.getStrategy(limitValue, offsetValue);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Strategy List",
                            AlgoticUtils.objectToJsonString(strategiesList),
                            jwtHelper.getUserId(),
                            null));
            StrategyReportsResponse response = new StrategyReportsResponse();
            Integer count = strategyRepo.allCount();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Strategy Count", AlgoticUtils.objectToJsonString(count), jwtHelper.getUserId(), null));
            response.setTotal(count);
            List<StrategyReportsDataResponse> strategyReportsDataResponseList = new ArrayList<>();
            for (Strategies data : strategiesList) {
                Integer strategyCount = tradesRepo.findCountForStrategy(data.getId());
                Integer userCount = tradesRepo.findCount(data.getId());
                StrategyReportsDataResponse strategyReportsDataResponse = new StrategyReportsDataResponse();
                strategyReportsDataResponse.setStrategyId(data.getId());
                strategyReportsDataResponse.setStrategyName(data.getName());
                strategyReportsDataResponse.setNoOfUser(userCount);
                strategyReportsDataResponse.setUsageOfStrategy(strategyCount);
                strategyReportsDataResponseList.add(strategyReportsDataResponse);
            }
            if (strategyReportsDataResponseList.isEmpty()) {
                throw new AlgoticException(CommonErrorCode.DATA_NOT_FOUND);
            }
            response.setResult(strategyReportsDataResponseList);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "get all the usage of strategy",
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

    @Override
    public ResponseEntity<StrategiesManagementResponse> getStrategies(
            String name, Date from, Date to, String status, Integer limit, Integer offset) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Strategy Management", "Strategy Management", jwtHelper.getUserId(), null));
            int limitValue = limit == null ? 5 : limit;
            int offsetValue = offset == null ? 0 : offset;
            if (name == null) {
                name = "";
            }
            if (status == null) {
                status = "";
            } else if (limitValue < 0 || offsetValue < 0) {
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
            List<StrategiesManagements> strategy = strategiesManagementRepo.getStrategies(
                    name, from, to, status.toUpperCase(), limitValue, offsetValue);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Strategy", AlgoticUtils.objectToJsonString(strategy), jwtHelper.getUserId(), null));
            Object[] strategyCount = strategiesManagementRepo.getStrategiesCount(name, from, to, status.toUpperCase());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Strategy count",
                            AlgoticUtils.objectToJsonString(strategyCount),
                            jwtHelper.getUserId(),
                            null));
            StrategiesManagementResponse response = new StrategiesManagementResponse();
            response.setTotal(Integer.parseInt(strategyCount[0].toString()));
            response.setResult(strategy);

            if (strategy.isEmpty()) {
                throw new AlgoticException(CommonErrorCode.DATA_NOT_FOUND);
            }

            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Get All Strategies",
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

    @Override
    public ResponseEntity<StrategyDetailsResponse> saveStrategy(StrategyRequest strategyRequest) {
        StrategyDetailsResponse saveStrategyResponse = new StrategyDetailsResponse();
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Save Strategy", "Save the Strategy ", jwtHelper.getUserId(), null));
            Strategies strategy =
                    strategyRepo.findByNameAndIsActiveAndIsDeleted(strategyRequest.getName(), true, false);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Strategy", AlgoticUtils.objectToJsonString(strategy), jwtHelper.getUserId(), null));
            if (strategy != null) {
                BusinessErrorCode errorCode = BusinessErrorCode.STRATEGY_ALREADY_EXIST;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            Strategies strategies = new Strategies();
            strategies.setName(strategyRequest.getName());
            strategies.setScript(strategyRequest.getScript());
            strategies.setIsActive(true);
            strategies.setIsDeleted(false);
            strategies.setCreatedAt(new Date());
            strategyRepo.save(strategies);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            " Save strategy in Database",
                            AlgoticUtils.objectToJsonString(strategies),
                            jwtHelper.getUserId(),
                            null));

            saveStrategyResponse.setName(strategies.getName());
            saveStrategyResponse.setScript(strategies.getScript());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "save strategy",
                            AlgoticUtils.objectToJsonString(saveStrategyResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.CREATED.value())));
            return new ResponseEntity<>(saveStrategyResponse, HttpStatus.CREATED);
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

    @Override
    public ResponseEntity<StrategyDetailsResponse> getStrategyDetails(int id) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Strategy by Id", "get Stattegy by id", jwtHelper.getUserId(), null));
            StrategyDetailsResponse strategyDetailsResponse = new StrategyDetailsResponse();
            Strategies detailsResponse = strategyRepo.findByIdAndIsActive(id);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Strategy details",
                            AlgoticUtils.objectToJsonString(detailsResponse),
                            jwtHelper.getUserId(),
                            null));
            if (detailsResponse != null) {
                strategyDetailsResponse.setName(detailsResponse.getName());
                strategyDetailsResponse.setScript(detailsResponse.getScript());
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "get strategy details",
                                AlgoticUtils.objectToJsonString(strategyDetailsResponse),
                                jwtHelper.getUserId(),
                                String.valueOf(HttpStatus.OK.value())));
                return new ResponseEntity<>(strategyDetailsResponse, HttpStatus.OK);
            }
            BusinessErrorCode errorCode = BusinessErrorCode.ID_NOT_EXISTS;
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
    public ResponseEntity<GlobalMessageResponse> activeInactiveStrategy(int id, String type) {
        GlobalMessageResponse globalMessageResponse = new GlobalMessageResponse();
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Strategy Active Inactive",
                            "Get the Strategy active or inactive",
                            jwtHelper.getUserId(),
                            null));

            Strategies strategy = strategyRepo.findByIdAndIsDeleted(id, false);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Strategy", AlgoticUtils.objectToJsonString(strategy), jwtHelper.getUserId(), null));
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
            } else if (AlgoticStatus.ACTIVE.name().equalsIgnoreCase(type)) {
                strategy.setIsActive(true);
                strategyRepo.save(strategy);
                globalMessageResponse.setMessage("Strategy Active");
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Strategy is Active",
                                AlgoticUtils.objectToJsonString(globalMessageResponse),
                                jwtHelper.getUserId(),
                                String.valueOf(HttpStatus.OK.value())));
                return new ResponseEntity<>(globalMessageResponse, HttpStatus.OK);
            } else if (AlgoticStatus.INACTIVE.name().equalsIgnoreCase(type)) {
                strategy.setIsActive(false);
                strategyRepo.save(strategy);
                globalMessageResponse.setMessage("Strategy Inactive");
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Strategy is  Inactive",
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
    public ResponseEntity<GlobalMessageResponse> deleteStrategy(int id) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Delete Strategy", "Delete Strategy By Id", jwtHelper.getUserId(), null));
            GlobalMessageResponse globalMessageResponse = new GlobalMessageResponse();
            Strategies strategies = strategyRepo.findByIdAndIsDeleted(id, false);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Get the strategy",
                            AlgoticUtils.objectToJsonString(strategies),
                            jwtHelper.getUserId(),
                            null));
            if (strategies != null) {
                strategies.setIsActive(false);
                strategies.setIsDeleted(true);
                strategyRepo.save(strategies);
                globalMessageResponse.setMessage("Strategy deleted");

                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Delete Strategy",
                                AlgoticUtils.objectToJsonString(globalMessageResponse),
                                jwtHelper.getUserId(),
                                null));
                return new ResponseEntity<>(globalMessageResponse, HttpStatus.OK);
            }
            BusinessErrorCode errorCode = BusinessErrorCode.STRATEGY_NOT_EXISTS;
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
}
