package com.emil_z.model;

import com.google.firebase.Timestamp;

import java.util.Objects;

public class OnlineGame extends Game{
	private Timestamp startedAt;
	public OnlineGame(){
		super();
	}

	public OnlineGame(Player player1) {
		super();
		this.player1 = player1;
	}

	public OnlineGame(Game game) {
	}

	public Timestamp getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Timestamp startedAt) {
		this.startedAt = startedAt;
	}

	public void startGameForJoiner() {
		outerBoard = new OuterBoard();
	}

	public void makeTurn(BoardLocation location){
		super.makeTurn(location);
		outerBoard.getBoard(location.getOuter()).isFinished();
		if (Objects.equals(currentPlayerIdFs, player1.getIdFs())) {
			currentPlayerIdFs = player2.getIdFs();
		} else {
			currentPlayerIdFs = player1.getIdFs();
		}
		if (outerBoard.isGameOver()) {
			isFinished = true;
			winnerIdFs = Objects.equals(currentPlayerIdFs, player2.getIdFs()) ? player1.getIdFs() : player2.getIdFs();
		}
	}
}