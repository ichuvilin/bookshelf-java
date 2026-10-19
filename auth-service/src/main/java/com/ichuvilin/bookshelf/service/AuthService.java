package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.api.model.request.LoginRequest;
import com.ichuvilin.bookshelf.api.model.request.RegisterRequest;
import com.ichuvilin.bookshelf.api.model.response.AuthResponse;
import com.ichuvilin.bookshelf.db.entity.User;
import com.ichuvilin.bookshelf.domain.UserPublic;
import com.ichuvilin.bookshelf.exception.EmailAlreadyExistsException;
import com.ichuvilin.bookshelf.exception.InvalidCredentialsException;
import com.ichuvilin.bookshelf.exception.UsernameAlreadyExistsException;
import com.ichuvilin.bookshelf.util.Constants;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtService jwtService;

    @Transactional(rollbackFor = Exception.class)
    public AuthResponse register(RegisterRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userService.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }

        String passwdHash = passwordEncoder.encode(request.getPassword());
        User user;
        try {
            user = userService.create(request.getUsername(), request.getEmail(), passwdHash);
        } catch (DataIntegrityViolationException ex) {
            String constraint = findConstraintName(ex);

            if (Constants.UNIQUE_EMAIL_CONSTRAINT_NAME.equals(constraint)) {
                throw new EmailAlreadyExistsException("Email already exists");
            }
            if (Constants.UNIQUE_USERNAME_CONSTRAINT_NAME.equals(constraint)) {
                throw new UsernameAlreadyExistsException();
            }

            throw ex;
        }

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

    private String findConstraintName(Throwable exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof ConstraintViolationException violation) {
                return violation.getConstraintName();
            }

            current = current.getCause();
        }

        return null;
    }
}
