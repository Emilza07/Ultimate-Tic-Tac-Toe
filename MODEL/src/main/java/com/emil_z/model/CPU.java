package com.emil_z.model;

import java.util.ArrayList;

public class CPU {

	private static final int PLAYER = 1;
	private static final int AI = -1;
	private static final int BOARD_SIZE = 9;

	// Helper method to check win conditions for a standard Tic Tac Toe board
	private int checkWinCondition(int[] map) {
		int a = PLAYER;

		// Check rows, columns, and diagonals
		if (map[0] + map[1] + map[2] == a * 3 || map[3] + map[4] + map[5] == a * 3 || map[6] + map[7] + map[8] == a * 3 ||
				map[0] + map[3] + map[6] == a * 3 || map[1] + map[4] + map[7] == a * 3 || map[2] + map[5] + map[8] == a * 3 ||
				map[0] + map[4] + map[8] == a * 3 || map[2] + map[4] + map[6] == a * 3) {
			return a;
		}

		a = AI;
		if (map[0] + map[1] + map[2] == a * 3 || map[3] + map[4] + map[5] == a * 3 || map[6] + map[7] + map[8] == a * 3 ||
				map[0] + map[3] + map[6] == a * 3 || map[1] + map[4] + map[7] == a * 3 || map[2] + map[5] + map[8] == a * 3 ||
				map[0] + map[4] + map[8] == a * 3 || map[2] + map[4] + map[6] == a * 3) {
			return a;
		}

		return 0;
	}

	// Evaluate the game state
	private int evaluateGame(int[][] position, int currentBoard) {
		int eval = 0;
		int[] mainBd = new int[BOARD_SIZE];
		double[] evaluatorMul = {1.4, 1, 1.4, 1, 1.75, 1, 1.4, 1, 1.4};

		for (int i = 0; i < BOARD_SIZE; i++) {
			eval += realEvaluateSquare(position[i]) * 1.5 * evaluatorMul[i];
			if (i == currentBoard) {
				eval += realEvaluateSquare(position[i]) * evaluatorMul[i];
			}
			int tmpEv = checkWinCondition(position[i]);
			eval -= tmpEv * evaluatorMul[i];
			mainBd[i] = tmpEv;
		}
		eval -= checkWinCondition(mainBd) * 5000;
		eval += realEvaluateSquare(mainBd) * 150;

		return eval;
	}

	// Minimax algorithm
	private Result miniMax(int[][] position, int boardToPlayOn, int depth, int alpha, int beta, boolean maximizingPlayer) {
		if (depth <= 0 || Math.abs(evaluateGame(position, boardToPlayOn)) > 5000) {
			return new Result(evaluateGame(position, boardToPlayOn), -1);
		}

		if (boardToPlayOn != -1 && checkWinCondition(position[boardToPlayOn]) != 0) {
			boardToPlayOn = -1;
		}

		if (boardToPlayOn != -1 && !containsZero(position[boardToPlayOn])) {
			boardToPlayOn = -1;
		}

		if (maximizingPlayer) {
			int maxEval = Integer.MIN_VALUE;
			int tmpPlay = -1;

			for (int boardIndex = 0; boardIndex < BOARD_SIZE; boardIndex++) {
				if (boardToPlayOn == -1 || boardIndex == boardToPlayOn) {
					for (int cell = 0; cell < BOARD_SIZE; cell++) {
						if (position[boardIndex][cell] == 0) {
							position[boardIndex][cell] = AI;
							int eval = miniMax(position, cell, depth - 1, alpha, beta, false).evaluation;
							position[boardIndex][cell] = 0;

							if (eval > maxEval) {
								maxEval = eval;
								tmpPlay = boardIndex;
							}
							alpha = Math.max(alpha, eval);
							if (beta <= alpha) break;
						}
					}
				}
			}
			return new Result(maxEval, tmpPlay);
		} else {
			int minEval = Integer.MAX_VALUE;
			int tmpPlay = -1;

			for (int boardIndex = 0; boardIndex < BOARD_SIZE; boardIndex++) {
				if (boardToPlayOn == -1 || boardIndex == boardToPlayOn) {
					for (int cell = 0; cell < BOARD_SIZE; cell++) {
						if (position[boardIndex][cell] == 0) {
							position[boardIndex][cell] = PLAYER;
							int eval = miniMax(position, cell, depth - 1, alpha, beta, true).evaluation;
							position[boardIndex][cell] = 0;

							if (eval < minEval) {
								minEval = eval;
								tmpPlay = boardIndex;
							}
							beta = Math.min(beta, eval);
							if (beta <= alpha) break;
						}
					}
				}
			}
			return new Result(minEval, tmpPlay);
		}
	}

	// Utility to check if an array contains zero
	private boolean containsZero(int[] array) {
		for (int value : array) {
			if (value == 0) return true;
		}
		return false;
	}

	// Evaluate a single board's state
	private int realEvaluateSquare(int[] pos) {
		int evaluation = 0;
		double[] points = {0.2, 0.17, 0.2, 0.17, 0.22, 0.17, 0.2, 0.17, 0.2};

		for (int i = 0; i < pos.length; i++) {
			evaluation -= pos[i] * points[i];
		}

		return evaluation;
	}

	// Result class to handle minimax output
	private static class Result {
		int evaluation;
		int board;

		Result(int evaluation, int board) {
			this.evaluation = evaluation;
			this.board = board;
		}
	}
}