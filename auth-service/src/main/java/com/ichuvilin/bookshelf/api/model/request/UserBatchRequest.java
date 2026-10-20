package com.ichuvilin.bookshelf.api.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserBatchRequest(
        @NotEmpty
        @Size(max = 500)
        List<@NotNull @Positive Long> ids) {}
