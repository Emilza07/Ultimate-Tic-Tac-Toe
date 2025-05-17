package com.emil_z.model;

import android.graphics.Point;

import com.emil_z.model.BASE.BaseEntity;

import java.io.Serializable;

/**
 * Represents a 3x3 inner board for the game .
 * Handles board state, winner detection, and move legality.
 */
public class InnerBoard extends BaseEntity implements Serializable {
	private final char[][] board;
	private char winner;
	private boolean isFinished;

	/**
	 * Constructs a new, empty InnerBoard.
	 */
	public InnerBoard() {
		board = new char[3][3];
		isFinished = false;
		winner = 0;
	}

	/**
	 * Copy constructor. Creates a deep copy of the given InnerBoard.
	 * @param innerBoard The InnerBoard to copy.
	 */
	public InnerBoard(InnerBoard innerBoard) {
		board = new char[3][3];
		isFinished = innerBoard.isFinished;
		winner = innerBoard.winner;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				board[i][j] = innerBoard.getCell(new Point(i, j));
			}
		}
	}

	/**
	 * Gets the winner of the board.
	 * @return The winner character ('X', 'O', 'T' for tie, or 0 if none).
	 */
	public char getWinner() {
		return winner;
	}

	/**
	 * Checks if the board is finished (win or tie).
	 * Updates the winner and finished state if necessary.
	 * @return true if the board is finished, false otherwise.
	 */
	public boolean isFinished() {
		if (isFinished) return true;
		for (int i = 0; i < 3; i++) {
			if (board[i][0] != 0 && board[i][0] == board[i][1] && board[i][0] == board[i][2]) {
				winner = board[i][0];
				isFinished = true;
				return true;
			}
			if (board[0][i] != 0 && board[0][i] == board[1][i] && board[0][i] == board[2][i]) {
				winner = board[0][i];
				isFinished = true;
				return true;
			}
		}
		if (board[0][0] != 0 && board[0][0] == board[1][1] && board[0][0] == board[2][2] ||
			board[0][2] != 0 && board[0][2] == board[1][1] && board[0][2] == board[2][0]) {
			winner = board[1][1];
			isFinished = true;
			return true;
		}
		if (isTie()) {
			isFinished = true;
			winner = 'T';
			return true;
		}
		return false;
	}

	/**
	 * Gets the value of a cell at the given point.
	 * @param inner The point (x, y) to get the cell from.
	 * @return The character in the cell, or 0 if empty.
	 */
	public char getCell(Point inner) {
		return board[inner.x][inner.y];
	}

	/**
	 * Checks if the board is a tie (all cells filled, no winner).
	 * @return true if the board is a tie, false otherwise.
	 */
	public boolean isTie() {
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				if (board[row][col] == 0) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks if a move at the given point is legal (cell empty and board not finished).
	 * @param inner The point (x, y) to check.
	 * @return true if the move is legal, false otherwise.
	 */
	public boolean isLegal(Point inner) {
		return board[inner.x][inner.y] == 0 && !isFinished;
	}

	/**
	 * Makes a move for the current player at the given point.
	 * @param inner The point (x, y) to place the move.
	 * @param currentPlayer The character representing the current player.
	 */
	public void makeMove(Point inner, char currentPlayer) {
		board[inner.x][inner.y] = currentPlayer;
	}
}