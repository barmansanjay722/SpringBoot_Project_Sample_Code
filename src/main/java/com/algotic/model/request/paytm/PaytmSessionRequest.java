package com.algotic.model.request.paytm;

import com.algotic.model.request.IBrokerSessionRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaytmSessionRequest(
        @NotNull(message = "Enter App token  it is Mandatory") String authToken,
        @NotNull(message = "Enter state (true or false)") String state,
        @Min(value = 0, message = "Enter Broker Id It is mandatory and greater than 0") Integer brokerId)
        implements IBrokerSessionRequest {}
