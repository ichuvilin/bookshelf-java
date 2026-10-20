package com.ichuvilin.bookshelf.exception;

public class NotReviewOwnerException extends RuntimeException {
    public NotReviewOwnerException() {
        super("Not review owner");
    }
}
