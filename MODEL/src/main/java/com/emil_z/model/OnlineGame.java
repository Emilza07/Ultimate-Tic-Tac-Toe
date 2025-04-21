package com.emil_z.model;

import java.util.Objects;

public class OnlineGame extends Game{
	public OnlineGame(){
		super();
	}

	public OnlineGame(Player player1) {
		super();
		this.player1 = player1;
	}

	public OnlineGame(Game game) {
	}

	public void startGameForJoiner() {
		outerBoard = new OuterBoard();
	}

	public void makeTurn(BoardLocation location){
		super.makeTurn(location);
		if (Objects.equals(currentPlayerIdFs, player1.getIdFs())) {
			currentPlayerIdFs = player2.getIdFs();
		} else {
			currentPlayerIdFs = player1.getIdFs();
		}
		if (outerBoard.isGameOver()) {
			isFinished = true;
			winner = Objects.equals(currentPlayerIdFs, player2.getIdFs()) ? player1.getIdFs() : player2.getIdFs();
		}
	}
}