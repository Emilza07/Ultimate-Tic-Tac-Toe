package com.emil_z.model;

import java.util.Objects;

/**
 * Represents a game where a human player competes against a CPU opponent.
 * Extends the {@link Game} class and manages player initialization and move logic.
 */
public class CpuGame extends Game {
	/**
	 * Constructs a new CpuGame with the specified player IDs.
	 *
	 * @param localPlayerIdFs   the ID for the local (human) player
	 * @param crossPlayerIdFs   the ID for the cross (CPU) player
	 */
	public CpuGame(String localPlayerIdFs, String crossPlayerIdFs) {
		super();
		setStarted(true);
		player1.setName("Player 1");
		player1.setIdFs(localPlayerIdFs);
		player2.setName("CPU");
		player2.setIdFs("CPU");
		this.crossPlayerIdFs = this.currentPlayerIdFs = crossPlayerIdFs;
	}

	/**
	 * Makes a move at the specified board location, updates the current player,
	 * and checks for game completion.
	 *
	 * @param location the {@link BoardLocation} where the move is made
	 */
	public void makeMove(BoardLocation location) {
		super.makeMove(location);
		outerBoard.getBoard(location.getOuter()).isFinished();
		currentPlayerIdFs = Objects.equals(currentPlayerIdFs, player1.getIdFs()) ?
			player2.getIdFs() :
			player1.getIdFs();
		if (outerBoard.isGameOver()) {
			isFinished = true;
			winnerIdFs = String.valueOf(outerBoard.getWinner());
		}
	}
}