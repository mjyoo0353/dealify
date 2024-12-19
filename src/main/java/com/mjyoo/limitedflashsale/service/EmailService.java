package com.mjyoo.limitedflashsale.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    private static final String authKey = createKey();

    public String sendAuthEmail(String email) throws MessagingException {
        MimeMessage message = createAuthEmail(email);
        try {
            emailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("Failed to send email to " + email, e);
        }
        return "이메일이 성공적으로 전송되었습니다.";
    }

    public MimeMessage createAuthEmail(String email) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("Dealify 회원가입 인증 메일입니다.");

        //이메일 본문 내용
        String content =
                "아래의 코드를 입력하여 회원가입을 완료해주세요.\n" +
                "인증번호는 " + authKey + " 입니다.";

        message.setText(content, "UTF-8", "html");
        return message;
    }

    public static String createKey() {
        Random r = new Random();
        StringBuilder key = new StringBuilder();
        for(int i = 0; i < 6; i++) {
            key.append(r.nextInt(10));
        }
        return key.toString();
    }

}
