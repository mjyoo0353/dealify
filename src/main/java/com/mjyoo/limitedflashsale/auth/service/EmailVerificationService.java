package com.mjyoo.limitedflashsale.auth.service;

import com.mjyoo.limitedflashsale.auth.entity.EmailVerification;
import com.mjyoo.limitedflashsale.auth.repository.EmailVerificationRepository;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender emailSender; // 이메일 발송을 위한 JavaMailSender

    public void sendVerificationEmail(String email) throws MessagingException {
        String token = createVerificationToken(email);
        MimeMessage message = createVerificationMessage(email, token);
        try {
            emailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("Failed to send email to " + email, e);
        }
    }

    public MimeMessage createVerificationMessage(String email, String token) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("Dealify - Welcome! Verify Your Email to Get Started");

        //이메일 본문 내용
        String content =
                "<html><body>" +
                "<p>Please verify your email address for Dealify.</p>" +
                "Your verification code is " + token +
                "</body></html>";

        message.setText(content, "UTF-8", "html");
        return message;
    }

    private String createVerificationToken(String email) {
        // 이메일 인증에 필요한 URL 생성
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        // 토큰을 DB에 저장
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        EmailVerification emailVerification = new EmailVerification(token, user);
        emailVerificationRepository.save(emailVerification);
        return token;
    }

    //이메일 인증 처리
    public boolean verifyEmail(String email, String token) {
        // 토큰을 이용해 인증 요청을 검증
        Optional<EmailVerification> verificationToken = emailVerificationRepository.findByToken(token);

        // 토큰이 유효한지 확인
        if (verificationToken.isEmpty()) {
            return false;
        }

        EmailVerification tokenEntity = verificationToken.get();
        User user = tokenEntity.getUser();

        // 이메일 일치 여부 확인
        if (!user.getEmail().equals(email)) {
            return false; // 이메일 불일치
        }

        // 이메일 인증 처리
        user.setEmailVerified(true);
        userRepository.save(user);

        // 인증 후 토큰 삭제
        emailVerificationRepository.delete(tokenEntity);
        return true; // 인증 성공
    }
}
