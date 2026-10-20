package com.ichuvilin.bookshelf.integration.auth.dto;

import java.util.List;

public record UserBatchRequest(List<Long> ids) {}
