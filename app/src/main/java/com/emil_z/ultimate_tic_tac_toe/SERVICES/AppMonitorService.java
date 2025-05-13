package com.emil_z.ultimate_tic_tac_toe.SERVICES;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.emil_z.repository.OnlineGamesRepository;

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

	public static void startService(Context context, boolean inGame, String gameId,
									String player1Id, String player2Id, boolean isPlayer1) {
		Intent intent = new Intent(context, AppMonitorService.class);
		intent.putExtra(EXTRA_IN_GAME, inGame);
		intent.putExtra(EXTRA_GAME_ID, gameId);
		intent.putExtra(EXTRA_PLAYER1_ID, player1Id);
		intent.putExtra(EXTRA_PLAYER2_ID, player2Id);
		intent.putExtra(EXTRA_IS_PLAYER1, isPlayer1);
		context.startForegroundService(intent);
		Log.d("AppMonitor", "Starting service with gameId: " + gameId);
	}

	public static void userClosedActivity(Context context) {
		Intent intent = new Intent(context, AppMonitorService.class);
		intent.putExtra(EXTRA_IN_GAME, false);
		intent.putExtra(EXTRA_GAME_ID, gameId);
		intent.putExtra(EXTRA_PLAYER1_ID, player1Id);
		intent.putExtra(EXTRA_PLAYER2_ID, player2Id);
		intent.putExtra(EXTRA_IS_PLAYER1, isPlayer1);
		context.startService(intent);
	}

	public static void updateGameState(boolean inGame, String gameId,
									   String player1Id, String player2Id, boolean isPlayer1) {
		AppMonitorService.inGameActivity = inGame;
		AppMonitorService.gameId = gameId;
		AppMonitorService.player1Id = player1Id;
		AppMonitorService.player2Id = player2Id;
		AppMonitorService.isPlayer1 = isPlayer1;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		repository = new OnlineGamesRepository((Application) getApplicationContext());
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

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

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);

		if (inGameActivity && gameId != null) {
			repository.exitGameDirect(gameId, player1Id, player2Id, isPlayer1)
					.addOnSuccessListener(aBoolean -> {
					})
					.addOnFailureListener(e -> {
					});
		}
		stopSelf();
	}

	private Notification createNotification() {
		NotificationChannel channel = new NotificationChannel(
				"app_monitor_channel",
				"App Monitor",
				NotificationManager.IMPORTANCE_LOW
		);
		NotificationManager manager = getSystemService(NotificationManager.class);
		manager.createNotificationChannel(channel);

		return new NotificationCompat.Builder(this, "app_monitor_channel")
				.setContentTitle("Game in progress")
				.setSmallIcon(android.R.drawable.ic_dialog_info)
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.build();
	}
}