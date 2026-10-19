package com.ichuvilin.bookshelf.api.model.response;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long bookId,
        Long userId,
        Integer rating,
        String title,
        String content,
        Instant createdAt,
        Instant updatedAt) {}
