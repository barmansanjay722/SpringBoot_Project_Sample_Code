package com.algotic.data.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class GSTDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private String id;

    @Column(name = "CompanyName")
    private String companyName;

    @Column(name = "Type")
    private String type;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "GSTIN")
    private String gstIn;

    @Column(name = "Address")
    private String address;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
