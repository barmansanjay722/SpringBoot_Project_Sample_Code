package com.algotic.constants.paytm;

import com.algotic.constants.AlgoticOrderStatus;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaytmOrderStatus {
    TRADED("Traded", AlgoticOrderStatus.COMPLETE.name()),
    REJECTED("Rejected", AlgoticOrderStatus.REJECTED.name()),
    O_PENDING("O-Pending", AlgoticOrderStatus.OPEN.name()),
    PENDING("Pending", AlgoticOrderStatus.OPEN.name()),
    O_CANCEL("O-Cancelled", AlgoticOrderStatus.CANCELLED.name()),
    CANCEL("Cancelled", AlgoticOrderStatus.CANCELLED.name());

    private final String paytmOrderStatus;

    private final String algoticOrderStatus;

    public static PaytmOrderStatus getOrderStatus(String statusName) {
        return Arrays.asList(values()).stream()
                .filter(b -> b.paytmOrderStatus.equals(statusName))
                .findFirst()
                .orElse(null);
    }
}
