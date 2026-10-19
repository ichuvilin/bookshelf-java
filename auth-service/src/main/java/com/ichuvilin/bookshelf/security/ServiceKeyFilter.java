package com.ichuvilin.bookshelf.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ichuvilin.bookshelf.api.model.request.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Component
public class ServiceKeyFilter extends OncePerRequestFilter {
    private static final String HEADER_NAME = "X-Service-Key";
    private static final String INTERNAL_PATH = "/internal/v1/";

    private final String expectedKey;
    private final ObjectMapper objectMapper;

    public ServiceKeyFilter(@Value("${security.service.key}") String expectedKey, ObjectMapper objectMapper) {
        if (expectedKey == null || expectedKey.isBlank()) {
            throw new IllegalStateException("Service key must not be empty");
        }

        this.expectedKey = expectedKey;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI()
                .substring(request.getContextPath().length());

        return !path.startsWith(INTERNAL_PATH)
                && !path.equals("/internal/v1");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String providedKey = request.getHeader(HEADER_NAME);
        if (providedKey == null || providedKey.isBlank()) {
            writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "missing service key");
            return;
        }
        boolean valid = MessageDigest.isEqual(
                expectedKey.getBytes(StandardCharsets.UTF_8), providedKey.getBytes(StandardCharsets.UTF_8));

        if (!valid) {
            writeError(response, request, HttpServletResponse.SC_FORBIDDEN, "invalid service key");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, HttpServletRequest request, int status, String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse body = new ErrorResponse(
                Instant.now(), status, status == 401 ? "Unauthorized" : "Forbidden", message, request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
