package com.algotic.data.entities;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "PaperOrder")
public class PaperOrder {
    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "TradingSymbol")
    private String tradingSymbol;

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

    @Column(name = "CreatedAt")
    private Instant createdAt;

    @Column(name = "ModifiedAt")
    private Instant modifiedAt;
}
