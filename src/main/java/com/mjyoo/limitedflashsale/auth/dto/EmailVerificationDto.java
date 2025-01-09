package com.mjyoo.limitedflashsale.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationDto {
    private String email;
    private String code;

}
