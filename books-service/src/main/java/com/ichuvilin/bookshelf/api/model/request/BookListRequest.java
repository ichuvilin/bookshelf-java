package com.ichuvilin.bookshelf.api.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record BookListRequest(
        String search,

        @Pattern(regexp = "title|author|createdAt", message = "Sort must be one of: title, author, createdAt")
        String sort,

        @Pattern(regexp = "(?i)asc|desc", message = "Order must be asc or desc")
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
