package com.algotic.data.entities;

import jakarta.persistence.*;
import java.math.BigInteger;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Instruments")
public class Instruments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Exchange")
    private String exchange;

    @Column(name = "Symbol")
    private String symbol;

    @Column(name = "TradingSymbol")
    private String tradingSymbol;

    @Column(name = "FormatedInsName")
    private String formatedInsName;

    @Column(name = "Token")
    private String token;

    @Column(name = "ExchangeSegment")
    private String exchangeSegment;

    @Column(name = "ExpiryDate")
    private BigInteger expiryDate;
}
