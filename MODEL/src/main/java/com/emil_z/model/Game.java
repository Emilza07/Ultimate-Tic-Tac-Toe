package com.emil_z.model;

import com.emil_z.model.BASE.BaseEntity;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Game extends BaseEntity implements Serializable {
	protected boolean isStarted;
	protected boolean isFinished;
	protected String winner;
	protected List<BoardLocation> moves;
	@Exclude
	protected OuterBoard outerBoard;
	protected Player player1;
	protected Player player2;
	protected PlayerType currentPlayer;

	public Game() {
		this.isStarted = false;
		this.isFinished = false;
		this.winner = null;
		this.moves = new ArrayList<>();
		this.outerBoard = new OuterBoard();
		this.player1 = new Player();
		this.player2 = new Player();
		this.currentPlayer = PlayerType.LOCAL;
	}
	public Game(Game game){
		this.isStarted = game.isStarted;
		this.isFinished = game.isFinished;
		this.winner = game.winner;
		this.moves = new ArrayList<>(game.moves);
		this.outerBoard = new OuterBoard();
		this.player1 = new Player(game.player1);
		this.player2 = new Player(game.player2);
		this.currentPlayer = game.currentPlayer;
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

	public String getWinner() {
		return winner;
	}

	public void setWinner(String winner) {
		this.winner = winner;
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

	public PlayerType getCurrentPlayer() {
		return currentPlayer;
	}

	public void setCurrentPlayer(PlayerType currentPlayer) {
		this.currentPlayer = currentPlayer;
	}
	//endregion

	public boolean isLegal(BoardLocation location) {
		return outerBoard.isLegal(location);
	}

	public void makeTurn(BoardLocation location) {
		outerBoard.makeTurn(location);
		moves.add(location);
	}
}