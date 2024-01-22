package com.algotic.data.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "BrokerCustomerDetails")
public class BrokerCustomerDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "BrokerID")
    private Integer brokerId;

    @Column(name = "ReferenceID")
    private String referenceID;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "Status")
    private String status;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
