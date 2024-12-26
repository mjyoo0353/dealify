package com.mjyoo.limitedflashsale.jwt;

import com.mjyoo.limitedflashsale.entity.UserRoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
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
public class JwtUtil {
    //Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    //Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";
    //Token 만료 시간
    private final long TOKEN_EXPIRATION_MS = 60 * 60 * 1000L; // 1시간

    //WT를 암호화하거나 검증할 때 사용되는 비밀 키
    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey;
    //암호화된 secretKey를 실제 사용할 수 있는 Key 객체로 변환
    private Key key;
    //JWT를 생성할 때 사용할 암호화 알고리즘 - HMAC SHA-256 사용
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    //로그 설정
    public static final Logger logger = LoggerFactory.getLogger("JWT 관련 로그");


    //secretKey를 디코딩해서 key 변수에 할당
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    //JWT 토큰 생성
    public String createToken(String username, UserRoleEnum role) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username) //사용자 식별자값 (ID)
                        .claim(AUTHORIZATION_KEY, role) //사용자 권한
                        .setIssuedAt(date) //토큰 발급일
                        .setExpiration(new Date(date.getTime() + TOKEN_EXPIRATION_MS)) //생성되는 시간 기준으로 만료 시간 계산
                        .signWith(key, signatureAlgorithm) //비밀 키와 암호화 알고리즘을 사용해 서명
                        .compact();
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
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

}
