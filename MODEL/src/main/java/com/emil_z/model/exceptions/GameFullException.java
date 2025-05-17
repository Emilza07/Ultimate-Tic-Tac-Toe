package com.emil_z.model.exceptions;

/**
 * Exception thrown to indicate that a game is already full and cannot accept more players.
 */
public class GameFullException extends Exception {
	/**
	 * Constructs a new GameFullException with a default message.
	 */
	public GameFullException() {
		super("Game is full");
	}

	/**
	 * Constructs a new GameFullException with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public GameFullException(String message) {
		super(message);
	}

	/**
	 * Constructs a new GameFullException with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause of the exception.
	 */
	public GameFullException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new GameFullException with the specified cause.
	 *
	 * @param cause the cause of the exception.
	 */
	public GameFullException(Throwable cause) {
		super(cause);
	}
}