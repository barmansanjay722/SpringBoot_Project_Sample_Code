package com.algotic.data.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Complexity")
    private String complexity;

    @Column(name = "DisclosedQuantity")
    private Integer discloseQuantity;

    @Column(name = "TradingSymbol")
    private String tradingSymbol;

    @Column(name = "Exchange")
    private String exchange;

    @Column(name = "Retention")
    private String retention;

    @Column(name = "PriceType")
    private String priceType;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "TriggerPrice")
    private Double triggerPrice;

    @Column(name = "ProductCode")
    private String productCode;

    @Column(name = "TransactionType")
    private String transactionType;

    @Column(name = "Price")
    private Double price;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "NestOrderNumber")
    private String nestOrderNumber;

    @Column(name = "OrderType")
    private String orderType;

    @Column(name = "Status")
    private String status;

    @Column(name = "TradeId")
    private String tradeId;

    @Column(name = "TradeType")
    private String tradeType;

    @Column(name = "BrokerId")
    private Integer brokerId;

    @Column(name = "ErrorMessage")
    private String errorMessage;

    @Column(name = "Token")
    private String token;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
