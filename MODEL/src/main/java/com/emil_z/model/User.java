package com.emil_z.model;

import android.graphics.Bitmap;

import com.emil_z.helper.BitMapHelper;
import com.emil_z.model.BASE.BaseEntity;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

/**
 * Represents a user in the application, including username, profile picture,
 * profile picture url and ELO rating.
 * Extends {@link BaseEntity} and implements {@link Serializable}.
 */
public class User extends BaseEntity implements Serializable {
	private String username;
	@Exclude
	private String profilePicture; // Base64-encoded string
	private String profilePictureUrl;
	private float elo;

	/**
	 * Default constructor for User.
	 */
	public User() {
	}

	/**
	 * Constructs a User with the specified username and picture.
	 * Sets the default ELO rating to 1200.
	 * @param username The user's username.
	 * @param profilePicture The user's profile picture as a Base64-encoded string.
	 */
	public User(String username, String profilePicture) {
		this(username, 1200, profilePicture);
	}

	/**
	 * Constructs a User with the specified username, ELO rating, and picture.
	 * @param username The user's username.
	 * @param elo The user's ELO rating.
	 * @param profilePicture The user's profile picture as a Base64-encoded string.
	 */
	public User(String username, float elo, String profilePicture) {
		this.username = username;
		this.elo = elo;
		this.profilePicture = profilePicture;
	}

	/**
	 * Copy constructor for the User class.
	 * Creates a new User instance by copying the properties of the given User object.
	 *
	 * @param user The User object to copy from.
	 */
	public User(User user) {
		this.username = user.username;
		this.elo = user.elo;
		this.profilePicture = user.profilePicture;
		this.profilePictureUrl = user.profilePictureUrl;
		this.setIdFs(user.getIdFs());
	}

	/**
	 * Gets the user's username.
	 * @return The username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Gets the user's profile picture as a Base64-encoded string.
	 * @return The profile picture.
	 */
	@Exclude
	public String getProfilePicture() {
		return profilePicture;
	}

	/**
	 * Gets the URL of the user's profile picture.
	 * @return The profile picture URL.
	 */
	public String getProfilePictureUrl() {
		return profilePictureUrl;
	}

	/**
	 * Sets the user's profile picture as a Base64-encoded string.
	 * @param profilePicture The new profile picture.
	 */
	@Exclude
	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}

	/**
	 * Gets the user's ELO rating.
	 * @return The ELO rating.
	 */
	public float getElo() {
		return elo;
	}

	/**
	 * Decodes and returns the user's profile picture as a Bitmap.
	 * Excluded from Firestore serialization.
	 * @return The profile picture as a Bitmap, or null if decoding fails.
	 */
	@Exclude
	public Bitmap getPictureBitmap() {
		return BitMapHelper.decodeBase64(profilePicture);
	}

}