package com.mjyoo.limitedflashsale.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDto {

    @NotNull(message = "Please enter your email.")
    @Email(message = "Please enter a valid email.")
    private String email;

    @NotNull(message = "Please enter the verification code.")
    private String verificationCode;

    @NotNull(message = "Please enter your name.")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
    private String username;

    @NotNull(message = "Please enter your password.")
    private String password;

    @NotNull(message = "Please enter your phone number.")
    private String phoneNumber;

    @NotNull(message = "Please enter your address.")
    private String address;

    private boolean admin = false;
    private String adminToken = "";
}
