package com.ichuvilin.bookshelf.internal.controller;

import com.ichuvilin.bookshelf.internal.dto.VerifyTokenRequest;
import com.ichuvilin.bookshelf.internal.dto.VerifyTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/internal/v1/auth")
@RequiredArgsConstructor
public class InternalAuthController {
    private final JwtDecoder jwtDecoder;

    @PostMapping("/verify")
    public ResponseEntity<VerifyTokenResponse> verifyToken(@Valid @RequestBody VerifyTokenRequest request) {
        try {
            Jwt jwt = jwtDecoder.decode(request.token());

            Long userId = Long.valueOf(jwt.getSubject());
            Instant expiresAt = jwt.getExpiresAt();

            return ResponseEntity.ok(new VerifyTokenResponse(true, userId, expiresAt, null));
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.ok(new VerifyTokenResponse(false, null, null, "invalid token"));
        }
    }
}
