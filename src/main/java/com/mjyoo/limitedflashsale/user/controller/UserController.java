package com.mjyoo.limitedflashsale.user.controller;

import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.user.dto.SignupRequestDto;
import com.mjyoo.limitedflashsale.user.dto.UserListResponseDto;
import com.mjyoo.limitedflashsale.user.dto.UserResponseDto;
import com.mjyoo.limitedflashsale.auth.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto requestDto) throws MessagingException {
        userService.signup(requestDto);
        return ResponseEntity.ok(ApiResponse.success("회원가입에 성공했습니다."));
    }

    //마이페이지 조회
    @GetMapping("/mypage")
    public ResponseEntity<ApiResponse<?>> getMyPage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED.getMessage()));
        }
        UserResponseDto user = userService.getMyPage(userDetails);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    //회원 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserInfo(@PathVariable Long userId) {
        UserResponseDto user = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    //회원 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<UserListResponseDto>> getUserList() {
        UserListResponseDto userList = userService.getUserList();
        return ResponseEntity.ok(ApiResponse.success(userList));
    }
}
