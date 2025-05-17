package com.emil_z.repository;

import android.app.Application;
import android.graphics.Point;

import com.emil_z.model.LocalGame;
import com.emil_z.model.Player;

/**
 * Repository for managing local game logic and state.
 * Extends {@link BaseGamesRepository} for LiveData and shared functionality.
 */
public class LocalGamesRepository extends BaseGamesRepository {
	/**
	 * Constructs a LocalGamesRepository with the given application context.
	 * @param application The application context.
	 */
	public LocalGamesRepository(Application application) {
		super(application);
	}

	/**
	 * Starts a new local game with the specified player and cross player ID.
	 * Initializes the game state and marks the game as started.
	 * @param player The player starting the game.
	 * @param crossPlayerIdFs The Firestore ID of the cross player.
	 */
	@Override
	public void startGame(Player player, String crossPlayerIdFs) {
		lvGame.setValue(new LocalGame(localPlayerIdFs));
		lvIsStarted.setValue(true);
	}

	/**
	 * Checks if the inner board is finished and updates the finished state.
	 * @param innerBoard The inner board coordinates.
	 */
	@SuppressWarnings("ConstantConditions")
	@Override
	protected void checkInnerBoardFinish(Point innerBoard) {
		super.checkInnerBoardFinish(innerBoard);
		if (lvGame.getValue().isFinished())
			lvIsFinished.setValue(true);
	}
}