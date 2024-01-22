package com.algotic.data.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Trades")
public class Trades {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "TradeType")
    private String tradeType;

    @Column(name = "StockType")
    private String stockType;

    @Column(name = "InstrumentName")
    private String instrumentName;

    @Column(name = "TradingSymbol")
    private String tradingSymbol;

    @Column(name = "Exchange")
    private String exchange;

    @Column(name = "StrategyID")
    private Integer strategyId;

    @Column(name = "LotSize")
    private Integer lotSize;

    @Column(name = "StopLossPrice")
    private Double stopLossPrice;

    @Column(name = "TargetProfit")
    private Double targetProfit;

    @Column(name = "OrderType")
    private String orderType;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "BrokerCustomerDetailID")
    private Integer brokerCustomerDetailId;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "IsDeleted")
    private Boolean isDeleted;

    @Column(name = "Token")
    private String token;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
