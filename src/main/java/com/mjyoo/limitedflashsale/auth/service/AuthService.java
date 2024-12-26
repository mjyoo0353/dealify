package com.mjyoo.limitedflashsale.auth.service;

import com.mjyoo.limitedflashsale.auth.repository.EmailVerificationRepository;
import com.mjyoo.limitedflashsale.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;

    /*//로그인
    @Transactional
    public void login(LoginRequestDto requestDto, HttpServletResponse response) {
        //사용자 조회
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("email not found"));

        //비밀번호 일치 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("password not matched");
        }
        //JWT 토큰 생성
        String token = jwtUtil.createToken(user.getEmail(), user.getRole());
        //JWT 토큰을 Response Header에 추가
        response.addHeader("Authorization", "Bearer " + token);
    }*/

    //로그아웃
    public void logout(HttpServletResponse response) {
        //쿠키에서 JWT 삭제하기 위해 쿠키 만료 설정
        Cookie cookie = new Cookie("Authorization", null); // "Authorization"은 쿠키 이름
        cookie.setMaxAge(0); //쿠키 만료
        cookie.setPath("/"); //모든 경로에서 쿠키 접근 가능하도록 설정
        cookie.setHttpOnly(true); //JavaScript로 쿠키 접근 불가
        response.addCookie(cookie);
    }


}
