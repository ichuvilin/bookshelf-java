package com.ichuvilin.bookshelf.internal.dto;

import com.ichuvilin.bookshelf.domain.UserPublic;

import java.util.List;

public record UsersBatchResponse(List<UserPublic> users) {}
