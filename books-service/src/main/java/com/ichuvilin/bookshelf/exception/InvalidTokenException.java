package com.ichuvilin.bookshelf.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Invalid access token");
    }
}
