package com.ichuvilin.bookshelf.internal.controller;

import com.ichuvilin.bookshelf.internal.dto.GetUsersByIdsRequest;
import com.ichuvilin.bookshelf.internal.dto.UsersBatchResponse;
import com.ichuvilin.bookshelf.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/users")
@RequiredArgsConstructor
public class InternalUserController {
    private final UserService userService;

    @PostMapping("/batch")
    public ResponseEntity<UsersBatchResponse> getUserIds(@RequestBody GetUsersByIdsRequest request) {
        return ResponseEntity.ok(new UsersBatchResponse(userService.getUsersById(request.userIds())));
    }
}
