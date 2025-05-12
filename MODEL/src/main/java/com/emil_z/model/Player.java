package com.emil_z.model;

import android.graphics.Bitmap;

import com.emil_z.helper.BitMapHelper;
import com.emil_z.model.BASE.BaseEntity;
import com.google.firebase.firestore.Exclude;

public class Player extends BaseEntity {
	private String name;
	private float elo;
	@Exclude
	private String picture;

	public Player (){}

	public Player(Player player){
		this.idFs = player.idFs;
		this.name = player.name;
		this.elo = player.elo;
		this.picture = player.picture;
	}

	public Player(User user) {
		this.idFs = user.getIdFs();
		this.name = user.getUsername();
		this.elo = user.getElo();
		this.picture = user.getPicture();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getElo(){
		return elo;
	}

	public void setElo(float elo){
		this.elo = elo;
	}

	@Exclude
	public String getPicture() {
		return picture;
	}

	@Exclude
	public Bitmap getPictureBitmap() {
		return BitMapHelper.decodeBase64(picture);
	}

	@Exclude
	public void setPicture(String picture) {
		this.picture = picture;
	}

	public int compareElo(Player player){
		return Float.compare(this.elo, player.elo);
	}
}