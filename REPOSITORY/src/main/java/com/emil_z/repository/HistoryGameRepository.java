package com.emil_z.repository;

import android.app.Application;

import com.emil_z.model.Game;
import com.emil_z.model.OnlineGame;
import com.emil_z.model.Player;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.Query;

public class HistoryGameRepository extends BaseGamesRepository {

	public HistoryGameRepository(Application application) {
		super(application);
	}

	@Override
	public void startGame(Player player, String crossPlayerIdFs) {
	}

	public void startGame(Game game) {
		lvGame.setValue(new Game(game));
		lvIsStarted.setValue(true);
	}

	public Task<OnlineGame> getOnlineGame(String gameId) {
		TaskCompletionSource<OnlineGame> tcs = new TaskCompletionSource<>();
		Query gameQuery = getCollection()
				.whereEqualTo("idFs", gameId);

		gameQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
			OnlineGame game = queryDocumentSnapshots.getDocuments().get(0).toObject(OnlineGame.class);
			tcs.setResult(game);
		});
		return tcs.getTask();
	}
}