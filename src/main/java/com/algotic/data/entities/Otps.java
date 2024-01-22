package com.algotic.data.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
@Table(name = "Otps")
public class Otps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "Otp")
    private String otp;

    @Column(name = "ReferenceCode")
    private String referenceCode;

    @Column(name = "ExpiresIn")
    private Integer expiresIn;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "IsVerified")
    private Boolean isVerified;

    @Column(name = "OtpType")
    private String otpType;

    @Column(name = "Attempts")
    private Integer attempts;

    @Column(name = "VerifiedAt")
    private Date verifiedAt;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
