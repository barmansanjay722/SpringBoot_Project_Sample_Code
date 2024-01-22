package com.algotic.constants.motilal;

import com.algotic.constants.AlgoticProductCode;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MotilalProductCode {
    DELIVERY(AlgoticProductCode.CNC.name()),
    NORMAL(AlgoticProductCode.NORMAL.name()),
    VALUEPLUS(AlgoticProductCode.MIS.name());

    private String value;

    public static MotilalProductCode getProductType(String productType) {
        return Arrays.asList(values()).stream()
                .filter(p -> p.value.equalsIgnoreCase(productType))
                .findFirst()
                .orElse(DELIVERY);
    }
}
