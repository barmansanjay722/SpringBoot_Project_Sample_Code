package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.model.response.WebSocketResponse;
import com.algotic.services.InstrumentService;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class WebsocketController {
    @Autowired
    InstrumentService instrumentService;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @CrossOrigin
    @GetMapping("/websocket")
    public ResponseEntity<WebSocketResponse> getWebsocketData() throws NoSuchAlgorithmException {
        log.info(logConfig.getLogHandler().getInfoLog("WebSocket Data", "Request for web socket data", null, null));
        return instrumentService.getWebsocketData();
    }
}
