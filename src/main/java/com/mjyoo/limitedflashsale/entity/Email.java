package com.mjyoo.limitedflashsale.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "email")
@NoArgsConstructor
@Getter
public class Email {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_id")
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

}
