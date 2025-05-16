package com.emil_z.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * UserSessionPreference extends LoginPreference to manage user session data,
 * including user IdFs and authentication token, using SharedPreferences.
 */
public class UserSessionPreference extends LoginPreference {
	private static final String KEY_USER_ID_FS = "userIdFs";
	private static final String KEY_AUTH_TOKEN = "authToken";
	private final SharedPreferences sharedPreferences;

	/**
	 * Constructs a new UserSessionPreference.
	 *
	 * @param context the application context
	 */
	public UserSessionPreference(Context context) {
		super(context);
		this.sharedPreferences = super.getSharedPreferences();
	}

	/**
	 * Saves login credentials and the user's Firestore user IdFs.
	 *
	 * @param email the user's email (username)
	 * @param password the user's password
	 * @param userIdFs the user's Firestore user IdFs
	 */
	public void saveLoginCredentials(String email, String password, String userIdFs) {
		super.saveLoginCredentials(email, password);
		sharedPreferences.edit()
			.putString(KEY_USER_ID_FS, userIdFs)
			.apply();
	}

	/**
	 * Generates a new authentication token using a random UUID and the user's Firestore user IdFs,
	 * saves it, and returns the token.
	 *
	 * @param userIdFs the user's Firestore user IdFs
	 * @return the generated authentication token
	 */
	public String generateToken(String userIdFs) {
		String token = UUID.randomUUID().toString() + "_" + userIdFs;
		saveToken(token);
		return token;
	}

	/**
	 * Saves the authentication token in SharedPreferences.
	 *
	 * @param token the authentication token to save
	 */
	public void saveToken(String token) {
		sharedPreferences.edit()
			.putString(KEY_AUTH_TOKEN, token)
			.apply();
	}

	/**
	 * Retrieves the user's Firestore user IdFs from SharedPreferences.
	 *
	 * @return the user's Firestore user IdFs, or null if not set
	 */
	public String getUserIdFs() {
		return sharedPreferences.getString(KEY_USER_ID_FS, null);
	}

	/**
	 * Retrieves the authentication token from SharedPreferences.
	 *
	 * @return the authentication token, or null if not set
	 */
	public String getToken() {
		return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
	}

	/**
	 * Removes the authentication token from SharedPreferences.
	 */
	public void clearToken() {
		sharedPreferences.edit()
			.remove(KEY_AUTH_TOKEN)
			.apply();
	}

	/**
	 * Clears login credentials, user ID, and authentication token from SharedPreferences.
	 */
	@Override
	public void clearLoginCredentials() {
		super.clearLoginCredentials();
		sharedPreferences.edit()
			.remove(KEY_USER_ID_FS)
			.remove(KEY_AUTH_TOKEN)
			.apply();
	}

	/**
	 * Saves the full session, including email, password, user ID, and authentication token.
	 *
	 * @param email the user's email
	 * @param password the user's password
	 * @param userIdFs the user's Firestore user ID
	 * @param token the authentication token
	 */
	public void saveFullSession(String email, String password, String userIdFs, String token) {
		super.saveLoginCredentials(email, password);
		sharedPreferences.edit()
			.putString(KEY_USER_ID_FS, userIdFs)
			.putString(KEY_AUTH_TOKEN, token)
			.apply();
	}

	/**
	 * Checks if the session is valid by verifying that email, password, user ID, and token are all set.
	 *
	 * @return true if all session data is present, false otherwise
	 */
	public boolean hasValidSession() {
		return getEmail() != null && getPassword() != null &&
			getUserIdFs() != null && getToken() != null;
	}
}