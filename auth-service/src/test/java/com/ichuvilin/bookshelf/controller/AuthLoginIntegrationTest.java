package com.ichuvilin.bookshelf.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.ichuvilin.bookshelf.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthLoginIntegrationTest extends AbstractIntegrationTest {

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String PROTECTED_TEST_URL = "/api/v1/protected-test";
    private static final String EMAIL = "reader@example.com";
    private static final String PASSWORD = "StrongPassword123!";

    @Test
    void login_shouldReturnTokenForValidCredentials() throws Exception {
        registerUser();

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.user.email").value(EMAIL))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void login_shouldReturnUnauthorizedForWrongPassword() throws Exception {
        registerUser();

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(EMAIL, "WrongPassword123!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_shouldReturnUnauthorizedForUnknownEmail() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("missing@example.com", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_shouldReturnBadRequestForInvalidEmail() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("not-an-email", PASSWORD)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectedEndpoint_shouldRejectMissingToken() throws Exception {
        mockMvc.perform(get(PROTECTED_TEST_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldRejectInvalidToken() throws Exception {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.invalid-signature";

        mockMvc.perform(get(PROTECTED_TEST_URL)
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldRejectExpiredToken() throws Exception {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("bookshelf-auth")
                .subject("1")
                .issuedAt(now.minusSeconds(120))
                .expiresAt(now.minusSeconds(60))
                .build();

        String expiredToken = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();

        mockMvc.perform(get(PROTECTED_TEST_URL)
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void generatedToken_shouldPassJwtAuthentication() throws Exception {
        registerUser();

        String response = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode body = objectMapper.readTree(response);
        String token = body.get("accessToken").asText();

        // This path is intentionally unmapped: 404 means the request passed the security filter.
        mockMvc.perform(get(PROTECTED_TEST_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private void registerUser() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest("reader", EMAIL, PASSWORD)))
                .andExpect(status().isCreated());
    }

    private String registerRequest(String username, String email, String password) throws Exception {
        return objectMapper.writeValueAsString(new RegisterPayload(username, email, password));
    }

    private String loginRequest(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(new LoginPayload(email, password));
    }

    private record RegisterPayload(String username, String email, String password) {
    }

    private record LoginPayload(String email, String password) {
    }
}
