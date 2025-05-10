package com.emil_z.repository;

import android.app.Application;
import android.graphics.Point;
import android.util.Log;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.emil_z.helper.PreferenceManager;
import com.emil_z.model.BoardLocation;
import com.emil_z.model.CPU;
import com.emil_z.model.CpuGame;
import com.emil_z.model.Game;
import com.emil_z.model.Games;
import com.emil_z.model.LocalGame;
import com.emil_z.model.OnlineGame;
import com.emil_z.model.Player;
import com.emil_z.model.exceptions.EmptyQueryException;
import com.emil_z.model.exceptions.GameFullException;
import com.emil_z.repository.BASE.BaseRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class GamesRepository extends BaseRepository<Game, Games> {
	private final MutableLiveData<Game> lvGame;
	private final MutableLiveData<char[][]> lvOuterBoardWinners;
	private final MutableLiveData<Boolean> lvIsFinished;
	private final MutableLiveData<Boolean> lvIsStarted;
	private int retryCounter = 0;
	private final String localPlayerIdFs;



	public GamesRepository(Application application) {
		super(Game.class, Games.class, application);
		lvGame = new MutableLiveData<>();
		lvOuterBoardWinners = new MutableLiveData<>();
		lvOuterBoardWinners.setValue(new char[3][3]);
		lvIsFinished = new MutableLiveData<>();
		lvIsStarted = new MutableLiveData<>(false);
		localPlayerIdFs = PreferenceManager.readFromSharedPreferences(application, "user_prefs", new Object[][]{{"UserIdFs", "String"}})[0][1].toString();
	}

	public LiveData<Game> getLvGame() {
		return lvGame;
	}

	public LiveData<char[][]> getLvOuterBoardWinners() {
		return lvOuterBoardWinners;
	}

	public LiveData<Boolean> getLvIsFinished() {
		return lvIsFinished;
	}

	public LiveData<Boolean> getLvIsStarted() {
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

	//region start game
	public void startCpuGame(String crossPlayerIdFs) {
		lvGame.setValue(new CpuGame(localPlayerIdFs, crossPlayerIdFs));
		lvIsStarted.setValue(true);
	}

	public void startLocalGame() {
		lvGame.setValue(new LocalGame(localPlayerIdFs));
		lvIsStarted.setValue(true);
	}

	public Task<Boolean> startOnlineGame(Player player) throws Exception {
		TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
		try {
			Games games = Tasks.await(getNotStartedGames());
			String gameId = getGameIdWithSimilarElo(player.getElo(), games);
			Tasks.await(joinGame(gameId, player));
			//joined successfully because joinGame returns true or throws exception
			OnlineGame game = Tasks.await(getOnlineGame(gameId));
			addSnapshotListener(gameId);
			lvGame.setValue(game);
			tcs.setResult(false); //will inform the ViewModel that he is a joiner
		} catch (NoSuchElementException e) { //game with elo in range not found, create a new game
			Tasks.await(hostOnlineGame(player));
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();

			if (cause instanceof EmptyQueryException) {
				String gameId = Tasks.await(hostOnlineGame(player));
				addSnapshotListener(gameId);
				tcs.setResult(true); //will inform the ViewModel that he is a host
			} else if (cause instanceof GameFullException) {
				int MAX_TRIES = 10;
				if (retryCounter < MAX_TRIES) {
					retryCounter++;
					// Recursive call within the background thread
					Tasks.await(startOnlineGame(player));
				} else {
					Log.d("qqq", "Max retries reached, hosting new game");
				}
			}

			tcs.setException(e); // Rethrow other execution exceptions
		}
		return tcs.getTask();
	}

	public Task<Games> getNotStartedGames() {
		TaskCompletionSource<Games> taskGetNotStartedGames = new TaskCompletionSource<>();
		Games games = new Games();
		getQueryForNotStarted().get()
				.addOnSuccessListener(queryDocumentSnapshots -> {
					if (!queryDocumentSnapshots.isEmpty()) {
						for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
							games.add(document.toObject(OnlineGame.class));
						}
						games.sort((g1, g2) -> g1.getPlayer1().compareElo(g2.getPlayer1()));
						taskGetNotStartedGames.setResult(games);
					} else {
						taskGetNotStartedGames.setException(new EmptyQueryException());
					}
				})
				.addOnFailureListener(taskGetNotStartedGames::setException);
		return taskGetNotStartedGames.getTask();
	}

	private Query getQueryForNotStarted() {
		return getCollection().whereEqualTo("started", false);
	}

	private String getGameIdWithSimilarElo(float userElo, Games games) throws NoSuchElementException {
		final int eloRange = 100;
		ListIterator<Game> iGames = games.listIterator();

		Game closestGame = iGames.next();
		float closestElo = Math.abs(closestGame.getPlayer1().getElo() - userElo);
		while (iGames.hasNext()) {
			Game currentGame = iGames.next();
			float currentElo = Math.abs(currentGame.getPlayer1().getElo() - userElo);
			if (currentElo <= closestElo) {
				closestGame = currentGame;
				closestElo = currentElo;
			} else {
				break;
			}
		}
		if (closestElo <= eloRange) {
			return closestGame.getIdFs();
		} else {
			throw new NoSuchElementException();
		}
	}

	public Task<OnlineGame> getOnlineGame(String idFs) {
		TaskCompletionSource<OnlineGame> taskGame = new TaskCompletionSource<>();

		super.getCollection().document(idFs).get()
				.addOnSuccessListener(documentSnapshot -> {
					OnlineGame game = documentSnapshot.toObject(OnlineGame.class);
					taskGame.setResult(game);
				})
				.addOnFailureListener(e -> {
					Log.e("qqq", "Error getting game: " + e.getMessage());
					taskGame.setResult(null);
				});

		return taskGame.getTask();
	}

	public Task<String> hostOnlineGame(Player player) {

		TaskCompletionSource<String> taskCreateGame = new TaskCompletionSource<>();
		OnlineGame game = new OnlineGame(player);
		add(game)
				.addOnSuccessListener(aBoolean -> {
				lvGame.setValue(game);
				taskCreateGame.setResult(game.getIdFs()); // inform the viewmodel that the game was created and uploaded to the database
				})
				.addOnFailureListener(taskCreateGame::setException);
		return taskCreateGame.getTask();
	}

	public void beginOnlineGame() {
		lvGame.getValue().setStarted(true);
		getCollection().document(lvGame.getValue().getIdFs())
				.update(
						"started", true,
						"startedAt", FieldValue.serverTimestamp()
				).addOnSuccessListener(voidTask -> {
					// Fetch the updated document with the actual server timestamp

					getCollection().document(lvGame.getValue().getIdFs()).get()
							.addOnSuccessListener(documentSnapshot -> {
								if (documentSnapshot.exists()) {
									Timestamp serverTimestamp = documentSnapshot.getTimestamp("startedAt");
									((OnlineGame) lvGame.getValue()).setStartedAt(serverTimestamp);
								}
							});
				});
		lvIsStarted.setValue(true);
	}

	public Task<Boolean> joinGame(String gameId, Player player) {
		Map<String, Object> data = new HashMap<>();
		Gson gson = new Gson();
		String json = gson.toJson(player);
		data.put("game_id", gameId);
		data.put("player", json);

		return function
				.getHttpsCallable("join_game")
				.call(data).continueWith(task -> {
					if (task.isSuccessful()) {
						return true;
					} else if (task.getException() instanceof FirebaseFunctionsException) {
						if (((FirebaseFunctionsException) task.getException()).getCode() == FirebaseFunctionsException.Code.ABORTED)
							throw new GameFullException();
					}
					throw task.getException();
				});
	}
	//endregion

	@SuppressWarnings("unchecked")
	public void addSnapshotListener(String gameId) {
		final DocumentReference gameRef = getCollection().document(gameId);
		gameRef.addSnapshotListener((snapshot, e) -> {
			if (e != null) {
				return;
			}

			boolean isLocal = snapshot != null && snapshot.getMetadata().hasPendingWrites();
			if (!isLocal) {
				if (snapshot != null && snapshot.exists()) {
					if (!snapshot.getBoolean("started") && !snapshot.toObject(OnlineGame.class).getPlayer2().getIdFs().isEmpty() && Objects.equals(snapshot.toObject(OnlineGame.class).getPlayer1().getIdFs(), localPlayerIdFs)) {
						//the joiner joined the game
						lvGame.setValue(snapshot.toObject(OnlineGame.class));
						beginOnlineGame();
					} else if ((Boolean) snapshot.get("started") && lvGame.getValue() == null && ((List<BoardLocation>) snapshot.get("moves")).isEmpty()) {
						//the Host started the game. the joiner get it, the host ignores it
						lvGame.setValue(snapshot.toObject(OnlineGame.class));
						((OnlineGame) (lvGame.getValue())).startGameForJoiner();
						lvIsStarted.setValue(true);
					} else if (snapshot.getBoolean("finished") && getLastMoveAsBoardLocation(snapshot.get("moves")).equals(lvGame.getValue().getMoves().get(lvGame.getValue().getMoves().size() - 1))) {
						//the opponent resigns
						lvGame.setValue(snapshot.toObject(OnlineGame.class));
						lvIsFinished.setValue(true);
					} else if (!((List<BoardLocation>) snapshot.get("moves")).isEmpty()) {
						//it's a move and send it to handle a move
						BoardLocation lastMove = getLastMoveAsBoardLocation(snapshot.get("moves"));
						lvGame.getValue().makeMove(lastMove);
						lvGame.setValue(lvGame.getValue());
						CheckInnerBoardFinish(lastMove.getOuter());
						Log.d("qqq", "move happened");
					} else if(((OnlineGame) lvGame.getValue()).getStartedAt() == null){
						Timestamp serverTimestamp = snapshot.getTimestamp("startedAt");
						((OnlineGame) lvGame.getValue()).setStartedAt(serverTimestamp);
					}
				} else {
					// The document does not exist (deleted)
					lvGame.setValue(null);
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	private BoardLocation getLastMoveAsBoardLocation(Object firestoreMoves) {
		if (firestoreMoves instanceof List<?>) {
			List<?> movesList = (List<?>) firestoreMoves;

			if (!movesList.isEmpty()) {
				Object lastMove = movesList.get(movesList.size() - 1);

				if (lastMove instanceof Map) {
					Map<String, Object> moveMap = (Map<String, Object>) lastMove;

					// Get outer coordinates
					Map<String, Object> outerMap = (Map<String, Object>) moveMap.get("outer");
					int outerX = ((Number) outerMap.get("x")).intValue();
					int outerY = ((Number) outerMap.get("y")).intValue();

					// Get inner coordinates
					Map<String, Object> innerMap = (Map<String, Object>) moveMap.get("inner");
					int innerX = ((Number) innerMap.get("x")).intValue();
					int innerY = ((Number) innerMap.get("y")).intValue();

					return new BoardLocation(outerX, outerY, innerX, innerY);
				}
			}
		}
		return null;
	}


	public Task<Boolean> makeMove(BoardLocation location) {
		TaskCompletionSource<Boolean> taskMakeMove = new TaskCompletionSource<>();
		if(Objects.equals(lvGame.getValue().getCurrentPlayerIdFs(), localPlayerIdFs)) {
			if(lvGame.getValue().isLegal(location)) {
				lvGame.getValue().makeMove(location);
				lvGame.setValue(lvGame.getValue());
				CheckInnerBoardFinish(location.getOuter());
				taskMakeMove.setResult(true);
			}
			else taskMakeMove.setException(new Exception("2"));
		}
		else taskMakeMove.setException(new Exception("1"));
		return taskMakeMove.getTask();
	}

	public Task<Boolean> makeCpuMove() {
		TaskCompletionSource <Boolean> tcs = new TaskCompletionSource<>();
		BoardLocation location = CPU.findBestMove(lvGame.getValue().getOuterBoard());
		lvGame.getValue().makeMove(location);
		lvGame.setValue(lvGame.getValue());
		CheckInnerBoardFinish(location.getOuter());
		tcs.setResult(true);
		return tcs.getTask();
	}

	private void CheckInnerBoardFinish(Point innerBoard) {
		if(lvGame.getValue().getOuterBoard().getBoard(innerBoard).isFinished()){			//check if the inner board is finished
			if(lvGame.getValue().getOuterBoard().getBoard(innerBoard).getWinner() != 0){	//check if the inner board isn't a tie
				char[][] tmp = new char[3][3];
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						tmp[i][j] = lvOuterBoardWinners.getValue()[i][j];
					}
				}
				tmp[innerBoard.x][innerBoard.y] = lvGame.getValue().getOuterBoard().getBoard(innerBoard).getWinner();
				lvOuterBoardWinners.setValue(tmp);
			}
			else {
				lvOuterBoardWinners.getValue()[innerBoard.x][innerBoard.y] = 'T';
			}
			if(lvGame.getValue().getOuterBoard().isGameOver()){
				lvGame.getValue().setFinished(true);
				if(lvGame.getValue() instanceof OnlineGame) {
					if (Objects.equals(lvGame.getValue().getPlayer1().getIdFs(), localPlayerIdFs)) {
						finishGame(lvGame.getValue().getPlayer1().getIdFs(), lvGame.getValue().getPlayer2().getIdFs())
								.addOnSuccessListener(aBoolean -> {
									lvIsFinished.setValue(true);
									Log.d("qqq", "game finished");
								})
								.addOnFailureListener(e -> Log.d("qqq", "game not finished"));
					} else {
						lvIsFinished.setValue(true);
					}
				}
				else {
					lvIsFinished.setValue(true);
				}
			}

		}
	}

	private Task<Boolean> finishGame(String player1IdFs, String player2IdFs) {
		Map<String, Object> data = new HashMap<>();
		data.put("player_1_id", player1IdFs);
		data.put("player_2_id", player2IdFs);
		data.put("score", Objects.equals(lvGame.getValue().getWinnerIdFs(), "T") ? 0.5 :
				(lvGame.getValue().getWinnerIdFs().equals(player1IdFs) ? 1.0 : 0.0));
		return function
				.getHttpsCallable("finish_game")
				.call(data).continueWith(task -> {
					if (task.isSuccessful()) {
						return true;
					} else if (task.getException() instanceof FirebaseFunctionsException) {
						if (((FirebaseFunctionsException) task.getException()).getCode() == FirebaseFunctionsException.Code.ABORTED)
							throw new FirebaseTooManyRequestsException("");
					}
					throw task.getException();
				});
	}

	public Task<Boolean> deleteOnlineGame(){
		TaskCompletionSource<Boolean> taskAbortGame = new TaskCompletionSource<>();

		if(lvGame.getValue() == null) {
			taskAbortGame.setResult(true);
		} else if (lvGame.getValue().getMoves().isEmpty()) { // Game not started
			delete(lvGame.getValue())
					.addOnSuccessListener(aBoolean -> {
						lvGame.setValue(null);
						taskAbortGame.setResult(true);
					})
					.addOnFailureListener(e -> taskAbortGame.setResult(false));
		} else { //Game in progress
			lvGame.getValue().setFinished(true);
			lvGame.getValue().setWinnerIdFs(
					Objects.equals(lvGame.getValue().getPlayer1().getIdFs(), localPlayerIdFs) ?
							lvGame.getValue().getPlayer2().getIdFs():
							lvGame.getValue().getPlayer1().getIdFs());
			update(lvGame.getValue());
			finishGame(lvGame.getValue().getPlayer1().getIdFs(), lvGame.getValue().getPlayer2().getIdFs())
					.addOnSuccessListener(aBoolean -> lvIsFinished.setValue(true))
					.addOnFailureListener(e -> {
					});
			taskAbortGame.setResult(true);
		}
		return taskAbortGame.getTask();
	}

}