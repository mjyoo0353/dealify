package com.mjyoo.limitedflashsale.dto.responseDto;

import com.mjyoo.limitedflashsale.entity.UserRoleEnum;
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
    boolean isEmailVerified;
}
