package com.algotic.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TransactionType {
    BUY,
    SELL,
    SQUAREOFF;
}
