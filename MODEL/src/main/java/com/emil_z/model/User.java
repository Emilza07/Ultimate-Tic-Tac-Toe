package com.emil_z.model;

import com.emil_z.model.BASE.BaseEntity;

import java.io.Serializable;

public class User extends BaseEntity implements Serializable {
	private String username;
	private String password;
	private int elo;

	public User() {
	}
	public User(String username, String password) {
		this(username, password, 1200);
	}
	public User(String username, String password, int elo) {
		this.username = username;
		this.password = password;
		this.elo = elo;
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
	public int getElo() {
		return elo;
	}
	public void setElo(int elo) {
		this.elo = elo;
	}
}