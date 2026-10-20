package com.ichuvilin.bookshelf.integration.auth;

import com.ichuvilin.bookshelf.exception.AuthServiceUnavailableException;
import com.ichuvilin.bookshelf.integration.auth.dto.VerifyTokenRequest;
import com.ichuvilin.bookshelf.integration.auth.dto.UserBatchRequest;
import com.ichuvilin.bookshelf.integration.auth.dto.AuthUserResponse;
import com.ichuvilin.bookshelf.integration.auth.dto.VerifyTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClientException;

import java.util.Collection;
import java.util.List;

@Component
public class AuthClient {
    private static final String VERIFY_TOKEN_PATH = "/internal/v1/auth/verify";
    private static final String USERS_BATCH_PATH = "/internal/v1/users/batch";

    private final RestClient restClient;
    private final String secretKey;

    public AuthClient(RestClient authServiceRestClient, @Value("${auth.service.key}") String secretKey) {
        this.restClient = authServiceRestClient;
        this.secretKey = secretKey;
    }

    public List<AuthUserResponse> getUsersByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        try {
            List<AuthUserResponse> response = restClient
                    .post()
                    .uri(USERS_BATCH_PATH)
                    .header("X-Service-Key", secretKey)
                    .body(new UserBatchRequest(userIds.stream().distinct().toList()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null) {
                throw new AuthServiceUnavailableException("Auth service returned an empty batch user response");
            }

            return response;
        } catch (RestClientException ex) {
            throw new AuthServiceUnavailableException("Failed to fetch users from auth service", ex);
        }
    }

    public VerifyTokenResponse verifyToken(String token) {
        try {
            VerifyTokenResponse response = restClient
                    .post()
                    .uri(VERIFY_TOKEN_PATH)
                    .header("X-Service-Key", secretKey)
                    .body(new VerifyTokenRequest(token))
                    .retrieve()
                    .body(VerifyTokenResponse.class);

            if (response == null) {
                throw new AuthServiceUnavailableException("Auth service returned an empty response");
            }

            return response;
        } catch (RestClientException ex) {
            throw new AuthServiceUnavailableException("Failed to communicate with auth service", ex);
        }
    }
}
