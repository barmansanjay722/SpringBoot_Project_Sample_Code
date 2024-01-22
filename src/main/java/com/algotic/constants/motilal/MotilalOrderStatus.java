package com.algotic.constants.motilal;

import com.algotic.constants.AlgoticOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MotilalOrderStatus {
    CONFIRM(AlgoticOrderStatus.OPEN.name()),
    ERROR(AlgoticOrderStatus.REJECTED.name()),
    TRADED(AlgoticOrderStatus.COMPLETE.name()),
    REJECTED(AlgoticOrderStatus.REJECTED.name()),
    CANCEL(AlgoticOrderStatus.CANCELLED.name());

    private String algoticOrderStatus;
}
