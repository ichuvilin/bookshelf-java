package com.ichuvilin.bookshelf.exception;

public class NotBookOwnerException extends RuntimeException {
    public NotBookOwnerException() {
        super("Not book owner");
    }
}
