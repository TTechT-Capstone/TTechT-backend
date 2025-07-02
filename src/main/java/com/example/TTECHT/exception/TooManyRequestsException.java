package com.example.TTECHT.exception;

public class TooManyRequestsException extends RuntimeException{
    public  TooManyRequestsException(String message) {
        super(message);
    }
}
