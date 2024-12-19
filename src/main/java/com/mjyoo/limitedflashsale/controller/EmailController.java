package com.mjyoo.limitedflashsale.controller;

import com.mjyoo.limitedflashsale.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    //인증코드 이메일 전송
    @PostMapping("/send")
    public String sendAuthEmail(@RequestParam String email) throws MessagingException {
        return emailService.sendAuthEmail(email);
    }

    //인증코드 확인
}
