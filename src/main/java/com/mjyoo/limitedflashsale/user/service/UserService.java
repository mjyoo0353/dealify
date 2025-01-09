package com.mjyoo.limitedflashsale.user.service;

import com.mjyoo.limitedflashsale.auth.service.EmailVerificationService;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.user.dto.SignupRequestDto;
import com.mjyoo.limitedflashsale.user.dto.UserResponseDto;
import com.mjyoo.limitedflashsale.user.dto.UserListResponseDto;
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import com.mjyoo.limitedflashsale.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Value("${ADMIN_TOKEN}")
    private String adminToken;

    public void signup(SignupRequestDto requestDto) throws MessagingException {
        //이메일 인증 확인
        if(!emailVerificationService.isEmailVerified(requestDto.getEmail())) {
            throw new CustomException(ErrorCode.INVALID_EMAIL_VERIFICATION);
        }

        String username = requestDto.getUsername();
        String email = requestDto.getEmail();
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        //email 중복 확인
        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }

        //회원 중복 확인
        Optional<User> checkUsername = userRepository.findByUsername(username);
        if (checkUsername.isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_USERNAME);
        }

        //사용자 Role 확인
        UserRoleEnum role = UserRoleEnum.USER;
        if (requestDto.isAdmin()) {
            if (!adminToken.equals(requestDto.getAdminToken())) {
                throw new CustomException(ErrorCode.INVALID_ADMIN_TOKEN);
            }
            role = UserRoleEnum.ADMIN;
        }

        //사용자 등록
        User user = User.builder()
                .email(email)
                .username(username)
                .password(encodedPassword)
                .phoneNumber(requestDto.getPhoneNumber())
                .address(requestDto.getAddress())
                .role(role)
                .build();
        userRepository.save(user);
    }

    //마이페이지 조회
    public UserResponseDto getMyPage(User user) {
        User userInfo = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return getUserInfo(userInfo);
    }

    //회원 정보 조회
    public UserResponseDto getUserInfo(Long id, User user) {
        checkAdminRole(user);
        User userInfo = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return getUserInfo(userInfo);
    }

    //회원 리스트 조회
    public UserListResponseDto getUserList(User user) {
        checkAdminRole(user);

        List<User> userList = userRepository.findAll();
        List<UserResponseDto> userInfoList = new ArrayList<>();

        for (User userInfo : userList) {
            UserResponseDto userResponseDto = getUserInfo(userInfo);
            userInfoList.add(userResponseDto);
        }
        long totalUser = userRepository.count(); //전체 회원 수
        return new UserListResponseDto(userInfoList, totalUser);
    }

    private void checkAdminRole(User user) {
        if (!user.getRole().equals(UserRoleEnum.ADMIN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private UserResponseDto getUserInfo(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .role(user.getRole())
                .build();
    }

}
