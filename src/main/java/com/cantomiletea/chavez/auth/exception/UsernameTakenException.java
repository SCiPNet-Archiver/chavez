package com.cantomiletea.chavez.auth.exception;

public class UsernameTakenException extends RuntimeException {
    public UsernameTakenException(String message) {
        super(message);
    }
}
