package com.algotic.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class CustomerManagement {
    @Column(name = "ID")
    @Id
    private String id;

    @Column(name = "Name")
    private String name;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "LastActive")
    private String lastActive;

    @Column(name = "Type")
    private String type;

    @Column(name = "Renewal")
    private String renewal;

    @Column(name = "Payment")
    private String payment;

    @Column(name = "Status")
    private String status;
}
