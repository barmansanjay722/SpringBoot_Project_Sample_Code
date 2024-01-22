package com.algotic.data.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "Users")
@NoArgsConstructor
public class Users {
    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "Email")
    private String email;

    @Column(name = "TermsAccepted")
    private Boolean termsAccepted;

    @Column(name = "PasswordHash")
    private String passwordHash;

    @Column(name = "PasswordSalt")
    private String passwordSalt;

    @Column(name = "IsDeleted")
    private Boolean isDeleted;

    @Column(name = "Role")
    private String role;

    @Column(name = "Status")
    private String status;

    @Column(name = "Stage")
    private String stage;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
