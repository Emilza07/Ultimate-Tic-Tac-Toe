package com.emil_z.model;

import com.emil_z.model.BASE.BaseEntity;

import java.io.Serializable;

public class Player extends BaseEntity implements Serializable {
	private String idFs;
	private String name;
	private String email;
	private String password;
	private int elo;

	public Player() {
	}
	public Player(String idFs, String name, String email, String password) {
		this.idFs = idFs;
		this.name = name;
		this.email = email;
		this.password = password;
		this.elo = elo;
	}
	public String getIdFs() {
		return idFs;
	}
	public void setIdFs(String idFs) {
		this.idFs = idFs;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
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