package com.library.exception;
public class DuplicateIsbnException extends RuntimeException {
    public DuplicateIsbnException(String message) { super(message); }
}
