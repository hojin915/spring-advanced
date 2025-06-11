package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    public void auth_회원가입_이메일_중복_예외처리() {
        //given
        SignupRequest request = new SignupRequest("test@example.com", "password", "USER");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        //when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> authService.signup(request));
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    public void auth_회원가입_정상작동_테스트() {
        //given
        when(jwtUtil.createToken(anyLong(), anyString(), eq(UserRole.USER))).thenReturn("mocked_jwt_token");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User savedUser = i.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        SignupRequest request = new SignupRequest("test@example.com", "password", "USER");

        //when
        SignupResponse response = authService.signup(request);

        //then
        assertEquals("mocked_jwt_token", response.getBearerToken());
    }

    @Test
    public void auth_로그인_정상작동_테스트() {
        //given
        when(jwtUtil.createToken(anyLong(), anyString(), eq(UserRole.USER))).thenReturn("mocked_jwt_token");
        SigninRequest request = new SigninRequest("test@example.com", "password");

        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setUserRole(UserRole.USER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        //when
        SigninResponse response = authService.signin(request);

        //then
        assertEquals("mocked_jwt_token", response.getBearerToken());
    }

    @Test
    public void auth_로그인_미가입_회원_예외처리() {
        //given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        SigninRequest request = new SigninRequest("test@example.com", "password");

        //when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> authService.signin(request));
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }

    @Test
    public void auth_로그인_비밀번호_불일치_예외처리() {
        //given
        SigninRequest request = new SigninRequest("test@example.com", "password");

        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setUserRole(UserRole.USER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        //when & then
        AuthException exception = assertThrows(AuthException.class, () -> authService.signin(request));
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }
}