package com.mjyoo.limitedflashsale.auth.controller;

import com.mjyoo.limitedflashsale.auth.service.AuthService;
import com.mjyoo.limitedflashsale.auth.service.EmailVerificationService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
        String message = emailVerificationService.sendVerificationEmail(email);
        return ResponseEntity.ok(message);
    }

    //인증코드 확인
    @GetMapping("/email-verification/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String email, @RequestParam String token) {
        boolean isVerified = emailVerificationService.verifyEmail(email, token);
        if (isVerified) {
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        }
        return ResponseEntity.badRequest().body("인증 코드가 유효하지 않습니다.");
    }

    /*//로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        authService.login(requestDto, response);
        return ResponseEntity.ok("로그인에 성공했습니다.");
    }*/

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok("로그아웃에 성공했습니다.");
    }

}
