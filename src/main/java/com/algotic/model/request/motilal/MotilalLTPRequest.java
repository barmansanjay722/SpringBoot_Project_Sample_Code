package com.algotic.model.request.motilal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MotilalLTPRequest {

    private String exchange;

    private int scripcode;
}
