package com.mjyoo.limitedflashsale.dto.responseDto;

import lombok.Getter;

import java.util.List;

@Getter
public class UserListResponseDto {
    private List<UserResponseDto> userInfoList;
    private Long totalUserCount;

    public UserListResponseDto(List<UserResponseDto> userInfoList, long totalUser) {
        this.userInfoList = userInfoList;
        this.totalUserCount = totalUser;
    }
}