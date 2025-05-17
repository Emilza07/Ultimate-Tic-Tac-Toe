package com.emil_z.repository;

import android.app.Application;
import android.graphics.Point;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.emil_z.helper.UserSessionPreference;
import com.emil_z.model.BoardLocation;
import com.emil_z.model.Game;
import com.emil_z.model.Games;
import com.emil_z.model.Player;
import com.emil_z.repository.BASE.BaseRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.Query;

import java.util.Objects;

/**
 * Abstract repository for managing game data and logic.
 * Handles LiveData for game state, board winners, and game status.
 * Provides base methods for starting games and making moves.
 */
public abstract class BaseGamesRepository extends BaseRepository<Game, Games> {
	protected final MutableLiveData<Integer> lvCode;
	protected final MutableLiveData<Game> lvGame;
	protected final MutableLiveData<char[][]> lvOuterBoardWinners;
	protected final MutableLiveData<Boolean> lvIsStarted;
	protected final MutableLiveData<Boolean> lvIsFinished;

	protected final String localPlayerIdFs;

	/**
	 * Constructs a BaseGamesRepository and initializes LiveData fields.
	 * @param application The application context.
	 */
	public BaseGamesRepository(Application application) {
		super(Game.class, Games.class, application);
		lvCode = new MutableLiveData<>();
		lvGame = new MutableLiveData<>();
		lvOuterBoardWinners = new MutableLiveData<>();
		lvOuterBoardWinners.setValue(new char[3][3]);
		lvIsStarted = new MutableLiveData<>(false);
		lvIsFinished = new MutableLiveData<>();
		localPlayerIdFs = new UserSessionPreference(application).getUserIdFs();
	}

	/**
	 * Returns LiveData for status or error codes.
	 * @return LiveData of Integer representing the current code.
	 */
	public LiveData<Integer> getLiveDataCode() {
		return lvCode;
	}

	/**
	 * Returns LiveData for the current game state.
	 * @return LiveData of the current Game.
	 */
	public LiveData<Game> getLiveDataGame() {
		return lvGame;
	}

	/**
	 * Returns LiveData for the outer board winners.
	 * @return LiveData of a 2D char array representing winners.
	 */
	public LiveData<char[][]> getLiveDataOuterBoardWinners() {
		return lvOuterBoardWinners;
	}

	/**
	 * Returns LiveData indicating if the game has started.
	 * @return LiveData of Boolean for game started status.
	 */
	public LiveData<Boolean> getLiveDataIsStarted() {
		return lvIsStarted;
	}

	/**
	 * Returns LiveData indicating if the game has finished.
	 * @return LiveData of Boolean for game finished status.
	 */
	public LiveData<Boolean> getLiveDataIsFinished() {
		return lvIsFinished;
	}

	/**
	 * Resets the LiveData code value to 0.
	 */
	public void resetLiveDataCode() {
		lvCode.setValue(0);
	}

	/**
	 * Returns a Firestore query to check if a game entity exists by its idFs.
	 * @param entity The game entity to check.
	 * @return Firestore Query for existence check.
	 */
	@Override
	protected Query getQueryForExist(Game entity) {
		return getCollection().whereEqualTo("idFs", entity.getIdFs());
	}

	/**
	 * Starts a new game with the given player and cross player IdFs.
	 * Must be implemented by subclasses.
	 * @param player The player to start the game with.
	 * @param crossPlayerIdFs The IdFs of the cross player.
	 */
	public abstract void startGame(Player player, String crossPlayerIdFs);

	/**
	 * Attempts to make a move at the specified board location.
	 * Validates move legality and updates game state accordingly.
	 * Returns a Task that completes on success or fails with an error code:
	 * "1" - Not the current player's turn,
	 * "2" - Illegal move,
	 * "3" - Game not started.
	 * @param location The board location for the move.
	 * @return Task representing the result of the move.
	 */
	@SuppressWarnings("ConstantConditions")
	public Task<Void> makeMove(BoardLocation location) {
		TaskCompletionSource<Void> taskMakeMove = new TaskCompletionSource<>();
		Game game = lvGame.getValue();
		if (!game.isStarted()) {
			taskMakeMove.setException(new Exception("1"));
		} else if (!Objects.equals(game.getCurrentPlayerIdFs(), localPlayerIdFs)) {
			taskMakeMove.setException(new Exception("2"));
		} else if (!game.isLegal(location)) {
			taskMakeMove.setException(new Exception("3"));
		} else {
			game.makeMove(location);
			lvGame.setValue(game);
			checkInnerBoardFinish(location.getOuter());
			taskMakeMove.setResult(null);
		}
		return taskMakeMove.getTask();
	}

	/**
	 * Checks if the inner board at the given point is finished.
	 * Updates the outer board winners and sets the game as finished if necessary.
	 * @param innerBoard The coordinates of the inner board to check.
	 */
	@SuppressWarnings("ConstantConditions")
	protected void checkInnerBoardFinish(Point innerBoard) {
		Game game = lvGame.getValue();
		if (game.getOuterBoard().getBoard(innerBoard).isFinished()) {
			char[][] winners = lvOuterBoardWinners.getValue();
			winners[innerBoard.x][innerBoard.y] = game.getOuterBoard().getBoard(innerBoard).getWinner();
			lvOuterBoardWinners.postValue(winners);
			if (game.getOuterBoard().isGameOver()) {
				game.setFinished(true);
			}
		}
	}
}