package com.emil_z.model;

import com.emil_z.model.BASE.BaseEntity;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Game extends BaseEntity implements Serializable {
	protected boolean isStarted;
	protected boolean isFinished;
	protected String winnerIdFs;
	protected List<BoardLocation> moves;
	@Exclude
	protected OuterBoard outerBoard;
	protected Player player1;
	protected Player player2;
	protected String currentPlayerIdFs;
	protected String crossPlayerIdFs;

	public Game() {
		this.isStarted = false;
		this.isFinished = false;
		this.winnerIdFs = null;
		this.moves = new ArrayList<>();
		this.outerBoard = new OuterBoard();
		this.player1 = new Player();
		this.player2 = new Player();
	}
	public Game(Game game){
		this.isStarted = game.isStarted;
		this.isFinished = game.isFinished;
		this.winnerIdFs = game.winnerIdFs;
		this.moves = new ArrayList<>(game.moves);
		this.outerBoard = new OuterBoard();
		this.player1 = new Player(game.player1);
		this.player2 = new Player(game.player2);
		this.currentPlayerIdFs = game.currentPlayerIdFs;
	}

	//region getters and setters
	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean started) {
		isStarted = started;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean finished) {
		isFinished = finished;
	}

	public String getWinnerIdFs() {
		return winnerIdFs;
	}

	public void setWinnerIdFs(String winnerIdFs) {
		this.winnerIdFs = winnerIdFs;
	}

	public List<BoardLocation> getMoves() {
		return moves;
	}

	public void setMoves(List<BoardLocation> moves) {
		this.moves = moves;
	}

	@Exclude
	public OuterBoard getOuterBoard() {
		return outerBoard;
	}
	@Exclude
	public void setOuterBoard(OuterBoard outerBoard) {
		this.outerBoard = outerBoard;
	}

	public Player getPlayer1() {
		return player1;
	}

	public void setPlayer1(Player player1) {
		this.player1 = player1;
	}

	public Player getPlayer2() {
		return player2;
	}

	public void setPlayer2(Player player2) {
		this.player2 = player2;
	}

	public String getCurrentPlayerIdFs() {
		return currentPlayerIdFs;
	}

	public void setCurrentPlayerIdFs(String currentPlayerIdFs) {
		this.currentPlayerIdFs = currentPlayerIdFs;
	}

	public String getCrossPlayerIdFs() {
		return crossPlayerIdFs;
	}

	public void setCrossPlayerIdFs(String crossPlayerIdFs) {
		this.crossPlayerIdFs = crossPlayerIdFs;
	}
	//endregion

	public boolean isLegal(BoardLocation location) {
		return outerBoard.isLegal(location);
	}

	public void makeMove(BoardLocation location) {
		outerBoard.makeMove(location);
		moves.add(location);
	}
}