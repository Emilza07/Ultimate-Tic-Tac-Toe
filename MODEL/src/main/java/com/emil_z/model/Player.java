package com.emil_z.model;

import android.graphics.Bitmap;

import com.emil_z.helper.BitMapHelper;
import com.emil_z.model.BASE.BaseEntity;
import com.google.firebase.firestore.Exclude;

/**
 * Represents a player in the application, including their name, ELO rating, and profile picture.
 * Extends {@link BaseEntity} for common entity properties.
 */
public class Player extends BaseEntity {
	private String name;
	private float elo;
	@Exclude
	private String picture;

	/**
	 * Default constructor for Player.
	 */
	public Player() {
	}

	/**
	 * Constructs a Player from a User object.
	 * @param user The User to convert to a Player.
	 */
	public Player(User user) {
		this.idFs = user.getIdFs();
		this.name = user.getUsername();
		this.elo = user.getElo();
		this.picture = user.getPicture();
	}

	/**
	 * Copy constructor. Creates a new Player by copying another Player's properties.
	 * @param player The Player to copy.
	 */
	public Player(Player player) {
		this.idFs = player.idFs;
		this.name = player.name;
		this.elo = player.elo;
		this.picture = player.picture;
	}

	/**
	 * Gets the player's name.
	 * @return The player's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the player's name.
	 * @param name The new name for the player.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the player's ELO rating.
	 * @return The player's ELO rating.
	 */
	public float getElo() {
		return elo;
	}

	/**
	 * Sets the player's profile picture as a Base64-encoded string.
	 * Excluded from Firestore serialization.
	 * @param picture The Base64-encoded profile picture.
	 */
	@Exclude
	public void setPicture(String picture) {
		this.picture = picture;
	}

	/**
	 * Decodes and returns the player's profile picture as a Bitmap.
	 * Excluded from Firestore serialization.
	 * @return The profile picture as a Bitmap, or null if decoding fails.
	 */
	@Exclude
	public Bitmap getPictureBitmap() {
		return BitMapHelper.decodeBase64(picture);
	}

	/**
	 * Compares this player's ELO rating to another player's ELO rating.
	 * @param player The other player to compare to.
	 * @return A negative integer, zero, or a positive integer as this player's ELO
	 *         is less than, equal to, or greater than the specified player's ELO.
	 */
	public int compareElo(Player player) {
		return Float.compare(this.elo, player.elo);
	}
}