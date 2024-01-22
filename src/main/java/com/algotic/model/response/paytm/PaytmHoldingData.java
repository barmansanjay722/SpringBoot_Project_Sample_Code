package com.algotic.model.response.paytm;

import java.util.List;
import lombok.Data;

@Data
public class PaytmHoldingData {

    private List<PaytmHoldingResult> results;
}
