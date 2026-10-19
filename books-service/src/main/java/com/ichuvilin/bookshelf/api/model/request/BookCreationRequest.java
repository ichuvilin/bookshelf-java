package com.ichuvilin.bookshelf.api.model.request;

import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;

public record BookCreationRequest(
        @NotEmpty String title, @NotEmpty String author, String description, String isbn, LocalDate publishedYear) {}
