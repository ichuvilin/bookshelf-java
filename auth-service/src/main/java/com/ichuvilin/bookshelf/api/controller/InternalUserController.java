package com.ichuvilin.bookshelf.api.controller;

import com.ichuvilin.bookshelf.api.model.request.UserBatchRequest;
import com.ichuvilin.bookshelf.domain.UserSummary;
import com.ichuvilin.bookshelf.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/internal/v1/users")
@RequiredArgsConstructor
public class InternalUserController {
    private final UserService userService;

    @PostMapping("/batch")
    public ResponseEntity<List<UserSummary>> getUsersByIds(@Valid @RequestBody UserBatchRequest request) {
        return ResponseEntity.ok(userService.getUsersById(request.ids()));
    }
}
