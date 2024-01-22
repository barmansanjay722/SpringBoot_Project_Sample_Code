package com.algotic.model.request.motilal;

import com.algotic.model.request.IBrokerSessionRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MotilalSessionRequest(
        @NotNull(message = "Enter App token  it is Mandatory") String authToken,
        @Min(value = 0, message = "Enter Broker Id It is mandatory or greater than 0") Integer brokerId)
        implements IBrokerSessionRequest {}
