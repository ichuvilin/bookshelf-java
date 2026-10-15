package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.api.model.request.RegisterRequest;
import com.ichuvilin.bookshelf.api.model.response.AuthResponse;
import com.ichuvilin.bookshelf.db.entity.User;
import com.ichuvilin.bookshelf.domain.UserPublic;
import com.ichuvilin.bookshelf.exception.EmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthResponse register(RegisterRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userService.existsByUsername(request.getUsername())) {
            throw new EmailAlreadyExistsException("Username already exists");
        }

        String passwdHash = passwordEncoder.encode(request.getPassword());
        User user = userService.create(request.getUsername(), request.getEmail(), passwdHash);

        UserPublic userPublic = UserPublic.toPublic(user);

        return AuthResponse.builder()
                .user(userPublic)
                .build();
    }
}
