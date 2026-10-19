package com.ichuvilin.bookshelf.internal.dto;

import java.util.List;

public record GetUsersByIdsRequest(List<Long> userIds) {}
