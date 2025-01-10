package com.mjyoo.limitedflashsale.auth.controller;

import com.mjyoo.limitedflashsale.auth.dto.*;
import com.mjyoo.limitedflashsale.auth.service.AuthService;
import com.mjyoo.limitedflashsale.auth.service.EmailVerificationService;
import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    //인증코드 이메일 전송
    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationEmail(@RequestParam String email) throws MessagingException, UnsupportedEncodingException {
        emailVerificationService.sendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.success("인증 코드가 이메일로 전송되었습니다."));
    }

    //인증코드 확인
    @PostMapping("/verify-verification-code")
    public ResponseEntity<?> verifyEmail(@RequestBody EmailVerificationDto emailVerificationDto) {
        boolean isVerified = emailVerificationService.verifyEmail(emailVerificationDto.getEmail(), emailVerificationDto.getCode());
        if (isVerified) {
            return ResponseEntity.ok().body(ApiResponse.success("이메일 인증이 완료되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("인증 코드가 일치하지 않습니다."));
        }
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        LoginResponseDto loginResponseDto = authService.login(requestDto, response);
        return ResponseEntity.ok().body(ApiResponse.success("로그인 되었습니다.", loginResponseDto));
    }

    //Refresh Token 재발급
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDto requestDto,
                                          HttpServletRequest request, HttpServletResponse response) {
        String newAccessToken = authService.refreshToken(requestDto, request, response);
        return ResponseEntity.ok().body(ApiResponse.success(newAccessToken));
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok().body(ApiResponse.success("로그아웃 되었습니다."));
    }

}
