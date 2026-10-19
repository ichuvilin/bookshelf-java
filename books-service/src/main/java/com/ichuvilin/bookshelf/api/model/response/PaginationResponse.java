package com.ichuvilin.bookshelf.api.model.response;

public record PaginationResponse(int page, int limit, long total, int totalPages) {}
