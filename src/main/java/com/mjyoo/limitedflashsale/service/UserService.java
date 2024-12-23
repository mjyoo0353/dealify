package com.mjyoo.limitedflashsale.service;

import com.mjyoo.limitedflashsale.dto.SignupRequestDto;
import com.mjyoo.limitedflashsale.dto.UserInfoDto;
import com.mjyoo.limitedflashsale.dto.UserInfoListDto;
import com.mjyoo.limitedflashsale.entity.User;
import com.mjyoo.limitedflashsale.repository.UserRepository;
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

        //사용자 등록
        User user = User.builder()
                .email(email)
                .username(username)
                .password(encodedPassword)
                .phoneNumber(requestDto.getPhoneNumber())
                .address(requestDto.getAddress())
                .isEmailVerified(false) //이메일 인증 전 상태
                .build();
        userRepository.save(user);
    }

    //회원 정보 조회
    public UserInfoDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        return getUserInfoDto(user);
    }

    //회원 리스트 조회
    public UserInfoListDto getUserList() {
        List<User> userList = userRepository.findAll();
        List<UserInfoDto> userInfoList = new ArrayList<>();

        for (User user : userList) {
            UserInfoDto userInfoDto = getUserInfoDto(user);
            userInfoList.add(userInfoDto);
        }
        long totalUser = userRepository.count(); //전체 회원 수
        return new UserInfoListDto(userInfoList, totalUser);
    }

    private UserInfoDto getUserInfoDto(User user) {
        return UserInfoDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .isEmailVerified(user.isEmailVerified())
                .build();
    }

}
