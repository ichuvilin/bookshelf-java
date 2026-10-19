package com.ichuvilin.bookshelf.internal.dto;

import java.time.Instant;

public record VerifyTokenResponse(boolean valid, Long userId, Instant expiresAt, String error) {}
