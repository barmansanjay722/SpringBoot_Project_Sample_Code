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
@Table(name = "InstrumentPaperDetails")
public class InstrumentPaperDetails {
    @Id
    @Column(name = "Id")
    private int id;

    @Column(name = "TradingSymbol")
    private String tradingSymbol;

    @Column(name = "Quantity")
    private int qty;

    @Column(name = "BuyPrice")
    private double buyPrice;

    @Column(name = "SellPrice")
    private double sellPrice;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "Token")
    private String token;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
