package com.ichuvilin.bookshelf.api.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record BookListRequest(
        String search,
        String sort,
        String order,

        @Min(value = 1, message = "Page must be greater than 0")
        Integer page,

        @Min(value = 1, message = "Limit must be greater than 0")
        @Max(value = 100, message = "Limit must not exceed 100")
        Integer limit) {
    public BookListRequest {
        page = page == null ? 1 : page;
        limit = limit == null ? 10 : limit;
    }
}
