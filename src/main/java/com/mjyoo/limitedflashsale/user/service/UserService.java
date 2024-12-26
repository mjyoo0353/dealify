package com.mjyoo.limitedflashsale.user.service;

import com.mjyoo.limitedflashsale.common.config.EnvironmentConfig;
import com.mjyoo.limitedflashsale.user.dto.SignupRequestDto;
import com.mjyoo.limitedflashsale.user.dto.UserResponseDto;
import com.mjyoo.limitedflashsale.user.dto.UserListResponseDto;
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import com.mjyoo.limitedflashsale.user.repository.UserRepository;
import com.mjyoo.limitedflashsale.auth.security.UserDetailsImpl;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
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
    private final EnvironmentConfig environmentConfig;
    private String ADMIN_TOKEN;

    public void signup(SignupRequestDto requestDto) throws MessagingException {
        String username = requestDto.getUsername();
        String email = requestDto.getEmail();
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        //email 중복 확인
        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 Email 입니다.");
        }

        //회원 중복 확인
        Optional<User> checkUsername = userRepository.findByUsername(username);
        if (checkUsername.isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 사용자 이름입니다.");
        }

        //사용자 Role 확인
        UserRoleEnum role = UserRoleEnum.USER;
        if (requestDto.isAdmin()) {
            ADMIN_TOKEN = environmentConfig.getAdminToken();
            if (!ADMIN_TOKEN.equals(requestDto.getAdminToken())) {
                throw new IllegalArgumentException("관리자 암호가 틀려 등록이 불가능합니다.");
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
                .isEmailVerified(false) //이메일 인증 전 상태
                .role(role)
                .build();
        userRepository.save(user);
    }

    //마이페이지 조회
    public UserResponseDto getMyPage(UserDetailsImpl userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        return getUserInfo(user);
    }

    //회원 정보 조회
    public UserResponseDto getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        return getUserInfo(user);
    }

    //회원 리스트 조회
    public UserListResponseDto getUserList() {
        List<User> userList = userRepository.findAll();
        List<UserResponseDto> userInfoList = new ArrayList<>();

        for (User user : userList) {
            UserResponseDto userResponseDto = getUserInfo(user);
            userInfoList.add(userResponseDto);
        }
        long totalUser = userRepository.count(); //전체 회원 수
        return new UserListResponseDto(userInfoList, totalUser);
    }

    private UserResponseDto getUserInfo(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .isEmailVerified(user.isEmailVerified())
                .role(user.getRole())
                .build();
    }

}
