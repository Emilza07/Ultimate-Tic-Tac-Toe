package com.emil_z.model;

public class LocalGame extends Game{
	public LocalGame(String localPlayerIdFs) {
		super();
		setStarted(true);
		player1.setPlayerType(PlayerType.LOCAL);
		player1.setName("Player 1");
		player1.setIdFs(localPlayerIdFs);
		player2.setPlayerType(PlayerType.LOCAL);
		player2.setName("Player 2");
		player2.setIdFs(localPlayerIdFs);
		currentPlayerIdFs = player1.getIdFs();
	}

	public void makeTurn(BoardLocation location) {
		super.makeTurn(location);
		if (outerBoard.isGameOver()) {
			isFinished = true;
			winner = String.valueOf(outerBoard.getWinner());
		}
	}
}