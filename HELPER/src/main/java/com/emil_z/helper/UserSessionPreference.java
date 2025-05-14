package com.emil_z.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class UserSessionPreference extends LoginPreference {
	private static final String KEY_USER_ID_FS = "userIdFs";
	private static final String KEY_AUTH_TOKEN = "authToken";

	private final SharedPreferences sharedPreferences;

	public UserSessionPreference(Context context) {
		super(context);
		this.sharedPreferences = super.getSharedPreferences();
	}


	public void saveLoginCredentials(String email, String password, String userIdFs) {
		super.saveLoginCredentials(email, password);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(KEY_USER_ID_FS, userIdFs);
		editor.apply();
	}

	public String generateToken(String userIdFs) {
		// Create token using UUID and user ID
		String token = UUID.randomUUID().toString() + "_" + userIdFs;

		saveToken(token);

		return token;
	}

	public void saveToken(String token) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(KEY_AUTH_TOKEN, token);
		editor.apply();
	}

	public String getUserIdFs() {
		return sharedPreferences != null ? sharedPreferences.getString(KEY_USER_ID_FS, null) : null;
	}

	public String getToken() {
		return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
	}

	public void clearToken() {
		sharedPreferences.edit()
				.remove(KEY_AUTH_TOKEN)
				.apply();
	}

	@Override
	public void clearLoginCredentials() {
		super.clearLoginCredentials();
		sharedPreferences.edit()
				.remove(KEY_USER_ID_FS)
				.remove(KEY_AUTH_TOKEN)
				.apply();
	}

	public void saveFullSession(String email, String password, String userIdFs, String token) {
		super.saveLoginCredentials(email, password);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(KEY_USER_ID_FS, userIdFs);
		editor.putString(KEY_AUTH_TOKEN, token);
		editor.apply();
	}

	public boolean hasValidSession() {
		return getEmail() != null && getPassword() != null &&
				getUserIdFs() != null && getToken() != null;
	}
}