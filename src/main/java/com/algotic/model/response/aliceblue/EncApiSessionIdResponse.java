package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncApiSessionIdResponse {

    @JsonProperty(value = "stat")
    private String stat;

    @JsonProperty(value = "sessionID")
    private String sessionId;
}
