package com.emil_z.model;

public class OnlineGame extends Game{
	public OnlineGame(){
		super();
	}
	public OnlineGame(User player1) {
		super();
		this.player1 = new Player(player1.getIdFs(), player1.getUsername(), player1.getElo());
		this.player1.setPlayerType(PlayerType.LOCAL);
	}
	public OnlineGame(Game game) {

	}
	public void startGameForJoiner() {
		gameModel = new GameModel();
		player1.setPlayerType(PlayerType.REMOTE);
		player2.setPlayerType(PlayerType.LOCAL);
	}

}