package com.mjyoo.limitedflashsale.controller;

import com.mjyoo.limitedflashsale.dto.requestDto.SignupRequestDto;
import com.mjyoo.limitedflashsale.dto.responseDto.UserListResponseDto;
import com.mjyoo.limitedflashsale.dto.responseDto.UserResponseDto;
import com.mjyoo.limitedflashsale.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto requestDto) throws MessagingException {
        userService.signup(requestDto);
        return ResponseEntity.ok("회원가입에 성공했습니다.");
    }

    /*//로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        userService.login(requestDto, response);
        return ResponseEntity.ok("로그인에 성공했습니다.");
    }*/

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        userService.logout(response);
        return ResponseEntity.ok("로그아웃에 성공했습니다.");
    }

    //마이페이지 조회
    @GetMapping("/mypage")
    public ResponseEntity<UserResponseDto> getMyPage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserResponseDto user = userService.getMyPage(userDetails);
        return ResponseEntity.ok(user);
    }

    //회원 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserInfo(@PathVariable Long id) {
        UserResponseDto user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

    //회원 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<UserListResponseDto> getUserList() {
        UserListResponseDto userList = userService.getUserList();
        return ResponseEntity.ok(userList);
    }
}
