package com.algotic.constants.aliceblue;

import com.algotic.constants.AlgoticProductCode;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AliceBlueProductCode {
    MIS(AlgoticProductCode.MIS.name()),
    CNC(AlgoticProductCode.CNC.name()),
    NRML(AlgoticProductCode.NORMAL.name());

    private String algoticProductCode;

    public static AliceBlueProductCode getProductCode(String input) {
        return Arrays.asList(AliceBlueProductCode.values()).stream()
                .filter(p -> p.algoticProductCode.equals(input))
                .findFirst()
                .orElse(null);
    }
}
