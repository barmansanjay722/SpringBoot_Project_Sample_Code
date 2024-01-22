package com.algotic.model.response.paytm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaytmSessionResponse {

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("channel_id")
    private String channelId;

    @JsonProperty("api_key")
    private String apiKey;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("public_access_token")
    private String publicAccessToken;

    @JsonProperty("read_access_token")
    private String readAccessToken;
}
