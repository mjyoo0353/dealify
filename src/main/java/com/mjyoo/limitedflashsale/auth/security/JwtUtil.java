package com.mjyoo.limitedflashsale.auth.security;

import com.mjyoo.limitedflashsale.common.exception.TokenCreationException;
import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    //Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    //Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";
    //Token 만료 시간
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

    //암호화된 secretKey를 실제 사용할 수 있는 Key 객체로 변환
    protected Key key;
    //JWT를 생성할 때 사용할 암호화 알고리즘 - HMAC SHA-256 사용
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    //로그 설정
    public static final Logger logger = LoggerFactory.getLogger("JWT 관련 로그");

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    public static long getRefreshTokenExpirationTime() {
        return REFRESH_TOKEN_EXPIRATION_TIME;
    }

    @PostConstruct
    public void init() { //secretKey를 디코딩해서 key 변수에 할당
        //JWT를 암호화하거나 검증할 때 사용되는 비밀 키를 Base64 디코딩하여 Key 객체로 변환
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    //JWT 엑세스 토큰 생성
    public String createAccessToken(String username, UserRoleEnum role) {
        try {
            Date date = new Date();
            Date expirationDate = new Date(date.getTime() + ACCESS_TOKEN_EXPIRATION_TIME); //생성되는 시간 기준으로 만료 시간 계산

            return Jwts.builder()
                    .setSubject(username) //사용자 식별자값 (ID)
                    .claim(AUTHORIZATION_KEY, role) //사용자 권한
                    .setIssuedAt(date) //토큰 발급일
                    .setExpiration(expirationDate) // 만료 시간 설정
                    .signWith(key, signatureAlgorithm) //비밀 키와 암호화 알고리즘을 사용해 서명
                    .compact();
        } catch (Exception e) {
            throw new TokenCreationException("AccessToken 생성에 실패했습니다. : " + e.getMessage(), e);
        }
    }

    //JWT 리프레시 토큰 생성
    public String createRefreshToken(String username, UserRoleEnum role) {
        try {
            Date date = new Date();
            Date expirationDate = new Date(date.getTime() + REFRESH_TOKEN_EXPIRATION_TIME); //생성되는 시간 기준으로 만료 시간 계산

            return Jwts.builder()
                    .setSubject(username) //사용자 식별자값 (ID)
                    .claim(AUTHORIZATION_KEY, role) //사용자 권한
                    .setIssuedAt(date) //토큰 발급일
                    .setExpiration(expirationDate)  // 만료 시간 설정
                    .signWith(key, signatureAlgorithm) //비밀 키와 암호화 알고리즘을 사용해 서명
                    .compact();
        } catch (Exception e) {
            throw new TokenCreationException("RefreshToken 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    // HTTP 요청 header에서 JWT 가져오기
    public String getJwtFromHeader(HttpServletRequest request) {
        //Authorization 헤더에서 "Bearer "로 시작하는 값을 읽어와 토큰만 반환하도록 함
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    //JWT 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) { //잘못된 서명
            logger.error("invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) { //만료된 토큰
            logger.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) { //지원되지 않는 JWT 토큰
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) { //토큰이 잘못되거나 비어있는 경우
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    //JWT 토큰에서 사용자 정보 가져오기, 파싱된 JWT 내용을 반환함
    public Claims getUserInfoFromToken(String token) {
        try{
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e){
            logger.error("Expired JWT token: {}", e.getMessage());
            return e.getClaims(); //만료된 토큰일 경우, JWT 내용을 반환
        } catch (JwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public void printTokenDetails(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Date expiration = claims.getExpiration();
        logger.info("----Current Time: " + new Date());
        logger.info("Token Expiration: " + expiration);
    }

}
