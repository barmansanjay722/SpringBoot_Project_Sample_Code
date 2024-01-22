package com.algotic.constants.motilal;

import com.algotic.constants.Exchange;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MotilalExchangeEnum {
    NSE(Exchange.NSE.name()),
    BSE(Exchange.BSE.name()),
    NSEFO(Exchange.NFO.name()),
    BSEFO(Exchange.BFO.name());

    private String algoticExchangeType;

    public static MotilalExchangeEnum getExchangeEnum(String exchangeName) {
        return Arrays.asList(values()).stream()
                .filter(p -> p.algoticExchangeType.equalsIgnoreCase(exchangeName))
                .findFirst()
                .orElse(null);
    }
}
