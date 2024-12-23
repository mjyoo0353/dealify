package com.mjyoo.limitedflashsale.controller;

import com.mjyoo.limitedflashsale.dto.*;
import com.mjyoo.limitedflashsale.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    //회원가입
    @PostMapping("/user/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto requestDto, BindingResult bindingResult) throws MessagingException {
        if(bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }
        userService.signup(requestDto);
        return ResponseEntity.ok("회원가입에 성공했습니다.");
    }

    //회원 정보 조회
    @GetMapping("/user/{id}")
    public UserInfoDto getUserInfo(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    //회원 리스트 조회
    @GetMapping("/user/list")
    public UserInfoListDto getUserList() {
        return userService.getUserList();
    }
}
