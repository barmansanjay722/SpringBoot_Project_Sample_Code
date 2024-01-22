package com.algotic.model.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InstrumentWatchlistResponse {
    private Integer id;
    private String instrumentName;
    private String exchange;
    private String tradingSymbol;
    private String token;
    private Boolean isStock;
    private Date expiry;
}
