package com.emil_z.repository;

import android.app.Application;
import android.graphics.Point;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.emil_z.model.BoardLocation;
import com.emil_z.model.Game;
import com.emil_z.model.Games;
import com.emil_z.model.OnlineGame;
import com.emil_z.model.Player;
import com.emil_z.model.exceptions.EmptyQueryException;
import com.emil_z.model.exceptions.GameFullException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class OnlineGamesRepository extends BaseGamesRepository {
	private final MutableLiveData<String> lvGameIdFs;
	private final MutableLiveData<Games> lvGames = new MutableLiveData<>();

	public OnlineGamesRepository(Application application) {
		super(application);
		lvGameIdFs = new MutableLiveData<>();
	}

	public LiveData<String> getLiveDataGameIdFs() {
		return lvGameIdFs;
	}

	public LiveData<Games> getUserGamesPaginated(String userId, int limit, String startAfterId) {
		// Add pagination if needed
		if (startAfterId != null) {
			getCollection().document(startAfterId).get()
					.addOnSuccessListener(documentSnapshot -> {
						if (documentSnapshot.exists()) {
							Timestamp timestamp = documentSnapshot.getTimestamp("startedAt");
							if (timestamp != null) {
								executeQueriesWithTimestamp(userId, limit, timestamp, lvGames);
							} else {
								lvGames.setValue(new Games());
							}
						} else {
							lvGames.setValue(new Games());
						}
					})
					.addOnFailureListener(e -> lvGames.setValue(new Games()));
		} else {
			// First page - no pagination cursor
			executeInitialQueries(userId, limit, lvGames);
		}

		return lvGames;
	}

	private void executeInitialQueries(String userId, int limit, MutableLiveData<Games> result) {
		Games allGames = new Games();

		// Query both player1 and player2 games
		getCollection()
				.whereEqualTo("player1.idFs", userId)
				.orderBy("startedAt", Query.Direction.DESCENDING)
				.limit(limit)
				.get()
				.addOnSuccessListener(player1Snapshot -> {
					for (DocumentSnapshot doc : player1Snapshot.getDocuments()) {
						Game game = doc.toObject(OnlineGame.class);
						if (game != null) {
							game.setIdFs(doc.getId());
							allGames.add(game);
						}
					}

					// Now query player2 games
					getCollection()
							.whereEqualTo("player2.idFs", userId)
							.orderBy("startedAt", Query.Direction.DESCENDING)
							.limit(limit)
							.get()
							.addOnSuccessListener(player2Snapshot -> {
								for (DocumentSnapshot doc : player2Snapshot.getDocuments()) {
									Game game = doc.toObject(OnlineGame.class);
									if (game != null) {
										game.setIdFs(doc.getId());
										allGames.add(game);
									}
								}

								// Sort all games by timestamp
								allGames.sort((g1, g2) -> ((OnlineGame) g2).getStartedAt().compareTo(((OnlineGame) g1).getStartedAt()));

								// Take only the top "limit" games
								Games limitedGames = new Games();
								for (int i = 0; i < Math.min(limit, allGames.size()); i++) {
									limitedGames.add(allGames.get(i));
								}

								result.setValue(limitedGames);
							})
							.addOnFailureListener(e -> {
								// Still return player1 games if player2 query fails
								allGames.sort((g1, g2) -> ((OnlineGame) g2).getStartedAt().compareTo(((OnlineGame) g1).getStartedAt()));
								result.setValue(allGames);
							});
				})
				.addOnFailureListener(e -> result.setValue(new Games()));
	}

	private void executeQueriesWithTimestamp(String userId, int limit, Timestamp startAfter, MutableLiveData<Games> result) {
		Games allGames = new Games();

		// Query player1 games with pagination
		getCollection()
				.whereEqualTo("player1.idFs", userId)
				.orderBy("startedAt", Query.Direction.DESCENDING)
				.startAfter(startAfter)
				.limit(limit)
				.get()
				.addOnSuccessListener(player1Snapshot -> {
					for (DocumentSnapshot doc : player1Snapshot.getDocuments()) {
						Game game = doc.toObject(OnlineGame.class);
						if (game != null) {
							game.setIdFs(doc.getId());
							allGames.add(game);
						}
					}

					// Now query player2 games with pagination
					getCollection()
							.whereEqualTo("player2.idFs", userId)
							.orderBy("startedAt", Query.Direction.DESCENDING)
							.startAfter(startAfter)
							.limit(limit)
							.get()
							.addOnSuccessListener(player2Snapshot -> {
								for (DocumentSnapshot doc : player2Snapshot.getDocuments()) {
									Game game = doc.toObject(OnlineGame.class);
									if (game != null) {
										game.setIdFs(doc.getId());
										allGames.add(game);
									}
								}

								// Sort all games by timestamp
								allGames.sort((g1, g2) -> ((OnlineGame) g2).getStartedAt().compareTo(((OnlineGame) g1).getStartedAt()));

								// Take only the top "limit" games
								Games limitedGames = new Games();
								for (int i = 0; i < Math.min(limit, allGames.size()); i++) {
									limitedGames.add(allGames.get(i));
								}

								result.setValue(limitedGames);
							})
							.addOnFailureListener(e -> {
								// If player2 query fails, still return player1 games
								allGames.sort((g1, g2) -> ((OnlineGame) g2).getStartedAt().compareTo(((OnlineGame) g1).getStartedAt()));
								result.setValue(allGames);
							});
				})
				.addOnFailureListener(e -> result.setValue(new Games()));
	}

	@Override
	public void startGame(Player player, String crossPlayerIdFs) {
		try {
			Games games = Tasks.await(getNotStartedGames());
			String gameId = getGameIdWithSimilarElo(player.getElo(), games);
			Tasks.await(joinGame(gameId, player));
			//joined successfully because joinGame returns true or throws exception

			addSnapshotListener(gameId);
		} catch (ExecutionException | NoSuchElementException e) {
			Throwable cause = e.getCause();

			if (cause instanceof EmptyQueryException || e instanceof NoSuchElementException) {
				hostOnlineGame(player).continueWith(gameId -> {
					addSnapshotListener(gameId.getResult());
					return null;
				});
			} else if (cause instanceof GameFullException) {
				int MAX_TRIES = 10;
				if (retryCounter < MAX_TRIES) {
					retryCounter++;
					// Recursive call within the background thread
					startGame(player, null);
				}
			}
		} catch (InterruptedException e) {
			// TODO: handle the exception
		}
	}

	private Task<Games> getNotStartedGames() {
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

	public Task<String> hostOnlineGame(Player player) {
		TaskCompletionSource<String> taskCreateGame = new TaskCompletionSource<>();
		OnlineGame game = new OnlineGame(player);
		add(game)
				.addOnSuccessListener(aBoolean -> {
					lvGame.setValue(game);
					taskCreateGame.setResult(game.getIdFs()); // inform the viewmodel that the game was created and uploaded to the database
					lvGameIdFs.setValue(game.getIdFs());
				})
				.addOnFailureListener(taskCreateGame::setException);
		return taskCreateGame.getTask();
	}

	@SuppressWarnings("ConstantConditions")
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

	public Task<Void> joinGame(String gameId, Player player) {
		Map<String, Object> data = new HashMap<>();
		Gson gson = new Gson();
		String json = gson.toJson(player);
		data.put("game_id", gameId);
		data.put("player", json);

		return function
				.getHttpsCallable("join_game")
				.call(data).continueWith(task -> {
					if (task.getException() instanceof FirebaseFunctionsException) {
						if (((FirebaseFunctionsException) task.getException()).getCode() == FirebaseFunctionsException.Code.ABORTED)
							throw new GameFullException();
					} else if (!task.isSuccessful()) {
						throw task.getException();
					}
					return null;
				});
	}

	@SuppressWarnings("unchecked")
	public void addSnapshotListener(String gameId) {
		final DocumentReference gameRef = getCollection().document(gameId);
		gameRef.addSnapshotListener((snapshot, e) -> {
			if (e != null) {
				return;
			}

			boolean isLocal = snapshot != null && snapshot.getMetadata().hasPendingWrites();
			try {
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
							this.checkInnerBoardFinish(lastMove.getOuter());
							Log.d("qqq", "move happened");
						} else if (((OnlineGame) lvGame.getValue()).getStartedAt() == null) {
							Timestamp serverTimestamp = snapshot.getTimestamp("startedAt");
							((OnlineGame) lvGame.getValue()).setStartedAt(serverTimestamp);
						}
					} else {
						// The document does not exist (deleted)
						lvGame.setValue(null);
					}
				}
			} catch (Exception ex) {
			} //TODO: for some reason the first time the listeners catches as joiner, the started is false
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

	private Task<Void> finishGame(String player1IdFs, String player2IdFs) {
		Map<String, Object> data = new HashMap<>();
		data.put("player_1_id", player1IdFs);
		data.put("player_2_id", player2IdFs);
		data.put("score", Objects.equals(lvGame.getValue().getWinnerIdFs(), "T") ? 0.5 :
				(lvGame.getValue().getWinnerIdFs().equals(player1IdFs) ? 1.0 : 0.0));
		return function
				.getHttpsCallable("finish_game")
				.call(data).continueWith(task -> {
					if (task.getException() instanceof FirebaseFunctionsException) {
						if (((FirebaseFunctionsException) task.getException()).getCode() == FirebaseFunctionsException.Code.ABORTED)
							throw new FirebaseTooManyRequestsException("");
					} else if (task.getException() != null)
						throw task.getException();
					return null;
				});
	}

	@Override
	protected void checkInnerBoardFinish(Point innerBoard) {
		super.checkInnerBoardFinish(innerBoard);
		if (lvGame.getValue().isFinished()) {
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
	}

	public Task<Boolean> exitGame() {
		TaskCompletionSource<Boolean> taskAbortGame = new TaskCompletionSource<>();

		if (lvGame.getValue() == null) {
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
							lvGame.getValue().getPlayer2().getIdFs() :
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

	public Task<Boolean> exitGameDirect(String gameId, String player1IdFs, String player2IdFs, boolean isPlayer1) {
		TaskCompletionSource<Boolean> taskAbortGame = new TaskCompletionSource<>();

		if (gameId == null) {
			taskAbortGame.setResult(true);
			return taskAbortGame.getTask();
		}

		// Get the current game state from Firestore
		getCollection().document(gameId).get()
				.addOnSuccessListener(documentSnapshot -> {
					if (documentSnapshot.exists()) {
						OnlineGame game = documentSnapshot.toObject(OnlineGame.class);

						if (game != null) {
							if (game.getMoves().isEmpty()) {
								// Game not started, just delete it
								getCollection().document(gameId).delete()
										.addOnSuccessListener(aVoid -> taskAbortGame.setResult(true))
										.addOnFailureListener(e -> taskAbortGame.setResult(false));
							} else {
								// Game in progress, mark as finished and set winner
								String winnerId = isPlayer1 ? player2IdFs : player1IdFs;
								Map<String, Object> updates = new HashMap<>();
								updates.put("finished", true);
								updates.put("winnerIdFs", winnerId);

								getCollection().document(gameId).update(updates)
										.addOnSuccessListener(aVoid -> {
											// Call finish_game cloud function
											Map<String, Object> data = new HashMap<>();
											data.put("player_1_id", player1IdFs);
											data.put("player_2_id", player2IdFs);
											data.put("score", isPlayer1 ? 0.0 : 1.0); // Player who quit loses

											function.getHttpsCallable("finish_game")
													.call(data)
													.addOnSuccessListener(result -> taskAbortGame.setResult(true))
													.addOnFailureListener(e -> taskAbortGame.setResult(false));
										})
										.addOnFailureListener(e -> taskAbortGame.setResult(false));
							}
						} else {
							taskAbortGame.setResult(true);
						}
					} else {
						taskAbortGame.setResult(true);
					}
				})
				.addOnFailureListener(e -> taskAbortGame.setResult(false));

		return taskAbortGame.getTask();
	}
}