package com.emil_z.model;

import android.graphics.Bitmap;

import com.emil_z.helper.BitMapHelper;
import com.emil_z.model.BASE.BaseEntity;

import java.io.Serializable;

public class User extends BaseEntity implements Serializable {
	private String username;
	private String password;
	private float elo;
	private String picture;
	public User() {
	}
	public User(String username, String password, String picture) {
		this(username, password, 1200, picture);
	}
	public User(String username, String password, float elo, String picture) {
		this.username = username;
		this.password = password;
		this.elo = elo;
		this.picture = picture;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public float getElo() {
		return elo;
	}

	public void setElo(float elo) {
		this.elo = elo;
	}

	public String getPicture() {
		return picture;
	}

	public Bitmap getPictureBitmap() {
		return BitMapHelper.decodeBase64(picture);
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

}