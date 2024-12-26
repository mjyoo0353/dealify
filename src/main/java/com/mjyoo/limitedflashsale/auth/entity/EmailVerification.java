package com.mjyoo.limitedflashsale.auth.entity;

import com.mjyoo.limitedflashsale.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@NoArgsConstructor
@Getter
public class EmailVerification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private String email;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime expiredAt;

    @ManyToOne
    private User user;

    public EmailVerification(String token, User user) {
        this.token = token;
        this.user = user;
        this.email = user.getEmail();
        this.expiredAt = LocalDateTime.now().plusMinutes(5);
    }
}
