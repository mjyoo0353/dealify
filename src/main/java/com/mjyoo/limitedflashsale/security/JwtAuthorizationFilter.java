package com.mjyoo.limitedflashsale.security;

import com.mjyoo.limitedflashsale.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    //요청이 들어올 때마다 doFilterInternal 메서드가 호출되어 JWT를 처리
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("Processing request: {}", request.getRequestURI());
        //헤더에서 JWT를 가져옴
        String tokenValue = jwtUtil.getJwtFromHeader(request);

        //JWT가 존재하면 유효성 검사를 수행
        if(StringUtils.hasText(tokenValue)){
            //유효하지 않다면 에러 출력
            if (!jwtUtil.validateToken(tokenValue)) {
                log.error("Token Error");
                return;
            }
            //유효하다면 사용자 정보를 추출
            Claims info = jwtUtil.getUserInfoFromToken(tokenValue);
            log.info("Extracted username: " + info.getSubject());

            try {
                //JWT에서 추출한 사용자 정보를 바탕으로 인증 처리
                setAuthentication(info.getSubject());
            } catch (Exception e) {
                log.error(e.getMessage());
                return;
            }
        }
        //JWT 검증과 인증이 완료되면 필터 체인을 이어서 진행
        filterChain.doFilter(request, response);
    }

    //사용자 인증 설정
    private void setAuthentication(String email) {
        //SecurityContext는 현재 사용자 정보를 저장하는 곳
        //빈 SecurityContext를 생성하여 인증 정보를 설정할 준비
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        //사용자 정보가 담긴 인증 객체 생성
        Authentication authentication = createAuthentication(email);
        //SecurityContext에 인증 정보를 설정
        context.setAuthentication(authentication);
        //SecurityContext를 SecurityContextHolder에 설정
        SecurityContextHolder.setContext(context);
    }

    //인증 객체 생성
    private Authentication createAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        //userDetails(사용자정보)와 권한을 설정
        //해당 객체는 Spring Security의 인증 메커니즘에서 사용됨
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
