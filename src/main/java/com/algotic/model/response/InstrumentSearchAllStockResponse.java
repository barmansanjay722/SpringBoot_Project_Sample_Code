package com.algotic.model.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstrumentSearchAllStockResponse {

    private String exchange;
    private String tradingSymbol;
    private String formattedInsName;
    private String exchangeSegment;
    private String token;
    private String lotSize;
    private String ticSize;
    private Date expiry;
    private Boolean isFavourite;
}
