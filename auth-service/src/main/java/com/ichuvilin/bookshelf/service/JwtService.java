package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.db.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.access-token-ttl}")
    private Duration accessTokenTTl;

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessTokenTTl);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("bookshelf-auth")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    public Long getAccessTokenTtlSeconds() {
        return accessTokenTTl.getSeconds();
    }
}
