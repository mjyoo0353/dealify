package com.mjyoo.limitedflashsale.auth.controller;

import com.mjyoo.limitedflashsale.auth.service.AuthService;
import com.mjyoo.limitedflashsale.auth.service.EmailVerificationService;
import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    private final EmailVerificationService emailVerificationService;

    //인증코드 이메일 전송
    @PostMapping("/email-verification/send")
    public ResponseEntity<?> sendVerificationEmail(@RequestParam String email) throws MessagingException {
        emailVerificationService.sendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.success("인증 코드가 이메일로 전송되었습니다."));
    }

    //인증코드 확인
    @GetMapping("/email-verification/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String email, @RequestParam String token) {
        boolean isVerified = emailVerificationService.verifyEmail(email, token);
        if (!isVerified) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("인증 코드가 올바르지 않습니다."));
        }
        return ResponseEntity.ok().body(ApiResponse.success("이메일 인증이 완료되었습니다."));
    }

    /*//로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        authService.login(requestDto, response);
        return ResponseEntity.ok().body(ApiResponse.success("로그인 되었습니다."));
    }*/

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok().body(ApiResponse.success("로그아웃 되었습니다."));
    }

}
