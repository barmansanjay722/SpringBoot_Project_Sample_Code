package com.algotic.model.request.paytm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaytmAccessTokenRequest(
        @JsonProperty("api_key") String apiKey,
        @JsonProperty("api_secret_key") String apiSecretKey,
        @JsonProperty("request_token") String requestToken) {}
