package com.algotic.model.response.motilal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Data;

@Data
public class MotilalLTPResponse extends GeneralMotilalResponse {

    @JsonProperty("data")
    private Optional<MotilalLTPData> motilalLtpData;
}
