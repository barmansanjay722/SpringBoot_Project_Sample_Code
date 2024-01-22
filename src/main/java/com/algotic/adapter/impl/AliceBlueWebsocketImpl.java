package com.algotic.adapter.impl;

import com.algotic.adapter.AliceBlueWebSocketProvider;
import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.AlgoticConstants;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.aliceblue.WebsocketRequest;
import com.algotic.utils.AlgoticUtils;
import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AliceBlueWebsocketImpl implements AliceBlueWebSocketProvider {
    @Value("${aliceBlue.websocket.session.url}")
    private String websocketSessionUrl;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private JwtHelper jwtHelper;

    @Override
    public void getWebsocketSession(String token) {
        try {
            if (!token.isEmpty()) {
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog("Web socket session", "webSocket session creation", null, null));
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                headers.add(HttpHeaders.AUTHORIZATION, (AlgoticConstants.BEARER + token));
                WebsocketRequest websocketRequest = new WebsocketRequest();
                websocketRequest.setLoginType("API");
                HttpEntity<WebsocketRequest> entity = new HttpEntity<>(websocketRequest, headers);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Object> result =
                        restTemplate.exchange(websocketSessionUrl, HttpMethod.POST, entity, Object.class);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "WebSocket Result",
                                AlgoticUtils.objectToJsonString(result),
                                jwtHelper.getUserId(),
                                null));
                if (result.getBody() == null
                        || (((LinkedHashMap<?, ?>) result.getBody()).containsKey("stat")
                                && (((LinkedHashMap<?, ?>) result.getBody())
                                        .get("stat")
                                        .equals("Not_Ok")))) {
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

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
            log.error(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.error(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            "webSocket session error",
                            null,
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }
}
