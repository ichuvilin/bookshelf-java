package com.ichuvilin.bookshelf.api.model.request;

import java.time.LocalDate;

public record BookUpdateRequest(String title, String description, String author, String isbn, LocalDate publishedYear) {}
