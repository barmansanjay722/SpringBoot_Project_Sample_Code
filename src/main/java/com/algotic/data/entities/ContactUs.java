package com.algotic.data.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.*;

@Entity
@NoArgsConstructor
@Data
@Table(name = "ContactUs")
public class ContactUs {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Email")
    private String email;

    @Column(name = "Phone")
    private String phone;

    @Column(name = "Message")
    private String message;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "ModifiedAt")
    private Date modifiedAt;
}
