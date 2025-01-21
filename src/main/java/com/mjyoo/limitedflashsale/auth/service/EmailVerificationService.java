package com.mjyoo.limitedflashsale.auth.service;

import com.mjyoo.limitedflashsale.common.util.RedisKeys;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationService {

    private final JavaMailSender emailSender; // 이메일 발송을 위한 JavaMailSender
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${MAIL_USERNAME}")
    private String mailUsername;

    public void sendVerificationEmail(String email) throws MessagingException, UnsupportedEncodingException {
        Optional<User> checkEmail = userRepository.findByEmail(email);
        checkEmail.ifPresent(user -> {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);});

        String code = createVerificationCode();
        MimeMessage message = createVerificationMessage(email, code);
        try {
            emailSender.send(message);
        } catch (MailException e) {
            throw new IllegalArgumentException("Failed to send email to " + email, e);
        }
        // 이메일과 인증 코드를 Redis에 저장
        String key = RedisKeys.getSignupCode(email);
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        log.info("Verification code: {}", code);
    }

    public MimeMessage createVerificationMessage(String email, String code) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("Dealify - Welcome! Verify Your Email to Get Started");

        //이메일 본문 내용
        String content =
                "<html><body>" +
                "<p>Please verify your email address for Dealify.</p>" +
                "Your verification code is " + code +
                "</body></html>";

        message.setText(content, "UTF-8", "html");
        message.setFrom(new InternetAddress(mailUsername,"Dealify"));
        return message;
    }

    private String createVerificationCode() {
        // 이메일 인증에 필요한 URL 생성
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    //이메일 인증 처리
    public boolean verifyEmail(String email, String code) {
        // Redis에서 email Key에 해당하는 value를 가져옴
        String key = RedisKeys.getSignupCode(email);
        String redisCode = redisTemplate.opsForValue().get(key);

        // 로그 추가: Redis에 저장된 코드와 비교하려는 코드 출력
        log.info("Received code: {}, Redis code: {}", code, redisCode);

        // Redis에 저장된 인증 코드와 사용자가 입력한 인증 코드가 일치하는지 확인
        if(redisCode != null && redisCode.equals(code)) {
            // 5분 후에 만료되도록 설정
            redisTemplate.opsForValue().set(RedisKeys.getSignupCodeCheck(email), code, 5, TimeUnit.MINUTES);
            log.info("Email verification successful for: {}", email);
            return true;
        }
        log.warn("Email verification failed for: {}", email);
        return false;
    }

    public boolean isEmailVerified(String email) {
        String key = RedisKeys.getSignupCodeCheck(email);
        String code = redisTemplate.opsForValue().get(key);
        return code != null;
    }
}
