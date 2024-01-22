package com.algotic.constants;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BrokerEnum {
    ALICE_BLUE(1, "Alice Blue"),
    MOTILAL_OSWAL(2, "Motilal Oswal"),
    PAYTM_MONEY(3, "Paytm Money");

    private final Integer brokerId;
    private final String brokerName;

    public static BrokerEnum getBrokerEnum(String brokerName) {
        return Arrays.asList(values()).stream()
                .filter(b -> b.brokerName.equals(brokerName))
                .findFirst()
                .orElse(null);
    }
}
