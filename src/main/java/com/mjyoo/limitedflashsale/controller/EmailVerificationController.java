
package com.mjyoo.limitedflashsale.controller;

import com.mjyoo.limitedflashsale.service.EmailVerificationService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    //인증코드 이메일 전송
    @PostMapping("/send")
    public ResponseEntity<?> sendVerificationEmail(@RequestParam String email) throws MessagingException {
        String message = emailVerificationService.sendVerificationEmail(email);
        return ResponseEntity.ok(message);
    }

    //인증코드 확인
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String email, @RequestParam String token) {
        boolean isVerified = emailVerificationService.verifyEmail(email, token);
        if (isVerified) {
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        }
        return ResponseEntity.badRequest().body("인증 코드가 유효하지 않습니다.");
    }
}

