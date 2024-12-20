
package com.mjyoo.limitedflashsale.config;

import com.mjyoo.limitedflashsale.jwt.JwtUtil;
import com.mjyoo.limitedflashsale.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // CSRF 설정
        httpSecurity.csrf((csrf) -> csrf.disable());

        httpSecurity.authorizeHttpRequests((authorizeHttpRequests) ->
                authorizeHttpRequests
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // resources 접근 허가
                        .requestMatchers("/").permitAll() // 메인 페이지 접근 허가
                        .requestMatchers("/api/**").permitAll() // '/api/'로 시작하는 요청 모두 접근 허가
                        .anyRequest().authenticated() // 그 외 모든 요청 인증 요구
        );

        //로그인 사용
        //httpSecurity.formLogin(Customizer.withDefaults()); //Spring Security에서 제공하는 기본 로그인 화면
        /*httpSecurity.formLogin((formLogin) ->
                formLogin.loginPage("/api/user/login-page") //로그인 View 제공
                        .loginProcessingUrl("/api/user/login") //로그인 처리 POST
                        .defaultSuccessUrl("/") //로그인 처리 후 성공 시 URL
                        .failureUrl("/api/user/login-page?error") //실패 시 URL
                        .permitAll()
        );*/
        return httpSecurity.build();
    }

}

