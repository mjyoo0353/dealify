package com.mjyoo.limitedflashsale.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjyoo.limitedflashsale.auth.dto.LoginRequestDto;
import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    /*UsernamePasswordAuthenticationFilter을 그대로 사용하면 session 방식이므로 직접 사용하지 않고
    JWT 인증 방식에 맞게 customized filter 사용
    로그인 처리는 controller와 service단에서 구현안하고 filter단에서 구현함*/

    //JWT 토큰 생성을 위한 JwtUtil 주입
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/user/login");
    }

    //로그인 요청을 처리 (사용자 정보를 읽고 인증 시도)
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("로그인 시도");
        try { //request.getInputStream()을 통해 request body에 있는 JSON 데이터를 읽어와서 LoginRequestDto 객체로 변환
            LoginRequestDto requestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);
            return getAuthenticationManager().authenticate( //인증 매니저에게 인증을 요청
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getEmail(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    //인증 성공시 successfulAuthentication 메서드 실행
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.info("로그인 성공 및 JWT 생성");
        //UserDetailsImpl을 통해 사용자 정보를 가져옴
        String email = ((UserDetailsImpl) authResult.getPrincipal()).getEmail();
        UserRoleEnum role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getRole();

        //사용자 정보를 바탕으로 JWT 토큰 생성
        String token = jwtUtil.createToken(email, role);
        //생성된 JWT을 응답 헤더(Authorization)에 추가
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);
    }

    //인증 실패시 unsuccessfulAuthentication 메서드 실행
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("로그인 실패");
        //인증 실패시 401 status code 반환
        response.setStatus(401);
    }
}
