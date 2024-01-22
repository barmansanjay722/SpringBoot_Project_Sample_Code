package com.algotic.model.request;

import jakarta.annotation.Nullable;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WatchList {

    private String instrumentName;
    private String tradingSymbol;
    private String token;
    private String exchange;

    @Nullable
    private Date expiry;
}
