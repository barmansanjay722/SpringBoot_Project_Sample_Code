package com.algotic.data.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "BrokerSessionDetails")
public class BrokerSessionDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "AuthCode")
    private String authCode;

    @Column(name = "SessionID")
    private String sessionId;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "BrokerCustomerDetailID")
    private Integer brokerCustomerDetailID;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
