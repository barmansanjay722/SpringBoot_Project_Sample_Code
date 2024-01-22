package com.algotic.constants.aliceblue;

import com.algotic.constants.PriceType;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AliceBluePriceType {
    L(PriceType.LIMIT.name()),
    MKT(PriceType.MARKET.name()),
    SL(PriceType.STOP_LOSS.name());

    private String algoticPriceType;

    public static AliceBluePriceType getPriceType(String input) {
        return Arrays.asList(AliceBluePriceType.values()).stream()
                .filter(p -> p.algoticPriceType.equals(input))
                .findFirst()
                .orElse(null);
    }
}
