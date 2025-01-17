package com.mjyoo.limitedflashsale.auth.service;

import com.mjyoo.limitedflashsale.auth.dto.LoginRequestDto;
import com.mjyoo.limitedflashsale.auth.dto.LoginResponseDto;
import com.mjyoo.limitedflashsale.auth.dto.RefreshTokenRequestDto;
import com.mjyoo.limitedflashsale.auth.security.JwtUtil;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.common.exception.TokenCreationException;
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import com.mjyoo.limitedflashsale.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    //로그인
    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto, HttpServletResponse response) {
        //사용자 조회
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //비밀번호 일치 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        try {
            //Access Token 발급
            String accessToken = jwtUtil.createAccessToken(user.getEmail(), user.getRole());
            response.addHeader("Authorization", JwtUtil.BEARER_PREFIX + accessToken);

            // Refresh Token 발급 및 Redis에 저장
            String refreshToken = createAndStoreRefreshTokenInRedis(response, user);

            // 토큰 만료 시간 출력
            jwtUtil.printTokenDetails(accessToken);
            jwtUtil.printTokenDetails(refreshToken);

            return new LoginResponseDto(user.getEmail(), JwtUtil.BEARER_PREFIX + refreshToken);
        } catch (TokenCreationException e) {
            throw new CustomException(ErrorCode.TOKEN_CREATION_ERROR);
        }
    }

    //Access Token 재발급
    public String refreshToken(RefreshTokenRequestDto requestDto, HttpServletRequest request, HttpServletResponse response) {

        // Refresh Token이 없을 경우 예외 처리
        String refreshToken = requestDto.getRefreshToken();
        if(refreshToken == null || refreshToken.isEmpty()) {
            throw new CustomException(ErrorCode.MISSING_REFRESH_TOKEN);
        }

        // Redis에 저장된 Refresh Token이 있는지 확인
        String redisKey = "refresh_token:" + refreshToken;
        redisTemplate.opsForValue().get(redisKey);

        // JWT에서 사용자 정보 추출
        Claims claims = jwtUtil.getUserInfoFromToken(refreshToken);
        String username = claims.getSubject();
        String newAccessToken = jwtUtil.createAccessToken(username, UserRoleEnum.USER);
        log.info("Generated new Access Token: {}", newAccessToken);

        return newAccessToken;
    }

    //로그아웃
    public void logout(HttpServletResponse response) {
        //쿠키에서 JWT 삭제하기 위해 쿠키 만료 설정
        Cookie cookie = new Cookie("Authorization", null); // "Authorization"은 쿠키 이름
        cookie.setMaxAge(0); //쿠키 만료
        cookie.setPath("/"); //모든 경로에서 쿠키 접근 가능하도록 설정
        cookie.setHttpOnly(true); //JavaScript로 쿠키 접근 불가
        response.addCookie(cookie);
    }

    private String createAndStoreRefreshTokenInRedis(HttpServletResponse response, User user) {
        //refresh Token 발급
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), user.getRole());

        // Redis에 Refresh Token 저장
        String redisKey = "email:" + user.getEmail();
        redisTemplate.opsForValue().set(redisKey, refreshToken, JwtUtil.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);
        log.info("Generated Refresh Token: {}", refreshToken);

        // Refresh Token을 Response Header에 추가
        response.addHeader("Refresh-Token", JwtUtil.BEARER_PREFIX + refreshToken);
        return refreshToken;
    }
}
