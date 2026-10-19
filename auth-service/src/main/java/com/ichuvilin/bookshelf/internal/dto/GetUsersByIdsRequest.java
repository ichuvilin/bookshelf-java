package com.ichuvilin.bookshelf.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record GetUsersByIdsRequest(@NotEmpty List<@NotBlank @Positive Long> ids) {}
