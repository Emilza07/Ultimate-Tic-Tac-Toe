package com.emil_z.model;

public class OnlineGame extends Game{
	public OnlineGame(){
		super();
	}
	public OnlineGame(User player1) {
		super();
		this.player1 = new Player(player1.getIdFs(), player1.getUsername(), player1.getElo());
		this.player1.setPlayerType(PlayerType.LOCAL);
	}
	public OnlineGame(Game game) {
	}
	public void startGameForJoiner() {
		outerBoard = new OuterBoard();
		player1.setPlayerType(PlayerType.REMOTE);
		player2.setPlayerType(PlayerType.LOCAL);
	}

	public void makeTurn(BoardLocation location){
		super.makeTurn(location);
		if (currentPlayer == PlayerType.LOCAL) {
			currentPlayer = PlayerType.REMOTE;
		} else {
			currentPlayer = PlayerType.LOCAL;
		}
		if (outerBoard.isGameOver()) {
			isFinished = true;
			winner = outerBoard.getWinner() == 'X' ? player1.getIdFs() : player2.getIdFs(); //TODO: when added the option that p1 isn't always 'X' depend this to the player that began
		}
	}
}