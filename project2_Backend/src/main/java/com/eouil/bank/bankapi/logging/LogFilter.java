package com.eouil.bank.bankapi.logging;

import com.eouil.bank.bankapi.utils.JwtUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class LogFilter implements Filter {

    private static final String TRACE_ID = "traceId";
    private static final String USER_ID = "userId";

    @Override
    public void doFilter(
            jakarta.servlet.ServletRequest request,
            jakarta.servlet.ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            // 1. traceId 생성
            String traceId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID, traceId);

            // 2. Authorization 헤더에서 userId 추출
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String userId = JwtUtil.validateTokenAndGetUserId(token);
                MDC.put(USER_ID, userId);
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("[MDC Filter Error] {}", e.getMessage());
            chain.doFilter(request, response);
        } finally {
            MDC.clear(); // 꼭 해줘야 메모리 누수 방지됨
        }
    }
}
