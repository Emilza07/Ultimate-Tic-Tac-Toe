package com.emil_z.model;

/**
 * Represents a local game instance, extending the base Game class.
 * Handles initialization and move logic for a local two-player game.
 */
public class LocalGame extends Game {
	/**
	 * Constructs a new LocalGame with the given local player ID.
	 * Both players are assigned the same ID and default names.
	 *
	 * @param localPlayerIdFs The ID to assign to both players.
	 */
	public LocalGame(String localPlayerIdFs) {
		super();
		setStarted(true);
		player1.setName("Player 1");
		player2.setName("Player 2");
		player1.setIdFs(localPlayerIdFs);
		player2.setIdFs(localPlayerIdFs);
		currentPlayerIdFs = crossPlayerIdFs = player1.getIdFs();
	}

	/**
	 * Makes a move at the specified board location.
	 * Updates the game state and checks for game over conditions.
	 *
	 * @param location The location on the board where the move is made.
	 */
	public void makeMove(BoardLocation location) {
		super.makeMove(location);
		outerBoard.getBoard(location.getOuter()).isFinished();
		if (outerBoard.isGameOver()) {
			isFinished = true;
			winnerIdFs = String.valueOf(outerBoard.getWinner());
		}
	}
}