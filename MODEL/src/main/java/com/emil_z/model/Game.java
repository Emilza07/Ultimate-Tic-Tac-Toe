package com.emil_z.model;

import com.emil_z.model.BASE.BaseEntity;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game session, managing players, moves, and board state.
 * Extends {@link BaseEntity} and implements {@link Serializable}.
 */
public class Game extends BaseEntity implements Serializable {
	protected Player player1;
	protected Player player2;
	protected String crossPlayerIdFs;
	protected String currentPlayerIdFs;
	protected String winnerIdFs;
	protected boolean isStarted;
	protected boolean isFinished;
	protected List<BoardLocation> moves;
	@Exclude
	protected OuterBoard outerBoard;

	/**
	 * Default constructor. Initializes players, board, and state.
	 */
	public Game() {
		this.player1 = new Player();
		this.player2 = new Player();
		this.winnerIdFs = null;
		this.isStarted = false;
		this.isFinished = false;
		this.moves = new ArrayList<>();
		this.outerBoard = new OuterBoard();
	}

	/**
	 * Copy constructor. Creates a new Game instance by copying another.
	 * @param game the Game instance to copy
	 */
	public Game(Game game) {
		this.player1 = new Player(game.player1);
		this.player2 = new Player(game.player2);
		this.crossPlayerIdFs = game.crossPlayerIdFs;
		this.currentPlayerIdFs = game.currentPlayerIdFs;
		this.winnerIdFs = game.winnerIdFs;
		this.isStarted = game.isStarted;
		this.isFinished = game.isFinished;
		this.moves = new ArrayList<>(game.moves);
		this.outerBoard = new OuterBoard();
	}


	/**
	 * Gets the first player.
	 * @return player1
	 */
	public Player getPlayer1() {
		return player1;
	}

	/**
	 * Sets the first player.
	 * @param player1 the player to set
	 */
	public void setPlayer1(Player player1) {
		this.player1 = player1;
	}

	/**
	 * Gets the second player.
	 * @return player2
	 */
	public Player getPlayer2() {
		return player2;
	}

	/**
	 * Gets the Firestore ID of the cross player.
	 * @return crossPlayerIdFs
	 */
	public String getCrossPlayerIdFs() {
		return crossPlayerIdFs;
	}

	/**
	 * Gets the Firestore ID of the current player.
	 * @return currentPlayerIdFs
	 */
	public String getCurrentPlayerIdFs() {
		return currentPlayerIdFs;
	}

	/**
	 * Gets the Firestore ID of the winner.
	 * @return winnerIdFs
	 */
	public String getWinnerIdFs() {
		return winnerIdFs;
	}

	/**
	 * Sets the Firestore ID of the winner.
	 * @param winnerIdFs the ID to set
	 */
	public void setWinnerIdFs(String winnerIdFs) {
		this.winnerIdFs = winnerIdFs;
	}

	/**
	 * Checks if the game has started.
	 * @return true if started, false otherwise
	 */
	public boolean isStarted() {
		return isStarted;
	}

	/**
	 * Sets the started state of the game.
	 * @param started true if started, false otherwise
	 */
	public void setStarted(boolean started) {
		isStarted = started;
	}

	/**
	 * Checks if the game has finished.
	 * @return true if finished, false otherwise
	 */
	public boolean isFinished() {
		return isFinished;
	}

	/**
	 * Sets the finished state of the game.
	 * @param finished true if finished, false otherwise
	 */
	public void setFinished(boolean finished) {
		isFinished = finished;
	}

	/**
	 * Gets the list of moves made in the game.
	 * @return list of moves
	 */
	public List<BoardLocation> getMoves() {
		return moves;
	}

	/**
	 * Gets the outer board representing the game state.
	 * Excluded from Firestore serialization.
	 * @return outerBoard
	 */
	@Exclude
	public OuterBoard getOuterBoard() {
		return outerBoard;
	}

	/**
	 * Checks if a move at the given location is legal.
	 * @param location the board location to check
	 * @return true if the move is legal, false otherwise
	 */
	public boolean isLegal(BoardLocation location) {
		return outerBoard.isLegal(location);
	}

	/**
	 * Makes a move at the specified location and records it.
	 * @param location the board location where the move is made
	 */
	public void makeMove(BoardLocation location) {
		outerBoard.makeMove(location);
		moves.add(location);
	}
}