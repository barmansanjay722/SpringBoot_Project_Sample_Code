package com.algotic.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
public class InActiveUsers {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "Name")
    private String name;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "SubscriptionId")
    private Integer subscriptionId;

    @Column(name = "Type")
    private String type;

    @Column(name = "InActivityAge")
    private Long inactiveDays;
}
