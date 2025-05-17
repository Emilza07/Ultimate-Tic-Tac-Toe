package com.emil_z.repository;

import android.app.Application;

import com.emil_z.model.Game;
import com.emil_z.model.Player;

/**
 * Repository for managing replay games.
 * Extends {@link BaseGamesRepository} to provide logic for handling game replays.
 */
public class ReplayGamesRepository extends BaseGamesRepository {
	/**
	 * Constructs a ReplayGamesRepository with the given application context.
	 * @param application The application context.
	 */
	public ReplayGamesRepository(Application application) {
		super(application);
	}

	/**
	 * Starts a new game with the specified player and cross player ID.
	 * This method needs to be implemented, but we won't use it.
	 * @param player The player starting the game.
	 * @param crossPlayerIdFs The Firestore ID of the cross player.
	 */
	@Override
	public void startGame(Player player, String crossPlayerIdFs) {
	}

	/**
	 * Initializes the repository with a given game, setting it as the current game
	 * and marking it as started.
	 * @param game The game to load into the repository.
	 */
	public void startGame(Game game) {
		lvGame.setValue(new Game(game));
		lvIsStarted.setValue(true);
	}
}