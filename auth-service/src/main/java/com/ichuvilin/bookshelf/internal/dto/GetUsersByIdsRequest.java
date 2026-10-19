package com.ichuvilin.bookshelf.internal.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record GetUsersByIdsRequest(@NotEmpty List<@NotNull @Positive Long> ids) {}
