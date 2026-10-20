package com.ichuvilin.bookshelf.api.model.request;

import org.hibernate.validator.constraints.Range;

public record UpdateReviewRequest(@Range(min = 1, max = 5) int rating, String title, String content) {}
