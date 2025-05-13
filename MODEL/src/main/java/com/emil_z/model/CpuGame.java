package com.emil_z.model;

import java.util.Objects;

public class CpuGame extends Game {
	public CpuGame(String localPlayerIdFs, String crossPlayerIdFs) {
		super();
		setStarted(true);
		player1.setName("Player 1");
		player1.setIdFs(localPlayerIdFs);
		player2.setName("CPU");
		player2.setIdFs("CPU");
		this.crossPlayerIdFs = crossPlayerIdFs;
		this.currentPlayerIdFs = crossPlayerIdFs;
	}

	public void makeMove(BoardLocation location) {
		super.makeMove(location);
		outerBoard.getBoard(location.getOuter()).isFinished();
		if (Objects.equals(currentPlayerIdFs, player1.getIdFs())) {
			currentPlayerIdFs = player2.getIdFs();
		} else {
			currentPlayerIdFs = player1.getIdFs();
		}
		if (outerBoard.isGameOver()) {
			isFinished = true;
			winnerIdFs = String.valueOf(outerBoard.getWinner());
		}
	}
}