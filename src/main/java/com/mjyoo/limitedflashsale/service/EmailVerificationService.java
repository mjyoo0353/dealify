package com.mjyoo.limitedflashsale.service;

import com.mjyoo.limitedflashsale.entity.EmailVerification;
import com.mjyoo.limitedflashsale.entity.User;
import com.mjyoo.limitedflashsale.repository.EmailVerificationRepository;
import com.mjyoo.limitedflashsale.repository.UserRepository;
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

    public String sendVerificationEmail(String email) throws MessagingException {
        String token = createVerificationToken(email);
        MimeMessage message = createVerificationMessage(email, token);
        try {
            emailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("Failed to send email to " + email, e);
        }
        return "인증 이메일이 성공적으로 전송되었습니다.";
    }

    public MimeMessage createVerificationMessage(String email, String token) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("Dealify 회원가입 인증 메일입니다.");

        //이메일 본문 내용
        String content =
                "<html><body>" +
                "<h3>Dealify 회원가입 인증</h3>" +
                "<p>아래의 코드를 입력하여 회원가입을 완료해주세요.</p>" +
                "인증번호는 " + token + " 입니다." +
                "</body></html>";

        message.setText(content, "UTF-8", "html");
        return message;
    }

    private String createVerificationToken(String email) {
        // 이메일 인증에 필요한 URL 생성
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        // 토큰을 DB에 저장
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

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
