package com.algotic.data.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "SubscriptionTransaction")
public class SubscriptionTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "PaymentId")
    private String paymentId;

    @Column(name = "TransactionId")
    private String transactionId;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "Status")
    private String status;

    @Column(name = "SubscriptionId")
    private Integer subscriptionId;

    @Column(name = "Amount")
    private Double amount;

    @Column(name = "BrokerId")
    private Integer brokerId;

    @Column(name = "Currency")
    private String currency;

    @Column(name = "ResourceUri")
    private String resourceUri;

    @Column(name = "PaymentCompletedAt")
    private String paymentCompletedAt;

    @Column(name = "BuyerName")
    private String buyerName;

    @Column(name = "BuyerEmail")
    private String buyerEmail;

    @Column(name = "BuyerPhoneNum")
    private String buyerPhoneNum;

    @Column(name = "MessageAuthenticationCode")
    private String messageAuthenticationCode;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
