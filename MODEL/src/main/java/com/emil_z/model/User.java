package com.emil_z.model;

import android.graphics.Bitmap;

import com.emil_z.helper.BitMapHelper;
import com.emil_z.model.BASE.BaseEntity;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

/**
 * Represents a user in the application, including username, hashed password,
 * profile picture, and ELO rating.
 * Extends {@link BaseEntity} and implements {@link Serializable}.
 */
public class User extends BaseEntity implements Serializable {
	private String username;
	private String hashedPassword;
	private String picture;
	private float elo;

	/**
	 * Default constructor for User.
	 */
	public User() {
	}

	/**
	 * Constructs a User with the specified username, password, and picture.
	 * Sets the default ELO rating to 1200.
	 * @param username The user's username.
	 * @param password The user's hashed password.
	 * @param picture The user's profile picture as a Base64-encoded string.
	 */
	public User(String username, String password, String picture) {
		this(username, password, 1200, picture);
	}

	/**
	 * Constructs a User with the specified username, password, ELO rating, and picture.
	 * @param username The user's username.
	 * @param password The user's hashed password.
	 * @param elo The user's ELO rating.
	 * @param picture The user's profile picture as a Base64-encoded string.
	 */
	public User(String username, String password, float elo, String picture) {
		this.username = username;
		this.hashedPassword = password;
		this.elo = elo;
		this.picture = picture;
	}

	/**
	 * Gets the user's username.
	 * @return The username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the user's username.
	 * @param username The new username.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the user's hashed password.
	 * @return The hashed password.
	 */
	public String getHashedPassword() {
		return hashedPassword;
	}

	/**
	 * Gets the user's profile picture as a Base64-encoded string.
	 * @return The profile picture.
	 */
	public String getPicture() {
		return picture;
	}

	/**
	 * Sets the user's profile picture as a Base64-encoded string.
	 * @param picture The new profile picture.
	 */
	public void setPicture(String picture) {
		this.picture = picture;
	}

	/**
	 * Gets the user's ELO rating.
	 * @return The ELO rating.
	 */
	public float getElo() {
		return elo;
	}

	/**
	 * Sets the user's ELO rating.
	 * @param elo The new ELO rating.
	 */
	public void setElo(float elo) {
		this.elo = elo;
	}

	/**
	 * Decodes and returns the user's profile picture as a Bitmap.
	 * Excluded from Firestore serialization.
	 * @return The profile picture as a Bitmap, or null if decoding fails.
	 */
	@Exclude
	public Bitmap getPictureBitmap() {
		return BitMapHelper.decodeBase64(picture);
	}

}