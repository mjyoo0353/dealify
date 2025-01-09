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

    @NotNull(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotNull(message = "인증 코드를 입력해주세요.")
    private String verificationCode;

    @NotNull(message = "사용자 이름을 입력해주세요.")
    @Size(min = 3, max = 20, message = "사용자 이름은 3자 이상 20자 이내여야 합니다.")
    private String username;

    @NotNull(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotNull(message = "연락처를 입력해주세요.")
    private String phoneNumber;

    @NotNull(message = "주소를 입력해주세요.")
    private String address;

    private boolean admin = false;
    private String adminToken = "";
}
