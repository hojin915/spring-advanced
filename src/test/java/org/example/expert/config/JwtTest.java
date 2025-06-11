package org.example.expert.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JwtTest {

    JwtFilter jwtFilter;

    @Autowired
    JwtUtil jwtUtil;

    @Test
    public void JwtUtil_토큰_생성_속성_조회(){
        //given
        Long userId = 1L;
        String email = "test@example.com";
        UserRole userRole = UserRole.USER;

        //when
        String token = jwtUtil.createToken(userId, email, userRole);
        String subStringToken = jwtUtil.substringToken(token);
        Claims claims = jwtUtil.extractClaims(subStringToken);

        //then
        assertSame(userId, Long.parseLong(claims.getSubject()));
        assertEquals(email, claims.get("email"));
        assertEquals(userRole, UserRole.valueOf(claims.get("userRole", String.class)));
    }

    @Test
    public void JwtUtil_substringToken_입력값_조건_불만족시_예외처리() {
        //given
        String nullToken = null;
        String blankToken = "this token contains blank";
        String noPrefixToken = "ThisTokenHasNoPrefix";
        //when & then
        ServerException exceptionNull = assertThrows(ServerException.class, () -> jwtUtil.substringToken(nullToken));
        ServerException exceptionBlank = assertThrows(ServerException.class, () -> jwtUtil.substringToken(blankToken));
        ServerException exceptionPrefix = assertThrows(ServerException.class, () -> jwtUtil.substringToken(noPrefixToken));
        assertEquals("Not Found Token", exceptionNull.getMessage());
        assertEquals("Not Found Token", exceptionBlank.getMessage());
        assertEquals("Not Found Token", exceptionPrefix.getMessage());
    }

    @Test
    public void JwtFilter_Admin_요청_경로_매칭실패() throws ServletException, IOException {
        //given
        jwtFilter = new JwtFilter(jwtUtil);

        Long userId = 1L;
        String email = "admin@example.com";
        UserRole userRole = UserRole.USER;

        String token = jwtUtil.createToken(userId, email, userRole);

        String url = "/admins";
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", url);
        request.addHeader("Authorization", token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        //when
        jwtFilter.doFilter(request, response, filterChain);

        //then
        assertEquals(MockHttpServletResponse.SC_OK, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void JwtFilter_Admin_요청_경로_예외발생() throws ServletException, IOException {
        //given
        jwtFilter = new JwtFilter(jwtUtil);

        Long userId = 1L;
        String email = "admin@example.com";
        UserRole userRole = UserRole.USER;

        String token = jwtUtil.createToken(userId, email, userRole);

        String url = "/admin";
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", url);
        request.addHeader("Authorization", token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        //when
        jwtFilter.doFilter(request, response, filterChain);

        //then
        assertEquals(MockHttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals("관리자 권한이 없습니다.", response.getErrorMessage());

        verify(filterChain, never()).doFilter(any(), any());
    }
}