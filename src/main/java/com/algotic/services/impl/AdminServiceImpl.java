package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.data.repositories.AccessTokensRepo;
import com.algotic.data.repositories.SubscriptionTransactionsRepo;
import com.algotic.data.repositories.UsersRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.response.DashboardResponse;
import com.algotic.services.AdminService;
import com.algotic.utils.AlgoticUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {
    @Autowired
    SubscriptionTransactionsRepo transactionsRepo;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private AccessTokensRepo accessTokensRepo;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private JwtHelper jwtHelper;

    @Override
    public ResponseEntity<DashboardResponse> adminDashboard() {
        try {
            handleLogging("admin dashboard", "Request for the dashboard data", jwtHelper.getUserId(), null);
            DashboardResponse dashboardResponse = new DashboardResponse();
            Integer revenue = usersRepo.getSubscriberCount();
            Integer totalUsers = usersRepo.getUsersCount();
            dashboardResponse.setRevenue(revenue);
            Integer subscribers = totalUsers - revenue;
            handleLogging(
                    "Subscribers Data", AlgoticUtils.objectToJsonString(subscribers), jwtHelper.getUserId(), null);

            dashboardResponse.setSubscribers(subscribers);
            Integer users = usersRepo.getUsersCount();

            Integer loginUsers = accessTokensRepo.getUsersCount();

            dashboardResponse.setActiveUsersToday(loginUsers);

            Integer inactiveUsers = users - loginUsers;
            dashboardResponse.setInactiveUsersToday(inactiveUsers);
            handleLogging(
                    "admin dashboard",
                    AlgoticUtils.objectToJsonString(dashboardResponse),
                    jwtHelper.getUserId(),
                    String.valueOf(HttpStatus.OK.value()));
            return new ResponseEntity<>(dashboardResponse, HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            handleLogging(
                    errorCode.getErrorCode(),
                    errorCode.getErrorMessage(),
                    jwtHelper.getUserId(),
                    String.valueOf(errorCode.getHttpStatus().value()));
            throw new AlgoticException(errorCode);
        }
    }

    private void handleLogging(String infoMessage, String infoContext, String userid, String httpCode) {
        log.info(logConfig.getLogHandler().getInfoLog(infoMessage, infoContext, userid, httpCode));
    }

    private BusinessErrorCode handleSubscriberNotFound() {
        BusinessErrorCode errorCode = BusinessErrorCode.SUBSCRIBER_NOT_EXIST;
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        errorCode.getErrorCode(),
                        errorCode.getErrorMessage(),
                        jwtHelper.getUserId(),
                        String.valueOf(errorCode.getHttpStatus().value())));
        throw new AlgoticException(errorCode);
    }

    private BusinessErrorCode handleUserNotValid() {
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
