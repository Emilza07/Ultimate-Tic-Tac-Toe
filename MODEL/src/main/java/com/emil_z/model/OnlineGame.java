package com.emil_z.model;

public class OnlineGame extends Game{
	public OnlineGame(){
		super();
	}

	public OnlineGame(Player player1) {
		super();
		this.player1 = player1;
	}

	public OnlineGame(Game game) {
	}

	public void startGameForJoiner() {
		outerBoard = new OuterBoard();
	}

	public void makeTurn(BoardLocation location){
		super.makeTurn(location);
		if (currentPlayerIdFs == player1.getIdFs()) {
			currentPlayerIdFs = player2.getIdFs();
		} else {
			currentPlayerIdFs = player1.getIdFs();
		}
		if (outerBoard.isGameOver()) {
			isFinished = true;
			winner = outerBoard.getWinner() == 'X' ? player1.getIdFs() : player2.getIdFs(); //TODO: when added the option that p1 isn't always 'X' depend this to the player that began
		}
	}
}