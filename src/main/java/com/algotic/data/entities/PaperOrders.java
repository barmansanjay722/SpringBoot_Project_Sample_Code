package com.algotic.data.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "PaperOrder")
public class PaperOrders {
    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "TradingSymbol")
    private String tradingSymbol;

    @Column(name = "InstrumentName")
    private String instrumentName;

    @Column(name = "Price")
    private Double price;

    @Column(name = "ProductCode")
    private String productCode;

    @Column(name = "TransactionType")
    private String transactionType;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "Exchange")
    private String exchange;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "Token")
    private String token;

    @Column(name = "TriggerPrice")
    private Double triggerPrice;

    @Column(name = "Status")
    private String status;

    @Column(name = "TradeType")
    private String tradeType;

    @Column(name = "PriceType")
    private String priceType;

    @Column(name = "IsHolding")
    private Boolean IsHolding;

    @Column(name = "CreatedAt")
    private Instant createdAt;

    @Column(name = "Expiry")
    private Date expiry;

    @Column(name = "ModifiedAt")
    private Instant modifiedAt;
}
