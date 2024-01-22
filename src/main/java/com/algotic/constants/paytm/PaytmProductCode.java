package com.algotic.constants.paytm;

import com.algotic.constants.AlgoticProductCode;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaytmProductCode {
    C(AlgoticProductCode.CNC.name()),
    I(AlgoticProductCode.MIS.name());

    private final String algoticProductCode;

    public static PaytmProductCode getProductCode(String input) {
        return Arrays.asList(PaytmProductCode.values()).stream()
                .filter(e -> e.algoticProductCode.equalsIgnoreCase(input))
                .findFirst()
                .orElse(C);
    }
}
