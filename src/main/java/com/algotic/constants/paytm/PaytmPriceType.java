package com.algotic.constants.paytm;

import com.algotic.constants.PriceType;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaytmPriceType {
    LMT(PriceType.LIMIT.name()),
    MKT(PriceType.MARKET.name()),
    SLM(PriceType.STOP_LOSS.name());

    private String algoticPriceType;

    public static PaytmPriceType getPriceType(String input) {
        return Arrays.asList(PaytmPriceType.values()).stream()
                .filter(p -> p.algoticPriceType.equals(input))
                .findFirst()
                .orElse(null);
    }
}
