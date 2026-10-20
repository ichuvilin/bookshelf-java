package com.ichuvilin.bookshelf.auth.filter;

import com.ichuvilin.bookshelf.exception.AuthServiceUnavailableException;
import com.ichuvilin.bookshelf.integration.auth.AuthClient;
import com.ichuvilin.bookshelf.integration.auth.dto.VerifyTokenResponse;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AuthClient authClient;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(authClient);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesRequestWhenTokenIsValid() throws ServletException, IOException {
        var request = requestWithAuthorization("Bearer valid-jwt");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        when(authClient.verifyToken("valid-jwt")).thenReturn(
                new VerifyTokenResponse(true, 42L, Instant.parse("2026-10-20T12:00:00Z"), null)
        );

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(42L, authentication.getPrincipal());
        assertTrue(authentication.isAuthenticated());
        assertNotNull(chain.getRequest());
        verify(authClient).verifyToken("valid-jwt");
    }

    @Test
    void continuesUnauthenticatedWhenTokenIsInvalid() throws ServletException, IOException {
        var request = requestWithAuthorization("Bearer invalid-jwt");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        when(authClient.verifyToken("invalid-jwt")).thenReturn(
                new VerifyTokenResponse(false, null, null, "invalid token")
        );

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
        verify(authClient).verifyToken("invalid-jwt");
    }

    @Test
    void returns503AndStopsChainWhenAuthServiceIsUnavailable() throws ServletException, IOException {
        var request = requestWithAuthorization("Bearer valid-jwt");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        when(authClient.verifyToken("valid-jwt"))
                .thenThrow(new AuthServiceUnavailableException("service unavailable"));

        filter.doFilter(request, response, chain);

        assertEquals(503, response.getStatus());
        assertTrue(response.getContentAsString().contains("Authentication service is temporarily unavailable"));
        assertNull(chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void skipsAuthClientWhenAuthorizationHeaderIsMissing() throws ServletException, IOException {
        var request = new MockHttpServletRequest("GET", "/api/v1/books");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        verifyNoInteractions(authClient);
    }

    @Test
    void skipsAuthClientWhenBearerTokenIsEmpty() throws ServletException, IOException {
        var request = requestWithAuthorization("Bearer    ");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        verifyNoInteractions(authClient);
    }

    @Test
    void invalidResponseWithoutUserIdDoesNotAuthenticateRequest() throws ServletException, IOException {
        var request = requestWithAuthorization("Bearer strange-response");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        when(authClient.verifyToken("strange-response")).thenReturn(
                new VerifyTokenResponse(true, null, Instant.parse("2026-10-20T12:00:00Z"), null)
        );

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }

    private MockHttpServletRequest requestWithAuthorization(String authorization) {
        var request = new MockHttpServletRequest("GET", "/api/v1/books");
        request.addHeader("Authorization", authorization);
        return request;
    }
}
