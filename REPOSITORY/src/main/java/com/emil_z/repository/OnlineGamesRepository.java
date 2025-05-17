package com.emil_z.repository;

import android.app.Application;
import android.graphics.Point;

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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Repository for managing online games, including creation, joining, pagination, and state updates.
 * Handles Firestore and Cloud Functions interactions for online multiplayer games.
 */
public class OnlineGamesRepository extends BaseGamesRepository {
	private final MutableLiveData<String> lvGameIdFs;
	private final MutableLiveData<Games> lvGames;

	private int retryCounter = 0;

	/**
	 * Constructs an OnlineGamesRepository.
	 * @param application Application context.
	 */
	public OnlineGamesRepository(Application application) {
		super(application);

		lvGameIdFs = new MutableLiveData<>();
		lvGames = new MutableLiveData<>();
	}

	/**
	 * Returns LiveData for the current game idFs.
	 * @return LiveData of game idFs.
	 */
	public LiveData<String> getLiveDataGameIdFs() {
		return lvGameIdFs;
	}

	/**
	 * Retrieves a paginated list of games for a user.
	 * @param userIdFs User idFs.
	 * @param limit Maximum number of games to return.
	 * @param startAfterIdFs Game idFs to start after (for pagination), or null for first page.
	 * @return LiveData containing the paginated games.
	 */
	public LiveData<Games> getUserGamesPaginated(String userIdFs, int limit, String startAfterIdFs) {
		if (startAfterIdFs != null) {
			getCollection().document(startAfterIdFs).get()
					.addOnSuccessListener(documentSnapshot -> {
						if (documentSnapshot.exists()) {
							Timestamp timestamp = documentSnapshot.getTimestamp("startedAt");
							if (timestamp != null)
								executeQueriesWithTimestamp(userIdFs, limit, timestamp, lvGames);
							else
								lvGames.setValue(new Games());
						} else
							lvGames.setValue(new Games());
					})
					.addOnFailureListener(e -> lvGames.setValue(new Games()));
		} else {
			executeInitialQueries(userIdFs, limit, lvGames);
		}

		return lvGames;
	}

	/**
	 * Executes the initial paginated query for user games.
	 * @param userId User idFs.
	 * @param limit Max number of games.
	 * @param result LiveData to update with results.
	 */
	private void executeInitialQueries(String userId, int limit, MutableLiveData<Games> result) {
		executeQueriesWithTimestamp(userId, limit, new Timestamp(253402300799L, 999999999), result); //TODO: use a better timestamp?
	}

	/**
	 * Executes paginated queries for games with a timestamp cursor.
	 * @param userId User idFs.
	 * @param limit Max number of games.
	 * @param startAfter Timestamp to start after.
	 * @param result LiveData to update with results.
	 */
	private void executeQueriesWithTimestamp(String userId, int limit, Timestamp startAfter, MutableLiveData<Games> result) {
		Games allGames = new Games();

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

						allGames.sort((g1, g2) -> ((OnlineGame) g2).getStartedAt().compareTo(((OnlineGame) g1).getStartedAt()));
						Games limitedGames = new Games();
						for (int i = 0; i < Math.min(limit, allGames.size()); i++) {
							limitedGames.add(allGames.get(i));
						}
						result.setValue(limitedGames);
					})
					.addOnFailureListener(e -> {
						allGames.sort((g1, g2) -> ((OnlineGame) g2).getStartedAt().compareTo(((OnlineGame) g1).getStartedAt()));
						result.setValue(allGames);
					});
			})
			.addOnFailureListener(e -> result.setValue(new Games()));
	}
	/**
	 * Starts a new online game or joins an existing one with similar ELO.
	 * Handles retries and error cases.
	 * @param player The player starting or joining the game.
	 * @param crossPlayerIdFs Not used for online games.
	 */
	@Override
	public void startGame(Player player, String crossPlayerIdFs) {
		try {
			Games games = Tasks.await(getNotStartedGames());
			String gameId = getGameIdWithSimilarElo(player.getElo(), games);
			Tasks.await(joinGame(gameId, player));
			addSnapshotListener(gameId);
		} catch (ExecutionException | NoSuchElementException e) {
			Throwable cause = e.getCause();
			if (cause instanceof EmptyQueryException || e instanceof NoSuchElementException) {
				hostOnlineGame(player).continueWith(gameId -> {
					addSnapshotListener(gameId.getResult());
					return null;
				});
			} else if (cause instanceof GameFullException && retryCounter < 10) {
					retryCounter++;
					startGame(player, null);
			}
		} catch (InterruptedException e) {
			lvErrorCode.setValue(4);
		}
	}

	/**
	 * Gets a Task for all not-started games (waiting for a second player).
	 * @return Task with Games list.
	 */
	private Task<Games> getNotStartedGames() {
		TaskCompletionSource<Games> taskGetNotStartedGames = new TaskCompletionSource<>();
		Games games = new Games();

		getQueryForNotStarted().get()
				.addOnSuccessListener(queryDocumentSnapshots -> {
					for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
						OnlineGame game = document.toObject(OnlineGame.class);
						if(Objects.equals(game.getPlayer1().getIdFs(), localPlayerIdFs))
							delete(game);
						else
							games.add(game);
					}
					games.sort((g1, g2) -> g1.getPlayer1().compareElo(g2.getPlayer1()));
					if (!games.isEmpty())
						taskGetNotStartedGames.setResult(games);
					else
						taskGetNotStartedGames.setException(new EmptyQueryException());
				})
				.addOnFailureListener(taskGetNotStartedGames::setException);
		return taskGetNotStartedGames.getTask();
	}

	/**
	 * Returns a Firestore query for not-started games (waiting for joiner).
	 * @return Firestore Query.
	 */
	private Query getQueryForNotStarted() {
		return getCollection().whereEqualTo("started", false).whereEqualTo("player2.idFs", "");
	}

	/**
	 * Finds a game with similar ELO to the user from a list of games.
	 * @param userElo The user's ELO.
	 * @param games List of available games.
	 * @return idFs of the closest game.
	 * @throws NoSuchElementException if no suitable game is found.
	 */
	private String getGameIdWithSimilarElo(float userElo, Games games) throws NoSuchElementException {
		final int eloRange = 100;
		Game closestGame = games.get(0);
		float closestElo = Math.abs(closestGame.getPlayer1().getElo() - userElo);

		for (int i = 1; i < games.size(); i++) {
			Game currentGame = games.get(i);
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
		}
		throw new NoSuchElementException();
	}

	/**
	 * Hosts a new online game for the given player.
	 * @param player The player hosting the game.
	 * @return Task with the new game's Firestore ID.
	 */
	public Task<String> hostOnlineGame(Player player) {
		TaskCompletionSource<String> taskCreateGame = new TaskCompletionSource<>();
		OnlineGame game = new OnlineGame(player);
		add(game)
			.addOnSuccessListener(aBoolean -> {
				lvGame.setValue(game);
				taskCreateGame.setResult(game.getIdFs());
				lvGameIdFs.setValue(game.getIdFs());
			})
			.addOnFailureListener(taskCreateGame::setException);
		return taskCreateGame.getTask();
	}

	/**
	 * Begins an online game by marking it as started and setting the server timestamp.
	 * Updates LiveData accordingly.
	 */
	@SuppressWarnings("ConstantConditions")
	public void beginOnlineGame() {
		OnlineGame game = (OnlineGame) lvGame.getValue();
		game.setStarted(true);
		getCollection().document(lvGame.getValue().getIdFs())
			.update("started", true,"startedAt", FieldValue.serverTimestamp())
			.addOnSuccessListener(voidTask -> getCollection().document(lvGame.getValue().getIdFs()).get()
				.addOnSuccessListener(documentSnapshot -> {
					if (documentSnapshot.exists()) {
						Timestamp serverTimestamp = documentSnapshot.getTimestamp("startedAt");
						((OnlineGame) lvGame.getValue()).setStartedAt(serverTimestamp);
					}
				}));
		lvIsStarted.setValue(true);
	}

	/**
	 * Joins an existing game using a Cloud Function.
	 * @param gameId Game idFs.
	 * @param player Player joining the game.
	 * @return Task for the join operation.
	 */
	@SuppressWarnings("ConstantConditions")
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

	/**
	 * Adds a Firestore snapshot listener to the game document to observe real-time updates.
	 * Handles game state changes and updates LiveData accordingly.
	 * @param gameIdFs game's /idFs.
	 */
	@SuppressWarnings("ConstantConditions")
	public void addSnapshotListener(String gameIdFs) {
		final DocumentReference gameRef = getCollection().document(gameIdFs);
		gameRef.addSnapshotListener((snapshot, e) -> {
			if (e != null || snapshot == null) return;

			boolean isLocal = snapshot.getMetadata().hasPendingWrites();
			if (isLocal || !snapshot.exists()) {
				if (!isLocal) {
					lvGame.setValue(null); //The document does not exist (deleted)
				}
				return;
			}

			OnlineGame game = snapshot.toObject(OnlineGame.class);
			boolean isStarted = game.isStarted();
			boolean isFinished = game.isFinished();
			List<BoardLocation> moves = game.getMoves();

			try {
				if (!isStarted && !game.getPlayer2().getIdFs().isEmpty() &&
					Objects.equals(game.getPlayer1().getIdFs(), localPlayerIdFs)) {
					//the joiner joined the game
					lvGame.setValue(game);
					beginOnlineGame();
				} else if (isStarted && lvGame.getValue() == null && (moves.isEmpty())) {
					//the Host started the game
					lvGame.setValue(game);
					((OnlineGame) (lvGame.getValue())).startGameForJoiner();
					lvIsStarted.setValue(true);
				} else if (isFinished && moves.size() == lvGame.getValue().getMoves().size()) {
					//the opponent resigns
					lvGame.setValue(game);
					lvIsFinished.setValue(true);
				} else if (moves != null && !moves.isEmpty()) {
					//it's a move and send it to handle a move
					BoardLocation lastMove = moves.get(moves.size() - 1);
					lvGame.getValue().makeMove(lastMove);
					lvGame.setValue(lvGame.getValue());
					this.checkInnerBoardFinish(lastMove.getOuter());
				} else if (((OnlineGame) lvGame.getValue()).getStartedAt() == null) {
					Timestamp serverTimestamp = snapshot.getTimestamp("startedAt");
					((OnlineGame) lvGame.getValue()).setStartedAt(serverTimestamp);
				}
			} catch (Exception ex) {
				//intentionally empty
			}
		});
	}

	/**
	 * Finishes a game by calling a Cloud Function to update scores and state.
	 * @param player1IdFs IdFs of player 1.
	 * @param player2IdFs IdFs of player 2.
	 * @return Task for the finish operation.
	 */
	@SuppressWarnings("ConstantConditions")
	private Task<Void> finishGame(String player1IdFs, String player2IdFs) {
		Map<String, Object> data = new HashMap<>();
		data.put("player_1_id", player1IdFs);
		data.put("player_2_id", player2IdFs);
		data.put("score", Objects.equals(lvGame.getValue().getWinnerIdFs(), "T") ? 0.5 :
				(lvGame.getValue().getWinnerIdFs().equals(player1IdFs) ? 1.0 : 0.0));
		return function
			.getHttpsCallable("finish_game")
			.call(data).continueWith(task -> {
				Exception ex = task.getException();
				if (ex instanceof FirebaseFunctionsException &&
					((FirebaseFunctionsException) ex).getCode() == FirebaseFunctionsException.Code.ABORTED) {
						throw new FirebaseTooManyRequestsException("");
				}
				if (ex != null)
					throw ex;
				return null;
			});
	}

	/**
	 * Checks if the inner board is finished and updates the finished state.
	 * If the game is finished, calls finishGame for the host.
	 * @param innerBoard Inner board coordinates.
	 */
	@SuppressWarnings("ConstantConditions")
	@Override
	protected void checkInnerBoardFinish(Point innerBoard) {
		super.checkInnerBoardFinish(innerBoard);
		OnlineGame game = (OnlineGame) lvGame.getValue();
		if (game.isFinished()) {
			if (Objects.equals(lvGame.getValue().getPlayer1().getIdFs(), localPlayerIdFs)) {
				finishGame(game.getPlayer1().getIdFs(), game.getPlayer2().getIdFs())
					.addOnSuccessListener(aBoolean -> lvIsFinished.setValue(true))
					.addOnFailureListener(e -> {});
			} else {
				lvIsFinished.setValue(true);
			}
		}
	}

	/**
	 * Exits the current game, handling unfinished and finished states.
	 * Deletes the game if not started, or marks as finished if in progress.
	 * @return Task indicating if the exit was successful.
	 */
	public Task<Boolean> exitGame() {
		TaskCompletionSource<Boolean> taskAbortGame = new TaskCompletionSource<>();
		Game game = lvGame.getValue();
		if (game == null || game.getWinnerIdFs() != null) {
			taskAbortGame.setResult(true);
		} else if (game.getMoves().size() < 2) {
			delete(game)
					.addOnSuccessListener(aBoolean -> {
						lvGame.setValue(null);
						taskAbortGame.setResult(true);
					})
					.addOnFailureListener(e -> taskAbortGame.setResult(false));
		} else {
			game.setFinished(true);
			game.setWinnerIdFs(
					Objects.equals(game.getPlayer1().getIdFs(), localPlayerIdFs) ?
							game.getPlayer2().getIdFs() :
							game.getPlayer1().getIdFs());
			update(game);
			finishGame(game.getPlayer1().getIdFs(), game.getPlayer2().getIdFs())
					.addOnSuccessListener(aBoolean -> lvIsFinished.setValue(true))
					.addOnFailureListener(e -> {
					});
			taskAbortGame.setResult(true);
		}
		return taskAbortGame.getTask();
	}

	/**
	 * Exits a game directly by game ID, handling both deletion and marking as finished.
	 * @param gameId game's idFs.
	 * @param player1IdFs IdFs of player 1.
	 * @param player2IdFs IdFs of player 2.
	 * @param isPlayer1 True if the exiting player is player 1.
	 * @return Task indicating if the exit was successful.
	 */
	public Task<Boolean> exitGameDirect(String gameId, String player1IdFs, String player2IdFs, boolean isPlayer1) {
		TaskCompletionSource<Boolean> taskAbortGame = new TaskCompletionSource<>();

		if (gameId == null) {
			taskAbortGame.setResult(true);
			return taskAbortGame.getTask();
		}

		getCollection().document(gameId).get()
			.addOnSuccessListener(documentSnapshot -> {
				if (documentSnapshot.exists()) {
					OnlineGame game = documentSnapshot.toObject(OnlineGame.class);

					if (game != null) {
						if (game.getMoves().isEmpty()) {
							getCollection().document(gameId).delete()
								.addOnSuccessListener(aVoid -> taskAbortGame.setResult(true))
								.addOnFailureListener(e -> taskAbortGame.setResult(false));
						} else {
							String winnerId = isPlayer1 ? player2IdFs : player1IdFs;
							Map<String, Object> updates = new HashMap<>();
							updates.put("finished", true);
							updates.put("winnerIdFs", winnerId);

							getCollection().document(gameId).update(updates)
								.addOnSuccessListener(aVoid -> {
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