package com.ichuvilin.bookshelf.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException() {
        super("Review not found");
    }
}
