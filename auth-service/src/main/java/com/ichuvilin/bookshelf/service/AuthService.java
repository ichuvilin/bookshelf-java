package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.api.model.request.LoginRequest;
import com.ichuvilin.bookshelf.api.model.request.RegisterRequest;
import com.ichuvilin.bookshelf.api.model.response.AuthResponse;
import com.ichuvilin.bookshelf.db.entity.User;
import com.ichuvilin.bookshelf.domain.UserPublic;
import com.ichuvilin.bookshelf.exception.EmailAlreadyExistsException;
import com.ichuvilin.bookshelf.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userService.existsByUsername(request.getUsername())) {
            throw new EmailAlreadyExistsException("Username already exists");
        }

        String passwdHash = passwordEncoder.encode(request.getPassword());
        User user = userService.create(request.getUsername(), request.getEmail(), passwdHash);

        String accessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(Math.toIntExact(jwtService.getAccessTokenTtlSeconds()))
                .user(UserPublic.toPublic(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userService.findByEmail(request.getEmail()).orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(Math.toIntExact(jwtService.getAccessTokenTtlSeconds()))
                .user(UserPublic.toPublic(user))
                .build();
    }
}
