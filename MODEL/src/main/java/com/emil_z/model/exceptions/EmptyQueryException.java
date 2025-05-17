package com.emil_z.model.exceptions;

import com.google.firebase.firestore.FirebaseFirestoreException;

/**
 * Custom exception class that extends FirebaseFirestoreException.
 * This exception is used to indicate that no games are available in the queue.
 */
public class EmptyQueryException extends FirebaseFirestoreException {
	/**
	 * Default constructor that initializes the exception with a default message
	 * and a NOT_FOUND error code.
	 */
	public EmptyQueryException() {
		this("No games in queue", Code.NOT_FOUND);
	}

	/**
	 * Constructor that initializes the exception with a default message,
	 * a NOT_FOUND error code, and a specified cause.
	 *
	 * @param cause The throwable cause of the exception.
	 */
	public EmptyQueryException(Throwable cause) {
		this("No games in queue", Code.NOT_FOUND, cause);
	}

	/**
	 * Constructor that initializes the exception with a custom message and error code.
	 *
	 * @param message The custom error message.
	 * @param code    The error code associated with the exception.
	 */
	public EmptyQueryException(String message, Code code) {
		super(message, code);
	}

	/**
	 * Constructor that initializes the exception with a custom message, error code,
	 * and a specified cause.
	 *
	 * @param message The custom error message.
	 * @param code    The error code associated with the exception.
	 * @param cause   The throwable cause of the exception.
	 */
	public EmptyQueryException(String message, Code code, Throwable cause) {
		super(message, code, cause);
	}

}