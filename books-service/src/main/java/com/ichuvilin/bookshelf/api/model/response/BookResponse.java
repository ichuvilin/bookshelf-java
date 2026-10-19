package com.ichuvilin.bookshelf.api.model.response;

import java.time.Instant;
import java.time.LocalDate;

public record BookResponse(
        Long id,
        String title,
        String description,
        String isbn,
        String author,
        LocalDate publishedYear,
        Instant createdAt,
        Instant updatedAt) {}
