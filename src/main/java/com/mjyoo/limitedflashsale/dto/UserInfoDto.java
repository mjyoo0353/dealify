package com.mjyoo.limitedflashsale.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserInfoDto {
    Long id;
    String email;
    String username;
    String phoneNumber;
    String address;
    boolean isEmailVerified;
}
