package com.mjyoo.limitedflashsale.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class UserInfoListDto {
    private List<UserInfoDto> userInfoList;
    private Long totalUserCount;

    public UserInfoListDto(List<UserInfoDto> userInfoList, long totalUser) {
        this.userInfoList = userInfoList;
        this.totalUserCount = totalUser;
    }
}
