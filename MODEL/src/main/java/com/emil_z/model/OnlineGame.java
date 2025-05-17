package com.emil_z.model;

import com.google.firebase.Timestamp;

import java.util.Objects;

/**
 * Represents an online game session, extending the base Game class.
 * Handles player management, game start, and move logic for online play.
 */
public class OnlineGame extends Game {
	private Timestamp startedAt;

	/**
	 * Default constructor for OnlineGame.
	 * Initializes the base Game.
	 */
	public OnlineGame() {
		super();
	}

	/**
	 * Constructs an OnlineGame with the specified player as player1.
	 * @param player1 The first player in the game.
	 */
	public OnlineGame(Player player1) {
		super();
		this.player1 = player1;
	}

	/**
	 * Constructs an OnlineGame from an existing Game instance.
	 * @param game The Game instance to copy or reference.
	 */
	public OnlineGame(Game game) {
	}

	/**
	 * Gets the timestamp when the game started.
	 * @return The start timestamp.
	 */
	public Timestamp getStartedAt() {
		return startedAt;
	}

	/**
	 * Sets the timestamp for when the game started.
	 * @param startedAt The start timestamp to set.
	 */
	public void setStartedAt(Timestamp startedAt) {
		this.startedAt = startedAt;
	}

	/**
	 * Initializes the outer board for the joiner when the game starts.
	 */
	public void startGameForJoiner() {
		outerBoard = new OuterBoard();
	}

	/**
	 * Makes a move at the specified board location.
	 * Switches the current player and checks for game over conditions.
	 * @param location The location on the board where the move is made.
	 */
	public void makeMove(BoardLocation location) {
		super.makeMove(location);
		outerBoard.getBoard(location.getOuter()).isFinished();
		currentPlayerIdFs = Objects.equals(currentPlayerIdFs, player1.getIdFs()) ?
			player2.getIdFs() :
			player1.getIdFs();
		if (outerBoard.isGameOver()) {
			isFinished = true;
			winnerIdFs = outerBoard.getWinner() == 'T' ? "T" :
				Objects.equals(currentPlayerIdFs, player2.getIdFs()) ? player1.getIdFs() : player2.getIdFs();
		}
	}
}