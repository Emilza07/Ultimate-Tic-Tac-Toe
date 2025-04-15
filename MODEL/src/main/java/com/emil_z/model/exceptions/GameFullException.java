package com.emil_z.model.exceptions;

public class GameFullException extends Exception {
    public GameFullException() {
        super("Game is full");
    }

    public GameFullException(String message) {
        super(message);
    }

    public GameFullException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameFullException(Throwable cause) {
        super(cause);
    }
}