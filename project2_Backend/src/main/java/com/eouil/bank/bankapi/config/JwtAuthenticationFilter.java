package com.eouil.bank.bankapi.config;

import com.eouil.bank.bankapi.domains.User;
import com.eouil.bank.bankapi.repositories.UserRepository;
import com.eouil.bank.bankapi.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public JwtAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 로그인, 회원가입, 리프레시 요청은 필터 통과
        // 인증이 필요 없는 경로는 필터에서 제외
        if (path.equals("/api/login")
                || path.equals("/api/join")
                || path.equals("/api/refresh")
                || path.equals("/api/logout")
                || path.startsWith("/api/mfa/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("Authorization");

        // 토큰이 없으면 인증 안된 상태로 통과 (403 발생 안 하게)
        if (token == null || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = token.replace("Bearer ", "");
        try {
            String userId = JwtUtil.validateTokenAndGetUserId(token);

            //유저 검증
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    user.getUserId(), null, null
            );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (Exception e) {
            //인증 실패 → 명시적으로 401 Unauthorized 반환
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
