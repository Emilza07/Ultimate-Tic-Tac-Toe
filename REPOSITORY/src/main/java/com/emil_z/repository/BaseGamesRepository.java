package com.emil_z.repository;

import android.app.Application;
import android.graphics.Point;
import android.util.Log;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.emil_z.helper.PreferenceManager;
import com.emil_z.model.BoardLocation;
import com.emil_z.model.Game;
import com.emil_z.model.Games;
import com.emil_z.model.OnlineGame;
import com.emil_z.model.Player;
import com.emil_z.repository.BASE.BaseRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public abstract class BaseGamesRepository extends BaseRepository<Game, Games> {
	protected final MutableLiveData<Game> lvGame;
	protected final MutableLiveData<char[][]> lvOuterBoardWinners;
	protected final MutableLiveData<Boolean> lvIsFinished;
	protected final MutableLiveData<Boolean> lvIsStarted;
	protected int retryCounter = 0;
	protected final String localPlayerIdFs;



	public BaseGamesRepository(Application application) {
		super(Game.class, Games.class, application);
		lvGame = new MutableLiveData<>();
		lvOuterBoardWinners = new MutableLiveData<>();
		lvOuterBoardWinners.setValue(new char[3][3]);
		lvIsFinished = new MutableLiveData<>();
		lvIsStarted = new MutableLiveData<>(false);
		localPlayerIdFs = PreferenceManager.readFromSharedPreferences(application, "user_prefs", new Object[][]{{"UserIdFs", "String"}})[0][1].toString();
	}

	public LiveData<Game> getLiveDataGame() {
		return lvGame;
	}

	public LiveData<char[][]> getLiveDataOuterBoardWinners() {
		return lvOuterBoardWinners;
	}

	public LiveData<Boolean> getLiveDataIsFinished() {
		return lvIsFinished;
	}

	public LiveData<Boolean> getLiveDataIsStarted() {
		return lvIsStarted;
	}

	public LiveData<Games> getUserGames(String userId) {
		MutableLiveData<Games> result = new MutableLiveData<>();

		// Create a query for games where the user is player1
		Query player1Query = getCollection()
				.whereEqualTo("player1.idFs", userId);

		player1Query.get().addOnSuccessListener(queryDocuments -> {
			Games games = new Games();

			// Add all games where user is player1
			for (QueryDocumentSnapshot document : queryDocuments) {
				games.add(document.toObject(OnlineGame.class));
			}

			// Now query for games where user is player2
			getCollection()
					.whereEqualTo("player2.idFs", userId)
					.get()
					.addOnSuccessListener(player2Docs -> {
						// Add all games where user is player2
						for (QueryDocumentSnapshot document : player2Docs) {
							games.add(document.toObject(OnlineGame.class));
						}

						// Sort games by timestamp (newest first)
						games.sort((g1, g2) -> {
							Timestamp t1 = ((OnlineGame)g1).getStartedAt();
							Timestamp t2 = ((OnlineGame)g2).getStartedAt();
							return t2.compareTo(t1);
						});

						// Set the result with all games
						result.setValue(games);
					})
					.addOnFailureListener(e -> {
						Log.e("GamesRepository", "Error getting player2 games: " + e.getMessage());
						// Either return nothing or set error state
						result.setValue(new Games()); // OR handle as complete failure
					});
		}).addOnFailureListener(e -> {
			Log.e("GamesRepository", "Error getting player1 games: " + e.getMessage());
			result.setValue(new Games());
		});

		return result;
	}

	@Override
	protected Query getQueryForExist(Game entity) {
		return getCollection().whereEqualTo("idFs", entity.getIdFs());
	}

	public abstract void startGame(Player player, String crossPlayerIdFs);

	public Task<Void> makeMove(BoardLocation location) {
		TaskCompletionSource<Void> taskMakeMove = new TaskCompletionSource<>();
		if(Objects.equals(lvGame.getValue().getCurrentPlayerIdFs(), localPlayerIdFs)) {
			if(lvGame.getValue().isLegal(location)) {
				lvGame.getValue().makeMove(location);
				lvGame.setValue(lvGame.getValue());
				checkInnerBoardFinish(location.getOuter());
				taskMakeMove.setResult(null);
			}
			else taskMakeMove.setException(new Exception("2"));
		}
		else taskMakeMove.setException(new Exception("1"));
		return taskMakeMove.getTask();
	}

	protected void checkInnerBoardFinish(Point innerBoard) {
		if(lvGame.getValue().getOuterBoard().getBoard(innerBoard).isFinished()){			//check if the inner board is finished
			if(lvGame.getValue().getOuterBoard().getBoard(innerBoard).getWinner() != 'T'){	//check if the inner board isn't a tie
				char[][] tmp = new char[3][3];
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						tmp[i][j] = lvOuterBoardWinners.getValue()[i][j];
					}
				}
				tmp[innerBoard.x][innerBoard.y] = lvGame.getValue().getOuterBoard().getBoard(innerBoard).getWinner();
				lvOuterBoardWinners.postValue(tmp);
			}
			else {
				lvOuterBoardWinners.getValue()[innerBoard.x][innerBoard.y] = 'T';
			}
			if(lvGame.getValue().getOuterBoard().isGameOver()){
				lvGame.getValue().setFinished(true);
			}

		}
	}
}