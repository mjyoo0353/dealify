package com.mjyoo.limitedflashsale.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "Please enter your email.")
    private String email;

    @NotBlank(message = "Please enter your password.")
    private String password;
}
