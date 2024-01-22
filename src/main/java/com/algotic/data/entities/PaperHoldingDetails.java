package com.algotic.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "PaperHoldingDetails")
public class PaperHoldingDetails {
    @Id
    @Column(name = "Id")
    private int id;

    @Column(name = "TradingSymbol")
    private String tradingSymbol;

    @Column(name = "InstrumentName")
    private String instrumentName;

    @Column(name = "Quantity")
    private Integer qty;

    @Column(name = "AvgPrice")
    private Double avgPrice;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "Exchange")
    private String exchange;

    @Column(name = "Token")
    private String token;

    @Column(name = "TradeType")
    private String tradeType;

    @Column(name = "ProductCode")
    private String productCode;

    @Column(name = "TransactionType")
    private String transactionType;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
