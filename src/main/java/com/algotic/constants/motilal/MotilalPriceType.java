package com.algotic.constants.motilal;

import com.algotic.constants.PriceType;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MotilalPriceType {
    LIMIT("LIMIT", PriceType.LIMIT.name()),
    MARKET("MARKET", PriceType.MARKET.name()),
    STOPLOSS("STOP LOSS", PriceType.STOP_LOSS.name());

    private final String motilalPriceType;

    private final String algoticPriceType;

    public static MotilalPriceType getPriceType(String priceType) {
        return Arrays.asList(values()).stream()
                .filter(p -> p.algoticPriceType.equalsIgnoreCase(priceType))
                .findFirst()
                .orElse(null);
    }

    public static MotilalPriceType getMotilalPriceType(String priceType) {
        return Arrays.asList(values()).stream()
                .filter(p -> p.motilalPriceType.equalsIgnoreCase(priceType))
                .findFirst()
                .orElse(null);
    }
}
