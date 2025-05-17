package com.emil_z.model;

import android.graphics.Point;

/**
 * Represents the outer board in an ultimate tic-tac-toe game.
 * Manages the state of the game, including the current player, winner,
 * free move status, and the last move made.
 */
public class OuterBoard {
	private final InnerBoard[][] board;
	private char currentPlayer;
	private char winner;
	private boolean freeMove;
	private Point lastMove;

	/**
	 * Constructs a new OuterBoard, initializing all inner boards and setting the starting player.
	 */
	public OuterBoard() {
		board = new InnerBoard[3][3];
		winner = 0;
		currentPlayer = 'X';
		freeMove = true;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				board[i][j] = new InnerBoard();
			}
		}
	}

	/**
	 * Returns the inner board at the specified outer board location.
	 * @param outer The coordinates of the inner board.
	 * @return The InnerBoard at the given location.
	 */
	public InnerBoard getBoard(Point outer) {
		return board[outer.x][outer.y];
	}

	/**
	 * Gets the current player ('X' or 'O').
	 * @return The current player.
	 */
	public char getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 * Gets the winner of the game.
	 * @return The winner ('X', 'O', 'T' for tie, or 0 if no winner yet).
	 */
	public char getWinner() {
		return winner;
	}

	/**
	 * Checks if the next move can be made on any board.
	 * @return True if a free move is allowed, false otherwise.
	 */
	public boolean isFreeMove() {
		return freeMove;
	}

	/**
	 * Gets the last move made.
	 * @return The last move as a Point.
	 */
	public Point getLastMove() {
		return lastMove;
	}

	/**
	 * Checks if the game is a tie (all inner boards are finished).
	 * @return True if the game is a tie, false otherwise.
	 */
	public boolean isTie() {
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				if (!board[row][col].isFinished()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks if the game is over (win or tie).
	 * Updates the winner if the game is over.
	 * @return True if the game is over, false otherwise.
	 */
	public boolean isGameOver() {
		for (int i = 0; i < 3; i++) {
			if (board[i][0].getWinner() != 0 && board[i][0].getWinner() == board[i][1].getWinner() && board[i][0].getWinner() == board[i][2].getWinner()) {
				winner = board[i][0].getWinner();
				return true;
			}
			if (board[0][i].getWinner() != 0 && board[0][i].getWinner() == board[1][i].getWinner() && board[0][i].getWinner() == board[2][i].getWinner()) {
				winner = board[0][i].getWinner();
				return true;
			}
		}
		if (board[0][0].getWinner() != 0 && board[0][0].getWinner() == board[1][1].getWinner() && board[0][0].getWinner() == board[2][2].getWinner() ||
			board[0][2].getWinner() != 0 && board[0][2].getWinner() == board[1][1].getWinner() && board[0][2].getWinner() == board[2][0].getWinner()) {
			winner = board[1][1].getWinner();
			return true;
		}
		if (isTie()) {
			winner = 'T';
			return true;
		}
		return false;
	}

	/**
	 * Checks if a move at the given location is legal.
	 * @param location The board location to check.
	 * @return True if the move is legal, false otherwise.
	 */
	public boolean isLegal(BoardLocation location) {
		InnerBoard innerBoard = board[location.getOuter().x][location.getOuter().y];
		return freeMove
			? innerBoard.isLegal(location.getInner())
			: location.getOuter().equals(lastMove) && innerBoard.isLegal(location.getInner());
	}

	/**
	 * Makes a move at the specified location, updates the game state, and switches the current player.
	 * @param location The location to make the move.
	 */
	public void makeMove(BoardLocation location) {
		board[location.getOuter().x][location.getOuter().y].makeMove(location.getInner(), currentPlayer);
		lastMove = location.getInner();
		freeMove = board[location.getInner().x][location.getInner().y].isFinished();
		currentPlayer = currentPlayer == 'X' ? 'O' : 'X';
	}
}