package com.emil_z.model;

public class LocalGame extends Game{
	public LocalGame() {
		super();
		setStarted(true);
		player1.setPlayerType(PlayerType.LOCAL);
		player1.setName("Player 1");
		player2.setPlayerType(PlayerType.LOCAL);
		player2.setName("Player 2");
	}
}