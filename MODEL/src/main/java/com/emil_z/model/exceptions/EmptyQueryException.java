package com.emil_z.model.exceptions;

import com.google.firebase.firestore.FirebaseFirestoreException;

public class EmptyQueryException extends FirebaseFirestoreException {
	public EmptyQueryException() {
		this("No games in queue", Code.NOT_FOUND);
	}

	public EmptyQueryException(Throwable cause) {
		this("No games in queue", Code.NOT_FOUND, cause);
	}

	public EmptyQueryException(String message, Code code) {
		super(message, code);
	}

	public EmptyQueryException(String message, Code code, Throwable cause) {
		super(message, code, cause);
	}

}