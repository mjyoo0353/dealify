package com.mjyoo.limitedflashsale.service;

import com.mjyoo.limitedflashsale.dto.requestDto.LoginRequestDto;
import com.mjyoo.limitedflashsale.dto.requestDto.SignupRequestDto;
import com.mjyoo.limitedflashsale.dto.responseDto.UserResponseDto;
import com.mjyoo.limitedflashsale.dto.responseDto.UserListResponseDto;
import com.mjyoo.limitedflashsale.entity.User;
import com.mjyoo.limitedflashsale.entity.UserRoleEnum;
import com.mjyoo.limitedflashsale.jwt.JwtUtil;
import com.mjyoo.limitedflashsale.repository.UserRepository;
import com.mjyoo.limitedflashsale.security.UserDetailsImpl;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
    private final JwtUtil jwtUtil;

    // ADMIN_TOKEN
    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

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

    /*//로그인
    @Transactional
    public void login(LoginRequestDto requestDto, HttpServletResponse response) {
        //사용자 조회
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("email not found"));

        //비밀번호 일치 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("password not matched");
        }
        //JWT 토큰 생성
        String token = jwtUtil.createToken(user.getEmail(), user.getRole());
        //JWT 토큰을 Response Header에 추가
        response.addHeader("Authorization", "Bearer " + token);
    }*/

    //로그아웃
    public void logout(HttpServletResponse response) {
        //쿠키에서 JWT 삭제하기 위해 쿠키 만료 설정
        Cookie cookie = new Cookie("Authorization", null); // "Authorization"은 쿠키 이름
        cookie.setMaxAge(0); //쿠키 만료
        cookie.setPath("/"); //모든 경로에서 쿠키 접근 가능하도록 설정
        cookie.setHttpOnly(true); //JavaScript로 쿠키 접근 불가
        response.addCookie(cookie);
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
