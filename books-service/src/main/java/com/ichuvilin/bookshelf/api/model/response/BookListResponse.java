package com.ichuvilin.bookshelf.api.model.response;

import java.util.List;

public record BookListResponse(List<BookResponse> data, PaginationResponse pagination) {}
