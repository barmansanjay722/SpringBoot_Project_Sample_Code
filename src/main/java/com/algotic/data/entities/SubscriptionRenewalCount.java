package com.algotic.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
public class SubscriptionRenewalCount {
    @Id
    @Column(name = "data_count")
    private Integer count;
}
