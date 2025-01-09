package com.mjyoo.limitedflashsale.user.dto;

import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserResponseDto {
    Long id;
    String email;
    String username;
    String phoneNumber;
    String address;
    UserRoleEnum role;
}
