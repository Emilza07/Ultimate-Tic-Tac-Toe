package com.emil_z.model;

import android.graphics.Point;

import java.util.Arrays;

class MinimaxResult {
	public double mE; // minimax evaluation
	public int tP;    // tmpPlay (board/square index)

	public MinimaxResult(double mE, int tP) {
		this.mE = mE;
		this.tP = tP;
	}
}

public class CPU {
	public static int RUNS = 0;
	public static int ai = -1;
	public static int player = 1;
	private static int moves = 0;

	/**
	 * Convert OuterBoard to the 2D integer array format expected by minimax
	 * @param outerBoard the current game state
	 * @return 2D int array representing game state (9x9)
	 */
	private static int[][] convertOuterBoardToPosition(OuterBoard outerBoard) {
		int[][] position = new int[9][9];

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				InnerBoard innerBoard = outerBoard.getBoard(new Point(i, j));
				for (int k = 0; k < 3; k++) {
					for (int l = 0; l < 3; l++) {
						char cell = innerBoard.getCell(new Point(k, l));
						int boardIndex = i * 3 + j;
						int squareIndex = k * 3 + l;

						if (cell == 'X') {
							position[boardIndex][squareIndex] = player;
						} else if (cell == 'O') {
							position[boardIndex][squareIndex] = ai;
						} else {
							position[boardIndex][squareIndex] = 0;
						}
					}
				}
			}
		}
		return position;
	}

	//SIMPLY CHECKS A NORMAL TIC TAC TOE BOARD, RETURNS 1 or -1 if a specific player has won, returns 0 if no one has won.
	private static int checkWinCondition(int[] map) {
		int a = 1;
		if (map[0] + map[1] + map[2] == a * 3 || map[3] + map[4] + map[5] == a * 3 || map[6] + map[7] + map[8] == a * 3 || map[0] + map[3] + map[6] == a * 3 || map[1] + map[4] + map[7] == a * 3 ||
				map[2] + map[5] + map[8] == a * 3 || map[0] + map[4] + map[8] == a * 3 || map[2] + map[4] + map[6] == a * 3) {
			return a;
		}
		a = -1;
		if (map[0] + map[1] + map[2] == a * 3 || map[3] + map[4] + map[5] == a * 3 || map[6] + map[7] + map[8] == a * 3 || map[0] + map[3] + map[6] == a * 3 || map[1] + map[4] + map[7] == a * 3 ||
				map[2] + map[5] + map[8] == a * 3 || map[0] + map[4] + map[8] == a * 3 || map[2] + map[4] + map[6] == a * 3) {
			return a;
		}
		return 0;
	}

	//The most important function, returns a numerical evaluation of the whole game in it's current state
	private static double evaluateGame(int[][] position, int currentBoard) {
		double evale = 0;
		int[] mainBd = new int[9];
		double[] evaluatorMul = {1.4, 1, 1.4, 1, 1.75, 1, 1.4, 1, 1.4};
		for (int eh = 0; eh < 9; eh++){
			evale += realEvaluateSquare(position[eh])*1.5*evaluatorMul[eh];
			if(eh == currentBoard){
				evale += realEvaluateSquare(position[eh])*evaluatorMul[eh];
			}
			int tmpEv = checkWinCondition(position[eh]);
			evale -= tmpEv*evaluatorMul[eh];
			mainBd[eh] = tmpEv;
		}
		evale -= checkWinCondition(mainBd)*5000;
		evale += realEvaluateSquare(mainBd)*150;
		return evale;
	}

	//minimax algorithm
	private static MinimaxResult miniMax(int[][] position, int boardToPlayOn, int depth, double alpha, double beta, boolean maximizingPlayer) {
		RUNS++;

		int tmpPlay = -1;

		double calcEval = evaluateGame(position, boardToPlayOn);
		if(depth <= 0 || Math.abs(calcEval) > 5000) {
			return new MinimaxResult(calcEval, tmpPlay);
		}
		//If the board to play on is -1, it means you can play on any board
		if(boardToPlayOn != -1 && checkWinCondition(position[boardToPlayOn]) != 0){
			boardToPlayOn = -1;
		}
		//If a board is full (doesn't include 0), it also sets the board to play on to -1
		if(boardToPlayOn != -1) {
			boolean includesZero = false;
			for (int val : position[boardToPlayOn]) {
				if (val == 0) {
					includesZero = true;
					break;
				}
			}
			if (!includesZero) {
				boardToPlayOn = -1;
			}
		}


		if(maximizingPlayer){
			double maxEval = Double.NEGATIVE_INFINITY;
			for(int mm = 0; mm < 9; mm++){
				double evalut = Double.NEGATIVE_INFINITY;
				//If you can play on any board, you have to go through all of them
				if(boardToPlayOn == -1){
					for(int trr = 0; trr < 9; trr++){
						//Except the ones which are won
						if(checkWinCondition(position[mm]) == 0){
							if(position[mm][trr] == 0){
								position[mm][trr] = ai;
								//tmpPlay = pickBoard(position, true);
								evalut = miniMax(position, trr, depth-1, alpha, beta, false).mE;
								//evalut+=150;
								position[mm][trr] = 0;
							}
							if(evalut > maxEval){
								maxEval = evalut;
								tmpPlay = mm;
							}
							alpha = Math.max(alpha, evalut);
						}

					}
					if(beta <= alpha){
						break;
					}
					//If there's a specific board to play on, you just go through it's squares
				}else{
					MinimaxResult evalutResult = null;
					if(position[boardToPlayOn][mm] == 0){
						position[boardToPlayOn][mm] = ai;
						evalutResult = miniMax(position, mm, depth-1, alpha, beta, false);
						position[boardToPlayOn][mm] = 0;
					}
					// Check if evalutResult is not null before accessing mE
					double blop = (evalutResult != null) ? evalutResult.mE : Double.NEGATIVE_INFINITY; // Handle case where no move was possible in this square
					if(blop > maxEval){
						maxEval = blop;
						//Saves which board you should play on, so that this can be passed on when the AI is allowed to play in any board
						tmpPlay = evalutResult.tP;
					}
					alpha = Math.max(alpha, blop);
					if(beta <= alpha){
						break;
					}
				}
			}
			return new MinimaxResult(maxEval, tmpPlay);
		}else{
			double minEval = Double.POSITIVE_INFINITY;
			for(int mm = 0; mm < 9; mm++){
				double evalua = Double.POSITIVE_INFINITY;
				if(boardToPlayOn == -1){
					for(int trr = 0; trr < 9; trr++){
						if(checkWinCondition(position[mm]) == 0){
							if(position[mm][trr] == 0){
								position[mm][trr] = player;
								//tmpPlay = pickBoard(position, true);
								evalua = miniMax(position, trr, depth-1, alpha, beta, true).mE;
								//evalua -= 150;
								position[mm][trr] = 0;
							}
							if(evalua < minEval){
								minEval = evalua;
								tmpPlay = mm;
							}
							beta = Math.min(beta, evalua);
						}

					}
					if(beta <= alpha){
						break;
					}
				}else{
					MinimaxResult evaluaResult = null;
					if(position[boardToPlayOn][mm] == 0){
						position[boardToPlayOn][mm] = player;
						evaluaResult = miniMax(position, mm, depth-1, alpha, beta, true);
						position[boardToPlayOn][mm] = 0;
					}
					double blep = (evaluaResult != null) ? evaluaResult.mE : Double.POSITIVE_INFINITY; // Handle case where no move was possible in this square
					if(blep < minEval){
						minEval = blep;
						tmpPlay = evaluaResult.tP;
					}
					beta = Math.min(beta, blep);
					if(beta <= alpha){
						break;
					}
				}
			}
			return new MinimaxResult(minEval, tmpPlay);
		}
	}

	//Low number means losing the board, big number means winning
	//Tbf this is less an evaluation algorithm and more something that figures out where the AI should move to win normal Tic Tac Toe
	private static double evaluatePos(int[] pos, int square){
		int[] posCopy = new int[pos.length];
		System.arraycopy(pos, 0, posCopy, 0, pos.length);

		posCopy[square] = ai;
		double evaluation = 0;
		//Prefer center over corners over edges
		//evaluation -= (pos[0]*0.2+pos[1]*0.1+pos[2]*0.2+pos[3]*0.1+pos[4]*0.25+pos[5]*0.1+pos[6]*0.2+pos[7]*0.1+pos[8]*0.2);
		double[] points = {0.2, 0.17, 0.2, 0.17, 0.22, 0.17, 0.2, 0.17, 0.2};

		int a = 2;
		evaluation+=points[square];
		//Prefer creating pairs
		a = -2;
		if(posCopy[0] + posCopy[1] + posCopy[2] == a || posCopy[3] + posCopy[4] + posCopy[5] == a || posCopy[6] + posCopy[7] + posCopy[8] == a || posCopy[0] + posCopy[3] + posCopy[6] == a || posCopy[1] + posCopy[4] + posCopy[7] == a ||
				posCopy[2] + posCopy[5] + posCopy[8] == a || posCopy[0] + posCopy[4] + posCopy[8] == a || posCopy[2] + posCopy[4] + posCopy[6] == a) {
			evaluation += 1;
		}
		//Take victories
		a = -3;
		if(posCopy[0] + posCopy[1] + posCopy[2] == a || posCopy[3] + posCopy[4] + posCopy[5] == a || posCopy[6] + posCopy[7] + posCopy[8] == a || posCopy[0] + posCopy[3] + posCopy[6] == a || posCopy[1] + posCopy[4] + posCopy[7] == a ||
				posCopy[2] + posCopy[5] + posCopy[8] == a || posCopy[0] + posCopy[4] + posCopy[8] == a || posCopy[2] + posCopy[4] + posCopy[6] == a) {
			evaluation += 5;
		}

		//Block a players turn if necessary
		posCopy[square] = player;

		a = 3;
		if(posCopy[0] + posCopy[1] + posCopy[2] == a || posCopy[3] + posCopy[4] + posCopy[5] == a || posCopy[6] + posCopy[7] + posCopy[8] == a || posCopy[0] + posCopy[3] + posCopy[6] == a || posCopy[1] + posCopy[4] + posCopy[7] == a ||
				posCopy[2] + posCopy[5] + posCopy[8] == a || posCopy[0] + posCopy[4] + posCopy[8] == a || posCopy[2] + posCopy[4] + posCopy[6] == a) {
			evaluation += 2;
		}

		posCopy[square] = ai;

		evaluation -= checkWinCondition(posCopy)*15;

		//evaluation -= checkWinCondition(pos)*4;

		return evaluation;
	}

	//This function actually evaluates a board fairly
	private static double realEvaluateSquare(int[] pos){
		double evaluation = 0;
		double[] points = {0.2, 0.17, 0.2, 0.17, 0.22, 0.17, 0.2, 0.17, 0.2};

		for(int bw = 0; bw < pos.length; bw++){
			evaluation -= pos[bw]*points[bw];
		}

		int a = 2;
		if(pos[0] + pos[1] + pos[2] == a || pos[3] + pos[4] + pos[5] == a || pos[6] + pos[7] + pos[8] == a) {
			evaluation -= 6;
		}
		if(pos[0] + pos[3] + pos[6] == a || pos[1] + pos[4] + pos[7] == a || pos[2] + pos[5] + pos[8] == a) {
			evaluation -= 6;
		}
		if(pos[0] + pos[4] + pos[8] == a || pos[2] + pos[4] + pos[6] == a) {
			evaluation -= 7;
		}

		a = -1;
		if((pos[0] + pos[1] == 2*a && pos[2] == -a) || (pos[1] + pos[2] == 2*a && pos[0] == -a) || (pos[0] + pos[2] == 2*a && pos[1] == -a)
				|| (pos[3] + pos[4] == 2*a && pos[5] == -a) || (pos[3] + pos[5] == 2*a && pos[4] == -a) || (pos[5] + pos[4] == 2*a && pos[3] == -a)
				|| (pos[6] + pos[7] == 2*a && pos[8] == -a) || (pos[6] + pos[8] == 2*a && pos[7] == -a) || (pos[7] + pos[8] == 2*a && pos[6] == -a)
				|| (pos[0] + pos[3] == 2*a && pos[6] == -a) || (pos[0] + pos[6] == 2*a && pos[3] == -a) || (pos[3] + pos[6] == 2*a && pos[0] == -a)
				|| (pos[1] + pos[4] == 2*a && pos[7] == -a) || (pos[1] + pos[7] == 2*a && pos[4] == -a) || (pos[4] + pos[7] == 2*a && pos[1] == -a)
				|| (pos[2] + pos[5] == 2*a && pos[8] == -a) || (pos[2] + pos[8] == 2*a && pos[5] == -a) || (pos[5] + pos[8] == 2*a && pos[2] == -a)
				|| (pos[0] + pos[4] == 2*a && pos[8] == -a) || (pos[0] + pos[8] == 2*a && pos[4] == -a) || (pos[4] + pos[8] == 2*a && pos[0] == -a)
				|| (pos[2] + pos[4] == 2*a && pos[6] == -a) || (pos[2] + pos[6] == 2*a && pos[4] == -a) || (pos[4] + pos[6] == 2*a && pos[2] == -a)){
			evaluation-=9;
		}

		a = -2;
		if(pos[0] + pos[1] + pos[2] == a || pos[3] + pos[4] + pos[5] == a || pos[6] + pos[7] + pos[8] == a) {
			evaluation += 6;
		}
		if(pos[0] + pos[3] + pos[6] == a || pos[1] + pos[4] + pos[7] == a || pos[2] + pos[5] + pos[8] == a) {
			evaluation += 6;
		}
		if(pos[0] + pos[4] + pos[8] == a || pos[2] + pos[4] + pos[6] == a) {
			evaluation += 7;
		}

		a = 1;
		if((pos[0] + pos[1] == 2*a && pos[2] == -a) || (pos[1] + pos[2] == 2*a && pos[0] == -a) || (pos[0] + pos[2] == 2*a && pos[1] == -a)
				|| (pos[3] + pos[4] == 2*a && pos[5] == -a) || (pos[3] + pos[5] == 2*a && pos[4] == -a) || (pos[5] + pos[4] == 2*a && pos[3] == -a)
				|| (pos[6] + pos[7] == 2*a && pos[8] == -a) || (pos[6] + pos[8] == 2*a && pos[7] == -a) || (pos[7] + pos[8] == 2*a && pos[6] == -a)
				|| (pos[0] + pos[3] == 2*a && pos[6] == -a) || (pos[0] + pos[6] == 2*a && pos[3] == -a) || (pos[3] + pos[6] == 2*a && pos[0] == -a)
				|| (pos[1] + pos[4] == 2*a && pos[7] == -a) || (pos[1] + pos[7] == 2*a && pos[4] == -a) || (pos[4] + pos[7] == 2*a && pos[1] == -a)
				|| (pos[2] + pos[5] == 2*a && pos[8] == -a) || (pos[2] + pos[8] == 2*a && pos[5] == -a) || (pos[5] + pos[8] == 2*a && pos[2] == -a)
				|| (pos[0] + pos[4] == 2*a && pos[8] == -a) || (pos[0] + pos[8] == 2*a && pos[4] == -a) || (pos[4] + pos[8] == 2*a && pos[0] == -a)
				|| (pos[2] + pos[4] == 2*a && pos[6] == -a) || (pos[2] + pos[6] == 2*a && pos[4] == -a) || (pos[4] + pos[6] == 2*a && pos[2] == -a)){
			evaluation+=9;
		}

		evaluation -= checkWinCondition(pos)*12;

		return evaluation;
	}

	public static BoardLocation findBestMove(OuterBoard outerBoard) {
		int bestMove = -1;
		double[] bestScore = new double[9]; // Ensure bestScore is initialized as a double array of size 9
		int[][] boards = convertOuterBoardToPosition(outerBoard);
		int boardToPlayOn = -1; // Default to free move
		if (!outerBoard.isFreeMove()) {
			Point lastMove = outerBoard.getLastMove();
			boardToPlayOn = lastMove.x * 3 + lastMove.y;
		}


		for (int i = 0; i < 9; i++) {
			bestScore[i] = Double.NEGATIVE_INFINITY;
		}


		RUNS = 0;
		//Calculates the remaining amount of empty squares
		int count = 0;
		for (int bt = 0; bt < boards.length; bt++) {
			if (checkWinCondition(boards[bt]) == 0) {
				for (int v : boards[bt]) {
					if (v == 0) {
						count++;
					}
				}
			}
		}


		if (boardToPlayOn == -1 || checkWinCondition(boards[boardToPlayOn]) != 0) {
			MinimaxResult savedMm;
			System.out.println("Remaining: " + count);

			//This minimax doesn't actually play a move, it simply figures out which board you should play on
			if (moves < 10) {
				// Use Math.min for depth, Double.NEGATIVE_INFINITY and Double.POSITIVE_INFINITY for alpha/beta
				savedMm = miniMax(boards, -1, Math.min(4, count), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true); //Putting math.min makes sure that minimax doesn't run when the board is full
			} else if (moves < 18) {
				savedMm = miniMax(boards, -1, Math.min(5, count), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
			} else {
				savedMm = miniMax(boards, -1, Math.min(6, count), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
			}
			System.out.println(savedMm.tP);
			boardToPlayOn = savedMm.tP;
		}

		//Just makes a quick default move for if all else fails
		for (int i = 0; i < 9; i++) {
			if (boards[boardToPlayOn][i] == 0) {
				bestMove = i;
				break;
			}
		}


		if (bestMove != -1) { //This condition should only be false if the board is full, but it's here in case

			//Best score is an array which contains individual scores for each square, here we're just changing them based on how good the move is on that one local board
			for (int a = 0; a < 9; a++) {
				if (boards[boardToPlayOn][a] == 0) {
					double score = evaluatePos(boards[boardToPlayOn], a) * 45; // Use double for score
					bestScore[a] = score;
				}
			}

			//And here we actually run minimax and add those values to the array
			if (checkWinCondition(boards[boardToPlayOn]) == 0) { // Check if the current board is still playable
				for (int b = 0; b < 9; b++) {
					if (boards[boardToPlayOn][b] == 0) {
						boards[boardToPlayOn][b] = ai; // Make the move temporarily
						MinimaxResult savedMm; // Use the custom class
						//Notice the stacking, at the beginning of the game, the depth is much lower than at the end
						if (moves < 20) {
							savedMm = miniMax(boards, b, Math.min(5, count), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
						} else if (moves < 32) {
							System.out.println("DEEP SEARCH");
							savedMm = miniMax(boards, b, Math.min(6, count), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
						} else {
							System.out.println("ULTRA DEEP SEARCH");
							savedMm = miniMax(boards, b, Math.min(7, count), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
						}
						double score2 = savedMm.mE;
						boards[boardToPlayOn][b] = 0; // Undo the move
						bestScore[b] += score2;
						//boardSel[b] = savedMm.tP;
						//console.log(score2);
					}
				}
			}

			//Chooses to play on the square with the highest evaluation in the bestScore array
			// Translate for...in loop iterating over indices to a standard for loop
			// Find the index of the maximum value in bestScore
			bestMove = 0;
			for (int i = 1; i < bestScore.length; i++) { // Iterate from the second element
				if (bestScore[i] > bestScore[bestMove]) {
					bestMove = i; // Update bestMove if a higher score is found
				}
			}
			moves += 1; // Increment moves by 2 for the AI's turn
			return new BoardLocation(new Point(boardToPlayOn / 3, boardToPlayOn % 3), new Point(bestMove / 3, bestMove % 3)); // Return the best move as a BoardLocation object
		}
		return null;
	}
}