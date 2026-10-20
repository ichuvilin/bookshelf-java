package com.ichuvilin.bookshelf.api.model.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

public record ReviewCreationRequest(
        @Range(min = 1, max = 5) int rating,
        String title,
        @NotBlank String content) {}
