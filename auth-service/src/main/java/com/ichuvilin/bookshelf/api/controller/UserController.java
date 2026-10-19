package com.ichuvilin.bookshelf.api.controller;

import com.ichuvilin.bookshelf.api.model.request.UpdateUserRequest;
import com.ichuvilin.bookshelf.domain.UserPublic;
import com.ichuvilin.bookshelf.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserPublic> getMe(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping
    public ResponseEntity<UserPublic> update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateUserRequest request) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.ok(userService.update(userId, request));
    }
}
