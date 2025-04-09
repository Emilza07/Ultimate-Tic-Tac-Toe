package com.emil_z.repository;

import android.app.Application;
import android.os.Handler;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.emil_z.model.BASE.GameType;
import com.emil_z.model.Game;
import com.emil_z.model.Games;
import com.emil_z.model.OnlineGame;
import com.emil_z.model.User;
import com.emil_z.repository.BASE.BaseRepository;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

public class GamesRepository extends BaseRepository<Game, Games> {
	private MutableLiveData<Game> lvGame;
	private GameType gameType;


	public LiveData<Game> getLvGame() {
		return lvGame;
	}

	public GamesRepository(Application application) {
		super(Game.class, Games.class, application);
		lvGame = new MutableLiveData<>();

	}

//	public Task<Boolean> startLocalGame(User user) {
//		TaskCompletionSource<Boolean> taskCreateGame = new TaskCompletionSource<>();
//		lvGame.setValue(new)
//	}
	public Task<Boolean> hostOnlineGame(User user) {

		TaskCompletionSource<Boolean> taskCreateGame = new TaskCompletionSource<>();
		lvGame.setValue(new OnlineGame(user));
		add(lvGame.getValue()).addOnCompleteListener(new OnCompleteListener<Boolean>() {
			@Override
			public void onComplete(@NonNull Task<Boolean> task) {
				if(task.isSuccessful()){
					taskCreateGame.setResult(true); // inform the viewmodel that the game was created and uploaded to the database
				}
				else {
					taskCreateGame.setResult(false);
				}
			}
		});
		return taskCreateGame.getTask();
	}

	public void startOnlineGame() {
		lvGame.getValue().setStarted(true);
		update(lvGame.getValue());
	}

	public void joinOnlineGame(User user, OnSuccessListener<String> onSuccessListener, OnFailureListener onFailureListener) {
		//functions = FirebaseFunctions.getInstance();
		function.useEmulator("127.0.0.1", 5001);
		getNotStartedGames(remoteGames -> {
			if (remoteGames != null) {
				getGameIdWithSimilarElo(user.getElo(), remoteGames, gameId -> {
					//run a cloud function to join the game
					Log.d("qqq", "joinedOnlineGame: " + gameId);
					joinGame(gameId, user.getIdFs(), user.getName(), user.getElo()).addOnCompleteListener(new OnCompleteListener<String>() {
						@Override
						public void onComplete(@NonNull Task<String> task) {
							if (task.isSuccessful()) {
								// got status 200/400/409
								String status = task.getResult();
								if(Objects.equals(status, "200")){
									lvGame.setValue(new OnlineGame(get(gameId).getResult()));
									onSuccessListener.onSuccess("Joined"); // inform the viewmodel that successfully joined the game
									addSnapshotListener(gameId);
								}
								else if(Objects.equals(status, "400")){
									// garbage data
									onFailureListener.onFailure(new Exception("Garbage data"));
								}
								else if(Objects.equals(status, "409")){
									// game is full (someone wrote into the document in the meantime)
									onFailureListener.onFailure(new Exception("Game is full"));
								}
							} else {
								// Handle failure
								Exception e = task.getException();
								if (e instanceof FirebaseFunctionsException) {

									FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
									FirebaseFunctionsException.Code code = ffe.getCode();
									Object details = ffe.getDetails();
									onFailureListener.onFailure(new Exception("Error: " + code + ", Details: " + details));
								}
							}
						}
					});
				}, new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						// unable to find a game with similar elo
						onFailureListener.onFailure(new Exception("Unable to find a game with similar elo"));
					}
				});
			} else {
				// No games found
				onFailureListener.onFailure(new Exception("No games found"));
			}
		}, new OnFailureListener() {
			@Override
			public void onFailure(Exception e) {
				// Handle failure
				onFailureListener.onFailure(new Exception("Error fetching games: " + e.getMessage()));
			}
		});
	}
	@Override
	protected Query getQueryForExist(Game entity) {
		return getCollection().whereEqualTo("idFs", entity.getIdFs());
	}

	private Query getQueryForNotStarted() {
		return getCollection().whereEqualTo("started", false);
	}

	public void getNotStartedGames(OnSuccessListener<Games> onSuccessListener, OnFailureListener onFailureListener) {
		Games games = new Games();
		getQueryForNotStarted().get()
				.addOnSuccessListener(queryDocumentSnapshots -> {
					if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
						for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
							games.add(document.toObject(OnlineGame.class));
						}
						onSuccessListener.onSuccess(games);
					} else {
						onFailureListener.onFailure(new Exception("No games found"));
					}
				})
				.addOnFailureListener(e -> {
					onFailureListener.onFailure(e);
				});
	}

	private void getGameIdWithSimilarElo(int userElo, Games games, OnSuccessListener<String> onSuccessListener, OnFailureListener onFailureListener) {
		ArrayList<Integer> eloRange = new ArrayList<Integer>(Arrays.asList(100, 200, 300, 400, 500));
		ListIterator<Integer> iEloRange = eloRange.listIterator();

		final Handler handler = new Handler();
		Runnable task = new Runnable() {
			@Override
			public void run() {
					ListIterator<Game> iGames = games.listIterator();
					while (iGames.hasNext()) {
						if (Math.abs(iGames.next().getPlayer1().getElo() - userElo) <= iEloRange.next()) {
							onSuccessListener.onSuccess(iGames.previous().getIdFs());
							handler.removeCallbacks(this);
							return;
						}
						iEloRange.previous();
					}
					if (!iEloRange.hasNext()) {
						onFailureListener.onFailure(new Exception("No games found within 500 elo range"));
						handler.removeCallbacks(this);
					}
				iEloRange.next();
				handler.postDelayed(this, 3000);
			}
		};
		handler.post(task);
	}

	private Task<String> joinGame(String gameId, String userId, String userName, int userElo) {
		Map<String, String> data = new HashMap<>();
		data.put("game_id", gameId);
		//data.put("user_id", userId);
		//data.put("user_name", userName);
		//data.put("user_elo", String.valueOf(userElo));

		return function
				.getHttpsCallable("join_game")
				.call(data)
				.continueWith(new Continuation<HttpsCallableResult, String>() {
					@Override
					public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
						return (String) task.getResult().getData();
					}
				});
	}

	public void addSnapshotListener(String gameId) {
		final DocumentReference gameRef = getCollection().document(gameId);
		gameRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
			@Override
			public void onEvent(@Nullable DocumentSnapshot snapshot,
								@Nullable FirebaseFirestoreException e) {
				if (e != null) {
					//Log.w(TAG, "Listen failed.", e);
					return;
				}

				boolean isLocal = snapshot != null && snapshot.getMetadata().hasPendingWrites();
				if(!isLocal){
					if (snapshot != null && snapshot.exists()) {
						if(!snapshot.getBoolean("started") && !snapshot.toObject(OnlineGame.class).getPlayer2().getIdFs().isEmpty()) {
							//the second user joined the game
							//only the host get it, the joiner ignores it
							lvGame.setValue(snapshot.toObject(OnlineGame.class));
							startOnlineGame();
						}
						else if((boolean) snapshot.get("started") && snapshot.get("moves") == null) {
							//the Host started the game
							//the joiner get it, the host ignores it
							((OnlineGame)(lvGame.getValue())).startGameForJoiner();
							lvGame.setValue(snapshot.toObject(OnlineGame.class));
						}
						else {
							//it's a move and send it to handle a move
						}
					} else {
						//Log.d(TAG, "Current data: null");
					}
				}
			}
		});
	}

	public Task<Boolean> removeGame(){
		TaskCompletionSource<Boolean> taskRemoveGame = new TaskCompletionSource<>();
		delete(lvGame.getValue()).addOnSuccessListener(new OnSuccessListener<Boolean>() {
			@Override
			public void onSuccess(Boolean aBoolean) {
				lvGame.setValue(null);
				taskRemoveGame.setResult(true);
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
			taskRemoveGame.setResult(false);
			}
		});
		return taskRemoveGame.getTask();
	}
}