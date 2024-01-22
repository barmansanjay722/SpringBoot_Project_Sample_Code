package com.algotic.data.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
@Table(name = "VerificationDetails")
public class VerificationDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "Resource")
    private String resource;

    @Column(name = "ResourceType")
    private String resourceType;

    @Column(name = "IsVerified")
    private Boolean isVerified;

    @Column(name = "VerificationCode")
    private String verificationCode;

    @Column(name = "VerifiedAt")
    private Date verifiedAt;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
