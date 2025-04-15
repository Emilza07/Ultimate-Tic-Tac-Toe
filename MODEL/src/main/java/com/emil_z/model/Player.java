package com.emil_z.model;

import com.emil_z.model.BASE.BaseEntity;
import com.google.firebase.firestore.Exclude;

public class Player extends BaseEntity {
	@Exclude
	private PlayerType type;
	private String name;
	private int elo;

	public Player (){}
	public Player(String idFs, String name, int elo) {
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
	@Exclude
	public PlayerType getPlayerType() {
		return type;
	}

	@Exclude
	public void setPlayerType(PlayerType type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getElo(){
		return elo;
	}
	public void setElo(int elo){
		this.elo = elo;
	}

	public int compareElo(Player player){
		return Integer.compare(this.elo, player.elo);
	}
}