package com.ichuvilin.bookshelf.integration.auth;

import com.ichuvilin.bookshelf.exception.AuthServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AuthClientTest {

    private static final String BASE_URL = "http://auth-service";
    private static final String SERVICE_KEY = "test-service-key";
    private static final String VERIFY_PATH = "/internal/v1/auth/verify";
    private static final String USERS_BATCH_PATH = "/internal/v1/users/batch";

    private MockRestServiceServer server;
    private AuthClient authClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        authClient = new AuthClient(builder.build(), SERVICE_KEY);
    }

    @Test
    void getUsersByIdsReturnsUserSummariesAndSendsServiceKey() {
        server.expect(requestTo(BASE_URL + USERS_BATCH_PATH))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Service-Key", SERVICE_KEY))
                .andExpect(content().json("""
                        {"ids":[42,43]}
                        """))
                .andRespond(withSuccess("""
                        {
                          "users": [
                            {"id": 42, "username": "ivan"},
                            {"id": 43, "username": "anna"}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        var result = authClient.getUsersByIds(java.util.List.of(42L, 43L, 42L));

        assertEquals(2, result.size());
        assertEquals(42L, result.getFirst().id());
        assertEquals("ivan", result.getFirst().username());
        assertEquals(43L, result.getLast().id());
        assertEquals("anna", result.getLast().username());
        server.verify();
    }

    @Test
    void verifyTokenReturnsValidResponseAndSendsServiceKey() {
        server.expect(requestTo(BASE_URL + VERIFY_PATH))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Service-Key", SERVICE_KEY))
                .andExpect(content().json("""
                        {"token":"valid-jwt"}
                        """))
                .andRespond(withSuccess("""
                        {
                          "valid": true,
                          "userId": 42,
                          "expiresAt": "2026-10-20T12:00:00Z",
                          "error": null
                        }
                        """, MediaType.APPLICATION_JSON));

        var result = authClient.verifyToken("valid-jwt");

        assertTrue(result.valid());
        assertEquals(42L, result.userId());
        assertEquals("2026-10-20T12:00:00Z", result.expiresAt().toString());
        assertNull(result.error());
        server.verify();
    }

    @Test
    void verifyTokenReturnsInvalidResultForInvalidJwt() {
        server.expect(requestTo(BASE_URL + VERIFY_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "valid": false,
                          "userId": null,
                          "expiresAt": null,
                          "error": "invalid token"
                        }
                        """, MediaType.APPLICATION_JSON));

        var result = authClient.verifyToken("invalid-jwt");

        assertFalse(result.valid());
        assertNull(result.userId());
        assertNull(result.expiresAt());
        assertEquals("invalid token", result.error());
        server.verify();
    }

    @Test
    void verifyTokenThrowsWhenAuthServiceReturnsServerError() {
        server.expect(requestTo(BASE_URL + VERIFY_PATH))
                .andRespond(withServerError());

        assertThrows(
                AuthServiceUnavailableException.class,
                () -> authClient.verifyToken("some-jwt")
        );

        server.verify();
    }

    @Test
    void verifyTokenThrowsWhenResponseBodyIsEmpty() {
        server.expect(requestTo(BASE_URL + VERIFY_PATH))
                .andRespond(withSuccess());

        assertThrows(
                AuthServiceUnavailableException.class,
                () -> authClient.verifyToken("some-jwt")
        );

        server.verify();
    }

    @Test
    void verifyTokenThrowsWhenAuthServiceReturnsUnauthorized() {
        server.expect(requestTo(BASE_URL + VERIFY_PATH))
                .andRespond(withUnauthorizedRequest());

        assertThrows(
                AuthServiceUnavailableException.class,
                () -> authClient.verifyToken("some-jwt")
        );

        server.verify();
    }
}
