package com.algotic.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PriceType {
    LIMIT,
    MARKET,
    STOP_LOSS;
}
