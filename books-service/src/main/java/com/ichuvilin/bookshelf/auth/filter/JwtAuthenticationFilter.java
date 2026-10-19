package com.ichuvilin.bookshelf.auth.filter;

import com.ichuvilin.bookshelf.exception.AuthServiceUnavailableException;
import com.ichuvilin.bookshelf.integration.auth.AuthClient;
import com.ichuvilin.bookshelf.integration.auth.dto.VerifyTokenResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthClient authClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            VerifyTokenResponse result = authClient.verifyToken(token);

            if (result == null || !result.valid() || result.userId() == null) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            var authentication =
                    new UsernamePasswordAuthenticationToken(result.userId(), null, Collections.emptyList());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (AuthServiceUnavailableException exception) {
            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                {
                  "status": 503,
                  "error": "Service Unavailable",
                  "message": "Authentication service is temporarily unavailable"
                }
                """);
        }
    }
}
