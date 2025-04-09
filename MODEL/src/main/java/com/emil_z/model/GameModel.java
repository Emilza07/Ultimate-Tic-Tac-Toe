package com.emil_z.model;

public class GameModel {
	private InnerBoard[][] board;
	private char winner;
	private char currentPlayer;
	private boolean freeMove;
	private int lastMoveRow;
	private int lastMoveColumn;
	public GameModel(){
		board = new InnerBoard[3][3];
		winner = 0;
		currentPlayer = 'X';
		freeMove = true;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3 ; j++) {
				board[i][j] = new InnerBoard();
			}
		}
	}
	public InnerBoard getBoard(int outerRow, int outerColumn){
		return board[outerRow][outerColumn];
	}
	public char getCurrentPlayer(){return currentPlayer;}
	public char getWinner(){return winner;}
	public boolean isTie(){
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3 ; j++) {
				if(!board[i][j].isTie()){
					return false;
				}
			}
		}
		return true;
	}
	public boolean isGameOver(){
		//check if the game is a tie
		if(isTie()){
			return true;
		}
		//check rows
		for (int i = 0; i < 3; i++) {
			if(board[i][0].getWinner() != 0 && board[i][0].getWinner() == board[i][1].getWinner() && board[i][0].getWinner() == board[i][2].getWinner()){
				winner = board[i][0].getWinner();
				return true;
			}
			//check columns
			if(board[0][i].getWinner() != 0 && board[0][i].getWinner() == board[1][i].getWinner() && board[0][i].getWinner() == board[2][i].getWinner()){
				winner = board[0][i].getWinner();
				return true;
			}
		}
		//check diagonals
		if(board[0][0].getWinner() != 0 && board[0][0].getWinner() == board[1][1].getWinner() && board[0][0].getWinner() == board[2][2].getWinner()){
			winner = board[0][0].getWinner();
			return true;
		}
		if(board[0][2].getWinner() != 0 && board[0][2].getWinner() == board[1][1].getWinner() && board[0][2].getWinner() == board[2][0].getWinner()){
			winner = board[0][2].getWinner();
			return true;
		}
		return false;
	}
	public void resetGame(){
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3 ; j++) {
				board[i][j].resetGame();
			}
		}
	}
	public boolean isLegal(byte row, byte column){
		if(freeMove) {
			return board[row/3][column/3].isLegal(row%3, column%3);
		}
		return row/3 == lastMoveRow && column/3 == lastMoveColumn && board[row/3][column/3].isLegal(row%3, column%3);


	}
	public boolean makeTurn(byte row, byte column) {
		if(isLegal(row, column)){
			board[row/3][column/3].makeTurn(row%3, column%3, currentPlayer);
			lastMoveRow = row%3;
			lastMoveColumn = column%3;
			freeMove = board[lastMoveRow][lastMoveColumn].isGameOver();
			currentPlayer = currentPlayer == 'X' ? 'O' : 'X';
			return true;
		}
		return false;
	}
}