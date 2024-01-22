package com.algotic.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WebSocketResponse {
    private String websocketSession;
    private String tick;
    private String actId;
    private String userId;
    private String source;
    private Boolean paperTrade;
    private Boolean brokerConnected;
    private String brokerName;
}
