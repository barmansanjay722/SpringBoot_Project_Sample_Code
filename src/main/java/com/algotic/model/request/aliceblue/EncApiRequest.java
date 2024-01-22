package com.algotic.model.request.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EncApiRequest {
    @JsonProperty(value = "userId")
    private String userId;
}
