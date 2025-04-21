package com.emil_z.repository;

import android.app.Application;
import android.graphics.Point;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.emil_z.helper.PreferenceManager;
import com.emil_z.model.BoardLocation;
import com.emil_z.model.Game;
import com.emil_z.model.Games;
import com.emil_z.model.LocalGame;
import com.emil_z.model.OnlineGame;
import com.emil_z.model.Player;
import com.emil_z.model.exceptions.EmptyQueryException;
import com.emil_z.model.exceptions.GameFullException;
import com.emil_z.repository.BASE.BaseRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.firestore.DocumentReference;
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
	private MutableLiveData<Game> lvGame;
	private MutableLiveData<char[][]> lvOuterBoardWinners;
	private MutableLiveData<Boolean> lvIsFinished;
	private MutableLiveData<Boolean> lvIsStarted;
	private int retryCounter = 0;
	private final int MAX_TRIES = 10;
	private String localPlayerIdFs;



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

	//region start game
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
			//lvGame.setValue(game);
			addSnapshotListener(gameId);//will inform the user when the host started the game
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
				if (retryCounter < MAX_TRIES) {
					retryCounter++;
					// Recursive call within the background thread
					Tasks.await(startOnlineGame(player));
				} else {
					Log.d("GameViewModel", "Max retries reached, hosting new game");
					//return Tasks.await(hostOnlineGame(player));
				}
			}

			throw e; // Rethrow other execution exceptions
		}
		return tcs.getTask();
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
		add(game).addOnSuccessListener(new OnSuccessListener<Boolean>() {
			@Override
			public void onSuccess(Boolean aBoolean) {
					lvGame.setValue(game);
					taskCreateGame.setResult(game.getIdFs()); // inform the viewmodel that the game was created and uploaded to the database
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				taskCreateGame.setException(e);
			}
		});
		return taskCreateGame.getTask();
	}

	public void startOnlineGame() {
		lvGame.getValue().setStarted(true);
		update(lvGame.getValue());
		lvIsStarted.setValue(true);
	}

	@Override
	protected Query getQueryForExist(Game entity) {
		return getCollection().whereEqualTo("idFs", entity.getIdFs());
	}

	private Query getQueryForNotStarted() {
		return getCollection().whereEqualTo("started", false);
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

	public void addSnapshotListener(String gameId) {
		final DocumentReference gameRef = getCollection().document(gameId);
		gameRef.addSnapshotListener((snapshot, e) -> {
			if (e != null) {
				return;
			}

			boolean isLocal = snapshot != null && snapshot.getMetadata().hasPendingWrites();
			if(!isLocal){
				if (snapshot != null && snapshot.exists()) {
					if(!snapshot.getBoolean("started") && !snapshot.toObject(OnlineGame.class).getPlayer2().getIdFs().isEmpty() && Objects.equals(Objects.requireNonNull(lvGame.getValue()).getPlayer1().getIdFs(), localPlayerIdFs)) {
						//the joiner joined the game
						lvGame.setValue(snapshot.toObject(OnlineGame.class));
						startOnlineGame();
					} else if((Boolean) snapshot.get("started") && ((List<BoardLocation>) snapshot.get("moves")).isEmpty()) {
						//the Host started the game
						//the joiner get it, the host ignores it
						lvGame.setValue(snapshot.toObject(OnlineGame.class));
						((OnlineGame)(lvGame.getValue())).startGameForJoiner();
						lvIsStarted.setValue(true);
					}
					else if (snapshot.getBoolean("finished") == true && snapshot.get("winner") != null)
					{
						lvGame.getValue().setFinished(true);
						lvGame.getValue().setWinner(snapshot.get("winner").toString());
						lvIsFinished.setValue(true);
					}
					else if (!((List<BoardLocation>) snapshot.get("moves")).isEmpty()){
						//it's a move and send it to handle a move
						BoardLocation lastMove = getLastMoveAsBoardLocation(snapshot.get("moves"));
						lvGame.getValue().makeTurn(lastMove);
						lvGame.setValue(lvGame.getValue());
						CheckInnerBoardFinish(lastMove.getOuter());
						Log.d("qqq",  "move happened");
					}
				} else {
					// The document does not exist (deleted
					lvGame.setValue(null);
				}
			}
		});
	}

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
	public Task<Boolean> deleteOnlineGame(){
		TaskCompletionSource<Boolean> taskAbortGame = new TaskCompletionSource<>();
		if(lvGame.getValue() == null) {
			taskAbortGame.setResult(true);
		}
		else if (lvGame.getValue().getMoves().isEmpty()) {
			delete(lvGame.getValue()).addOnSuccessListener(new OnSuccessListener<Boolean>() {
				@Override
				public void onSuccess(Boolean aBoolean) {
					lvGame.setValue(null);
					taskAbortGame.setResult(true);
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
				taskAbortGame.setResult(false);
				}
			});
		}
		else {
			lvGame.getValue().setFinished(true);
			lvGame.getValue().setWinner(Objects.equals(lvGame.getValue().getPlayer1().getIdFs(), localPlayerIdFs) ?
					lvGame.getValue().getPlayer2().getIdFs():
					lvGame.getValue().getPlayer1().getIdFs());
			update(lvGame.getValue());
			taskAbortGame.setResult(true);
		}
		return taskAbortGame.getTask();
	}

	public Task<Boolean> makeMove(BoardLocation location) {
		TaskCompletionSource<Boolean> taskMakeMove = new TaskCompletionSource<>();
		if(Objects.equals(lvGame.getValue().getCurrentPlayerIdFs(), localPlayerIdFs)) {
			if(lvGame.getValue().isLegal(location)) {
				lvGame.getValue().makeTurn(location);
				CheckInnerBoardFinish(location.getOuter());
				taskMakeMove.setResult(true);
			}
			else taskMakeMove.setException(new Exception("2"));
		}
		else taskMakeMove.setException(new Exception("1"));
		return taskMakeMove.getTask();
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
				lvGame.getValue().setWinner(lvGame.getValue().getOuterBoard().getBoard(innerBoard).getWinner() == 'X' ? "X" : "O");
				lvGame.getValue().setFinished(true);
				if(Objects.equals(lvGame.getValue().getPlayer1().getIdFs(), localPlayerIdFs)) {
					finishGame(lvGame.getValue().getPlayer1().getIdFs(), lvGame.getValue().getPlayer2().getIdFs()).addOnSuccessListener(new OnSuccessListener<Boolean>() {
						@Override
						public void onSuccess(Boolean aBoolean) {
							lvIsFinished.setValue(true);
							Log.d("qqq", "game finished");
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Log.d("qqq", "game not finished");
						}
					});
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
		data.put("score", lvGame.getValue().getWinner() == null ? 0.5 :
				(lvGame.getValue().getWinner().equals(player1IdFs) ? 1.0 : 0.0));
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
	public Task<OnlineGame> getOnlineGame(String idFs) {
		TaskCompletionSource<OnlineGame> taskMember = new TaskCompletionSource<>();

		super.getCollection().document(idFs).get()
				.addOnSuccessListener(documentSnapshot -> {
					OnlineGame member = documentSnapshot.toObject(OnlineGame.class);
					if (member != null) {
						taskMember.setResult(member);
					} else {
						taskMember.setResult(null);
					}
				})
				.addOnFailureListener(e -> {
					Log.e("qqq", "Error getting member: " + e.getMessage());
					taskMember.setResult(null);
				});

		return taskMember.getTask();
	}
}