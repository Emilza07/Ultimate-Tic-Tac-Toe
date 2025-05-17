package com.emil_z.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.emil_z.model.BoardLocation;
import com.emil_z.model.Game;
import com.emil_z.model.GameType;
import com.emil_z.model.Games;
import com.emil_z.model.Player;
import com.emil_z.repository.BASE.BaseRepository;
import com.emil_z.repository.BaseGamesRepository;
import com.emil_z.repository.CpuGamesRepository;
import com.emil_z.repository.ReplayGamesRepository;
import com.emil_z.repository.LocalGamesRepository;
import com.emil_z.repository.OnlineGamesRepository;
import com.emil_z.viewmodel.BASE.BaseViewModel;

import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * ViewModel for managing game logic and state for different game types (CPU, Local, Online, Replay).
 * Handles game lifecycle, moves, and LiveData observers for UI updates.
 */
public class GamesViewModel extends BaseViewModel<Game, Games> {
	private final MediatorLiveData<Integer> lvCode;
	private final MediatorLiveData<Game> lvGame;
	private final MediatorLiveData<char[][]> lvOuterBoardWinners;
	private final MediatorLiveData<Boolean> lvIsStarted;
	private final MediatorLiveData<Boolean> lvIsFinished;
	private final MediatorLiveData<String> lvGameIdFs;

	private final GameType gameType;
	private BaseGamesRepository repository;

	/**
	 * Constructs a GamesViewModel for the specified game type.
	 * @param application The application context.
	 * @param gameType The type of game (CPU, Local, Online, Replay).
	 */
	public GamesViewModel(Application application, GameType gameType) {
		super(Game.class, Games.class, application);
		this.gameType = gameType;
		lvCode = new MediatorLiveData<>();
		lvGame = new MediatorLiveData<>();
		lvOuterBoardWinners = new MediatorLiveData<>();
		lvIsFinished = new MediatorLiveData<>();
		lvIsStarted = new MediatorLiveData<>(false);
		lvGameIdFs = new MediatorLiveData<>();
		super.repository = createRepository(application);
		isStartedObserver();
	}

	/**
	 * Creates the appropriate repository based on the game type.
	 * @param application The application context.
	 * @return The created repository.
	 */
	@Override
	protected BaseRepository<Game, Games> createRepository(Application application) {
		if (gameType == null) return null;
		if (repository == null) {
			switch (gameType) {
				case CPU: repository = new CpuGamesRepository(application); break;
				case LOCAL: repository = new LocalGamesRepository(application); break;
				case ONLINE: repository = new OnlineGamesRepository(application); break;
				case REPLAY: repository = new ReplayGamesRepository(application); break;
				default: throw new IllegalStateException("Unknown GameType: " + gameType);
			}
		}
		return repository;
	}

	/**
	 * @return LiveData for status/error codes.
	 */
	public LiveData<Integer> getLiveDataCode() {
		return lvCode;
	}

	/**
	 * @return LiveData for the current game state.
	 */
	public LiveData<Game> getLiveDataGame() {
		return lvGame;
	}

	/**
	 * @return LiveData for outer board winners.
	 */
	public LiveData<char[][]> getLiveDataOuterBoardWinners() {
		return lvOuterBoardWinners;
	}

	/**
	 * @return LiveData indicating if the game has started.
	 */
	public LiveData<Boolean> getLiveDataIsStarted() {
		return lvIsStarted;
	}

	/**
	 * @return LiveData indicating if the game is finished.
	 */
	public LiveData<Boolean> getLiveDataIsFinished() {
		return lvIsFinished;
	}

	/**
	 * @return LiveData for the game IdFs (for online games).
	 */
	public LiveData<String> getLiveDataGameIdFs() {
		return lvGameIdFs;
	}

	/**
	 * Loads a paginated list of user games into lvCollection (for online games).
	 * @param userIdFs The user IdFs.
	 * @param limit The maximum number of games to load.
	 * @param startAfterIdFs The IdFs to start after (for pagination).
	 */
	public void getUserGamesPaginated(String userIdFs, int limit, String startAfterIdFs) {
		if (repository instanceof OnlineGamesRepository)
			lvCollection = ((OnlineGamesRepository) repository).getUserGamesPaginated(userIdFs, limit, startAfterIdFs);
	}

	/**
	 * Starts a new CPU game.
	 * @param crossPlayerIdFs The IdFs of the cross player (or "CPU").
	 */
	public void startCpuGame(String crossPlayerIdFs) {
		repository.startGame(null, crossPlayerIdFs);
		if (Objects.equals(crossPlayerIdFs, "CPU"))
			Executors.newSingleThreadExecutor().execute(() -> ((CpuGamesRepository) repository).makeCpuMove());
	}

	/**
	 * Starts a new local game.
	 */
	public void startLocalGame() {
		repository.startGame(null, null);
	}

	/**
	 * Starts a new online game with the specified player.
	 * @param player The player to start the game with.
	 */
	public void startOnlineGame(Player player) {
		setGameIdFsObserver();
		Executors.newSingleThreadExecutor().execute(() -> repository.startGame(player, null));
	}

	/**
	 * Starts a replay game with the specified game data.
	 * @param game The game to load from Firebase.
	 */
	public void startReplayGame(Game game) {
		((ReplayGamesRepository) repository).startGame(game);
	}

	/**
	 * Observes code and game start state changes.
	 * Sets up further observers when the game starts.
	 */
	private void isStartedObserver() {
		lvCode.addSource(repository.getLiveDataCode(), lvCode::setValue);
		lvIsStarted.addSource(
			repository.getLiveDataIsStarted(), aBoolean -> {
				if (aBoolean)
					setObservers();
				lvIsStarted.setValue(aBoolean);
			});
	}

	/**
	 * Sets up observers for game state, outer board winners, and finished state.
	 */
	private void setObservers() {
		lvGame.addSource(
				repository.getLiveDataGame(),
				lvGame::setValue);
		lvOuterBoardWinners.addSource(
				repository.getLiveDataOuterBoardWinners(),
				lvOuterBoardWinners::setValue);
		lvIsFinished.addSource(
				repository.getLiveDataIsFinished(),
				lvIsFinished::setValue);
	}

	/**
	 * Observes the Firestore game IdFs for online games and updates LiveData accordingly.
	 */
	private void setGameIdFsObserver() {
		lvGameIdFs.addSource(((OnlineGamesRepository) repository).getLiveDataGameIdFs(), gameIdFs -> {
			if (gameIdFs != null) {
				lvGameIdFs.setValue(gameIdFs);
				lvGameIdFs.removeSource(((OnlineGamesRepository) repository).getLiveDataGameIdFs());
			} else {
				lvGameIdFs.setValue(null);
			}
		});
	}

	/**
	 * Exits the current online game and updates success LiveData.
	 */
	public void exitGame() {
		((OnlineGamesRepository) repository).exitGame().addOnSuccessListener(aBoolean -> lvSuccess.setValue(aBoolean))
				.addOnFailureListener(e -> lvSuccess.setValue(false));
	}

	/**
	 * Makes a move in the current game and handles post-move logic for CPU/Online games.
	 * @param boardLocation The location of the move.
	 */
	@SuppressWarnings("ConstantConditions")
	public void makeMove(BoardLocation boardLocation) {
		repository.makeMove(boardLocation)
			.addOnSuccessListener(voidTask -> {
				if (repository instanceof OnlineGamesRepository) {
					repository.update(lvGame.getValue());
				} else if (repository instanceof CpuGamesRepository && !lvGame.getValue().isFinished()) {
					Executors.newSingleThreadExecutor().execute(() -> ((CpuGamesRepository) repository).makeCpuMove());
				}
			})
			.addOnFailureListener(e -> lvCode.setValue(Integer.valueOf(e.getMessage())));
	}

	/**
	 * Resets the status/error code LiveData to 0.
	 */
	public void resetLvCode() {
		repository.resetLiveDataCode();
	}
}