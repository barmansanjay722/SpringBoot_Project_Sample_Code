package com.algotic.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
public class TradeExecution {
    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "TradingSymbol")
    private String tradingSymbol;

    @Column(name = "Name")
    private String name;

    @Column(name = "TransactionType")
    private String transactionType;

    @Column(name = "ErrorMessage")
    private String errorMessage;

    @Column(name = "Status")
    private String status;

    @Column(name = "Strategy")
    private String strategy;

    @Column(name = "ExecutionTime")
    private String executionTime;
}
