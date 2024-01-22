package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SquareOffResponse {
    @JsonProperty("stat")
    private String status;

    @JsonProperty("emsg")
    private String message;
}
