package com.algotic.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Data
public class PaperPositions {
    @Id
    @Column(name = "Id")
    private Integer id;

    @Column(name = "TradeType")
    private String tradeType;

    @Column(name = "TradingSymbol")
    private String tradingSymbol;

    @Column(name = "InstrumentName")
    private String instrumentName;

    @Column(name = "Exchange")
    private String exchange;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "BlQuantity")
    private Integer blQuantity;

    @Column(name = "BuyAveragePrice")
    private Double buyAverage;

    @Column(name = "SellAveragePrice")
    private Double sellAverage;

    @Column(name = "BuyQuantity")
    private Integer buyQuantity;

    @Column(name = "SellQuantity")
    private Integer sellQuantity;

    @Column(name = "Token")
    private String token;

    @Column(name = "ProductCode")
    private String productCode;
}
