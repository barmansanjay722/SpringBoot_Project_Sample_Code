package com.algotic.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
public class SubscriptionRenewalData {
    @Id
    @Column(name = "ID")
    private String userId;

    @Column(name = "Name")
    private String name;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "Email")
    private String email;

    @Column(name = "daysLeft")
    private Long daysLeft;

    @Column(name = "DateOfRenewal")
    private String dateOfRenewal;
}
