package com.emil_z.model;

import com.emil_z.model.BASE.BaseEntity;

public class Player extends BaseEntity {
	private String name;
	private float elo;

	public Player (){}
	public Player(String idFs, String name, float elo) {
		this.idFs = idFs;
		this.name = name;
		this.elo = elo;
	}
	public Player(Player player){
		this.idFs = player.idFs;
		this.name = player.name;
		this.elo = player.elo;
	}

	public Player(User user) {
		this.idFs = user.getIdFs();
		this.name = user.getName();
		this.elo = user.getElo();
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

	public int compareElo(Player player){
		return Float.compare(this.elo, player.elo);
	}
}