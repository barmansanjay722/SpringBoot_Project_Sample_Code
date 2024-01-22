package com.algotic.data.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
@Table(name = "InstrumentWatchlist")
public class InstrumentWatchLists {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "InstrumentTradingSymbol")
    private String instrumentTradingSymbol;

    @Column(name = "Exchange")
    private String exchange;

    @Column(name = "Token")
    private String token;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "InstrumentName")
    private String instrumentName;

    @Column(name = "Expiry")
    private Date expiry;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
