package com.emil_z.model;

import android.graphics.Point;

public class OuterBoard {
	private final InnerBoard[][] board;
	private char winner;
	private char currentPlayer;
	private boolean freeMove;
	private Point lastMove;

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

	public InnerBoard getBoard(Point outer) {
		return board[outer.x][outer.y];
	}

	public boolean isFreeMove() {
		return freeMove;
	}

	public char getWinner() {
		return winner;
	}

	public char getCurrentPlayer() {
		return currentPlayer;
	}

	public Point getLastMove() {
		return lastMove;
	}

	public boolean isTie() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (!board[i][j].isFinished()) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isGameOver() {
		//check if the game is a tie
		//check rows
		for (int i = 0; i < 3; i++) {
			if (board[i][0].getWinner() != 0 && board[i][0].getWinner() == board[i][1].getWinner() && board[i][0].getWinner() == board[i][2].getWinner()) {
				winner = board[i][0].getWinner();
				return true;
			}
			//check columns
			if (board[0][i].getWinner() != 0 && board[0][i].getWinner() == board[1][i].getWinner() && board[0][i].getWinner() == board[2][i].getWinner()) {
				winner = board[0][i].getWinner();
				return true;
			}
		}
		//check diagonals
		if (board[0][0].getWinner() != 0 && board[0][0].getWinner() == board[1][1].getWinner() && board[0][0].getWinner() == board[2][2].getWinner()) {
			winner = board[0][0].getWinner();
			return true;
		}
		if (board[0][2].getWinner() != 0 && board[0][2].getWinner() == board[1][1].getWinner() && board[0][2].getWinner() == board[2][0].getWinner()) {
			winner = board[0][2].getWinner();
			return true;
		}
		if (isTie()) {
			winner = 'T';
			return true;
		}
		return false;
	}

	public boolean isLegal(BoardLocation location) {
		if (freeMove) {
			return board[location.getOuter().x][location.getOuter().y].isLegal(location.getInner());
		}
		return location.getOuter().equals(lastMove) && board[location.getOuter().x][location.getOuter().y].isLegal(location.getInner());

	}

	public void makeMove(BoardLocation location) {
		board[location.getOuter().x][location.getOuter().y].makeMove(location.getInner(), currentPlayer);
		lastMove = location.getInner();
		freeMove = board[location.getInner().x][location.getInner().y].isFinished();
		currentPlayer = currentPlayer == 'X' ? 'O' : 'X';
	}
}