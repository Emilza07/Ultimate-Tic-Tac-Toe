package com.emil_z.repository;

import android.app.Application;
import android.graphics.Point;

import com.emil_z.model.BoardLocation;
import com.emil_z.model.Cpu;
import com.emil_z.model.CpuGame;
import com.emil_z.model.Game;
import com.emil_z.model.Player;

/**
 * Repository for managing CPU games.
 * Extends {@link BaseGamesRepository} to provide game logic specific to games against the CPU.
 */
public class CpuGamesRepository extends BaseGamesRepository {
	/**
	 * Constructs a CpuGamesRepository with the given application context.
	 * @param application The application context.
	 */
	public CpuGamesRepository(Application application) {
		super(application);
	}

	/**
	 * Starts a new CPU game with the specified player and cross player ID.
	 * Initializes the game state and marks the game as started.
	 * @param player The player starting the game.
	 * @param crossPlayerIdFs The Firestore ID of the cross player.
	 */
	@Override
	public void startGame(Player player, String crossPlayerIdFs) {
		lvGame.setValue(new CpuGame(localPlayerIdFs, crossPlayerIdFs));
		lvIsStarted.setValue(true);
	}

	/**
	 * Checks if the inner board at the given point is finished.
	 * If the game is finished, updates the LiveData indicating game completion.
	 * @param innerBoard The coordinates of the inner board to check.
	 */
	@SuppressWarnings("ConstantConditions")
	@Override
	protected void checkInnerBoardFinish(Point innerBoard) {
		super.checkInnerBoardFinish(innerBoard);
		if (lvGame.getValue().isFinished())
			lvIsFinished.postValue(true);
	}

	/**
	 * Makes a move for the CPU by finding the best move and updating the game state.
	 * After the move, checks if the inner board is finished.
	 */
	@SuppressWarnings("ConstantConditions")
	public void makeCpuMove() {
		Game game = lvGame.getValue();
		BoardLocation location = Cpu.findBestMove(game.getOuterBoard());
		game.makeMove(location);
		lvGame.postValue(game);
		checkInnerBoardFinish(location.getOuter());
	}
}