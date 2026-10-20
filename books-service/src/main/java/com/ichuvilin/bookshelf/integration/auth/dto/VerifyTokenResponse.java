package com.ichuvilin.bookshelf.integration.auth.dto;

import java.time.Instant;

public record VerifyTokenResponse(boolean valid, Long userId, Instant expiresAt, String error) {}
