package com.algotic.model.request.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EncApiSessionIdRequest {

    @JsonProperty(value = "userId")
    private String userId;

    @JsonProperty(value = "userData")
    private String userData;
}
