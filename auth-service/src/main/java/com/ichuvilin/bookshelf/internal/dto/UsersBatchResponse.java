package com.ichuvilin.bookshelf.internal.dto;

import com.ichuvilin.bookshelf.domain.UserSummary;

import java.util.List;

public record UsersBatchResponse(List<UserSummary> users) {}
