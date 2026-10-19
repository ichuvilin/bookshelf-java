
package com.ichuvilin.bookshelf.internal;

import com.ichuvilin.bookshelf.AbstractIntegrationTest;
import com.ichuvilin.bookshelf.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "security.service.key=test-service-key")
class InternalEndpointsTest extends AbstractIntegrationTest {

    private static final String SERVICE_KEY = "test-service-key";

    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturn401WhenServiceKeyIsMissing() throws Exception {
        mockMvc.perform(post("/internal/v1/users/batch")
                        .header("X-Debug-Filter", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"ids":[1,2]}
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenServiceKeyIsInvalid() throws Exception {
        mockMvc.perform(post("/internal/v1/users/batch")
                        .header("X-Service-Key", "wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ids":[1,2]}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowRequestWithValidServiceKey() throws Exception {
        when(userService.getUsersById(List.of(1L, 2L)))
                .thenReturn(List.of());

        mockMvc.perform(post("/internal/v1/users/batch")
                        .header("X-Service-Key", SERVICE_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ids":[1,2]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users").isEmpty());

        verify(userService).getUsersById(List.of(1L, 2L));
    }

    @Test
    void shouldReturnValidWhenJwtIsValid() throws Exception {
        Instant expiresAt = Instant.now().plusSeconds(600);

        Jwt jwt = Jwt.withTokenValue("valid-token")
                .header("alg", "HS256")
                .subject("42")
                .expiresAt(expiresAt)
                .build();

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);

        mockMvc.perform(post("/internal/v1/auth/verify")
                        .header("X-Service-Key", SERVICE_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"valid-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void shouldReturnInvalidWhenJwtIsInvalid() throws Exception {
        when(jwtDecoder.decode("invalid-token"))
                .thenThrow(new BadJwtException("Invalid token"));

        mockMvc.perform(post("/internal/v1/auth/verify")
                        .header("X-Service-Key", SERVICE_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"invalid-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.error").value("invalid token"));
    }

    @Test
    void shouldReturn400WhenTokenIsBlank() throws Exception {
        mockMvc.perform(post("/internal/v1/auth/verify")
                        .header("X-Service-Key", SERVICE_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":" "}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void shouldReturn400WhenBatchIdsAreInvalid() throws Exception {
        mockMvc.perform(post("/internal/v1/users/batch")
                        .header("X-Service-Key", SERVICE_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ids":[1,0,-2]}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}
