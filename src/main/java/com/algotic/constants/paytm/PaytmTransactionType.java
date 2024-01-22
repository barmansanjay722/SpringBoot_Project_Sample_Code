package com.algotic.constants.paytm;

import com.algotic.constants.TransactionType;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaytmTransactionType {
    B(TransactionType.BUY.name()),
    S(TransactionType.SELL.name()),
    SF(TransactionType.SQUAREOFF.name());

    private final String algoticTransactionType;

    public static PaytmTransactionType getTransactionType(String input) {
        return Arrays.asList(PaytmTransactionType.values()).stream()
                .filter(e -> e.algoticTransactionType.equalsIgnoreCase(input))
                .findFirst()
                .orElse(null);
    }
}
