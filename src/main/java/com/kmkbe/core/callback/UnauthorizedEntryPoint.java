package com.kmkbe.core.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.model.CommonResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        log.error("Unauthorized error: {}", authException.getMessage());
        try {
            response.addHeader("Content-Type", "application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            CommonResult<Object> body = new CommonResult<>().fail(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid login session, try to re-login again"
            );

            mapper.writeValue(response.getOutputStream(), body);
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException("Failed Authentication Response " + e.getMessage());
        }
    }
}
