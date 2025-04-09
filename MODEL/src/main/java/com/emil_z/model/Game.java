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
	protected GameModel gameModel;
	protected Player player1;
	protected Player player2;

	public Game() {
		this.isStarted = false;
		this.isFinished = false;
		this.winner = null;
		this.moves = new ArrayList<>();
		this.gameModel = new GameModel();
		this.player1 = new Player();
		this.player2 = new Player();
	}
	public Game(Game game){
		this.isStarted = game.isStarted;
		this.isFinished = game.isFinished;
		this.winner = game.winner;
		this.moves = new ArrayList<>(game.moves);
		this.gameModel = new GameModel();
		this.player1 = new Player(game.player1);
		this.player2 = new Player(game.player2);
	}

	public Player getPlayer2() {
		return player2;
	}

	public void setPlayer2(Player player2) {
		this.player2 = player2;
	}

	public Player getPlayer1() {
		return player1;
	}

	public void setPlayer1(Player player1) {
		this.player1 = player1;
	}

	@Exclude
	public GameModel getGameModel() {
		return gameModel;
	}
	@Exclude
	public void setGameModel(GameModel gameModel) {
		this.gameModel = gameModel;
	}

	public List<BoardLocation> getMoves() {
		return moves;
	}

	public void setMoves(List<BoardLocation> moves) {
		this.moves = moves;
	}

	public String getWinner() {
		return winner;
	}

	public void setWinner(String winner) {
		this.winner = winner;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean finished) {
		isFinished = finished;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean started) {
		isStarted = started;
	}

	public Game makeTurn(BoardLocation location, PlayerType playerType){
		return null;
	}

}