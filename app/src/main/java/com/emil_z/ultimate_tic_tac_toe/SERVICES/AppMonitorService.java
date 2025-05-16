package com.emil_z.ultimate_tic_tac_toe.SERVICES;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.emil_z.repository.OnlineGamesRepository;

/**
 * Service to monitor the app's game state and handle foreground notifications
 * for ongoing games. Manages game state transitions and ensures proper cleanup
 * when the app is removed from recent tasks.
 */
public class AppMonitorService extends Service {
	private static final String EXTRA_IN_GAME = "in_game_activity";
	private static final String EXTRA_GAME_ID = "game_id";
	private static final String EXTRA_PLAYER1_ID = "player1_id";
	private static final String EXTRA_PLAYER2_ID = "player2_id";
	private static final String EXTRA_IS_PLAYER1 = "is_player1";

	private static boolean inGameActivity = false;
	private static String gameId;
	private static String player1Id;
	private static String player2Id;
	private static boolean isPlayer1;

	private OnlineGamesRepository repository;

	/**
	 * Starts the AppMonitorService as a foreground service with the provided game state.
	 *
	 * @param context    The context to use for starting the service.
	 * @param inGame     Whether the user is currently in a game.
	 * @param gameId     The ID of the current game.
	 * @param player1Id  The ID of player 1.
	 * @param player2Id  The ID of player 2.
	 * @param isPlayer1  Whether the current user is player 1.
	 */
	public static void startService(Context context, boolean inGame, String gameId,
									String player1Id, String player2Id, boolean isPlayer1) {
		Intent intent = new Intent(context, AppMonitorService.class)
			.putExtra(EXTRA_IN_GAME, inGame)
			.putExtra(EXTRA_PLAYER1_ID, player1Id)
			.putExtra(EXTRA_PLAYER2_ID, player2Id)
			.putExtra(EXTRA_GAME_ID, gameId)
			.putExtra(EXTRA_IS_PLAYER1, isPlayer1);
		context.startForegroundService(intent);
	}

	/**
	 * Updates the static game state fields.
	 *
	 * @param inGame     Whether the user is currently in a game.
	 * @param gameId     The ID of the current game.
	 * @param player1Id  The ID of player 1.
	 * @param player2Id  The ID of player 2.
	 * @param isPlayer1  Whether the current user is player 1.
	 */
	public static void updateGameState(boolean inGame, String gameId,
									   String player1Id, String player2Id, boolean isPlayer1) {
		inGameActivity = inGame;
		AppMonitorService.gameId = gameId;
		AppMonitorService.player1Id = player1Id;
		AppMonitorService.player2Id = player2Id;
		AppMonitorService.isPlayer1 = isPlayer1;
	}

	/**
	 * Notifies the service that the user has closed the game activity.
	 * Updates the game state to reflect that the user is no longer in a game.
	 *
	 * @param context The context to use for starting the service.
	 */
	public static void userClosedActivity(Context context) {
		Intent intent = new Intent(context, AppMonitorService.class)
			.putExtra(EXTRA_IN_GAME, false)
			.putExtra(EXTRA_GAME_ID, gameId)
			.putExtra(EXTRA_PLAYER1_ID, player1Id)
			.putExtra(EXTRA_PLAYER2_ID, player2Id)
			.putExtra(EXTRA_IS_PLAYER1, isPlayer1);
		context.startService(intent);
	}

	/**
	 * Initializes the repository when the service is created.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		repository = new OnlineGamesRepository((Application) getApplicationContext());
	}

	/**
	 * Not used, as this is not a bound service.
	 *
	 * @param intent The intent that was used to bind to this service.
	 * @return Always returns null.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Handles the start command for the service, updates the game state from the intent,
	 * and starts the service in the foreground with a notification.
	 *
	 * @param intent  The intent supplied to startService(Intent).
	 * @param flags   Additional data about this start request.
	 * @param startId A unique integer representing this specific request to start.
	 * @return The mode in which to continue running.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			inGameActivity = intent.getBooleanExtra(EXTRA_IN_GAME, false);
			gameId = intent.getStringExtra(EXTRA_GAME_ID);
			player1Id = intent.getStringExtra(EXTRA_PLAYER1_ID);
			player2Id = intent.getStringExtra(EXTRA_PLAYER2_ID);
			isPlayer1 = intent.getBooleanExtra(EXTRA_IS_PLAYER1, false);
		}
		startForeground(1001, createNotification());
		return START_STICKY;
	}

	/**
	 * Called when the app is removed from recent tasks.
	 * If a game is in progress, attempts to exit the game directly.
	 *
	 * @param rootIntent The intent that was used to remove the task.
	 */
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
		if (inGameActivity && gameId != null) {
			repository.exitGameDirect(gameId, player1Id, player2Id, isPlayer1)
				.addOnSuccessListener(a -> {
				})
				.addOnFailureListener(e -> {
				});
		}
		stopSelf();
	}

	/**
	 * Creates and returns a notification for the foreground service.
	 *
	 * @return The notification to display.
	 */
	private Notification createNotification() {
		NotificationManager manager = getSystemService(NotificationManager.class);
		manager.createNotificationChannel(new NotificationChannel(
			"app_monitor_channel", "App Monitor", NotificationManager.IMPORTANCE_LOW));

		return new NotificationCompat.Builder(this, "app_monitor_channel")
			.setContentTitle("Game in progress")
			.setSmallIcon(android.R.drawable.ic_dialog_info)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.build();
	}
}