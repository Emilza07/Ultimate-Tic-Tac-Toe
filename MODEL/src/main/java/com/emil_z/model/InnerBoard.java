package com.emil_z.model;

import com.emil_z.model.BASE.BaseEntity;

import java.io.Serializable;

public class InnerBoard extends BaseEntity implements Serializable {
	private char[][] board;
	private char winner;
	private boolean isOver;
	public InnerBoard(){
		board = new char[3][3];
		isOver = false;
		winner = 0;

	}
	public boolean isTie(){
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3 ; j++) {
				if(board[i][j] == 0){
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
			if(board[i][0] != 0 && board[i][0] == board[i][1] && board[i][0] == board[i][2]){
				winner = board[i][0];
				return true;
			}
			//check columns
			if(board[0][i] != 0 && board[0][i] == board[1][i] && board[0][i] == board[2][i]){
				winner = board[0][i];
				return true;
			}
		}
		//check diagonals
		if(board[0][0] != 0 && board[0][0] == board[1][1] && board[0][0] == board[2][2]){
			winner = board[0][0];
			return true;
		}
		if(board[0][2] != 0 && board[0][2] == board[1][1] && board[0][2] == board[2][0]){
			winner = board[0][2];
			return true;
		}
		return false;
	}
	public void resetGame(){
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3 ; j++) {
				board[i][j] = 0;
			}
		}
	}
	public char getWinner(){
		return winner;
	}
	public boolean isLegal(int row, int column){
		return board[row][column] == 0 && isGameOver() == false;
	}
	public void makeTurn(int row, int column, char currentPlayer){
		board[row][column] = currentPlayer;

	}
}