package com.emil_z.repository;

import android.app.Application;
import android.graphics.Point;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.emil_z.helper.PreferenceManager;
import com.emil_z.model.BoardLocation;
import com.emil_z.model.Game;
import com.emil_z.model.Games;
import com.emil_z.model.Player;
import com.emil_z.repository.BASE.BaseRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public abstract class BaseGamesRepository extends BaseRepository<Game, Games> {
	protected final MutableLiveData<Game> lvGame;
	protected final MutableLiveData<char[][]> lvOuterBoardWinners;
	protected final MutableLiveData<Boolean> lvIsFinished;
	protected final MutableLiveData<Boolean> lvIsStarted;
	protected final String localPlayerIdFs;
	protected int retryCounter = 0;


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

	@Override
	protected Query getQueryForExist(Game entity) {
		return getCollection().whereEqualTo("idFs", entity.getIdFs());
	}

	public abstract void startGame(Player player, String crossPlayerIdFs);

	@SuppressWarnings("ConstantConditions")
	public Task<Void> makeMove(BoardLocation location) {
		TaskCompletionSource<Void> taskMakeMove = new TaskCompletionSource<>();
		if (Objects.equals(lvGame.getValue().getCurrentPlayerIdFs(), localPlayerIdFs)) {
			if (lvGame.getValue().isLegal(location)) {
				lvGame.getValue().makeMove(location);
				lvGame.setValue(lvGame.getValue());
				checkInnerBoardFinish(location.getOuter());
				taskMakeMove.setResult(null);
			} else taskMakeMove.setException(new Exception("2"));
		} else taskMakeMove.setException(new Exception("1"));
		return taskMakeMove.getTask();
	}

	@SuppressWarnings("ConstantConditions")
	protected void checkInnerBoardFinish(Point innerBoard) {
		if (lvGame.getValue().getOuterBoard().getBoard(innerBoard).isFinished()) {
			lvOuterBoardWinners.getValue()[innerBoard.x][innerBoard.y] = lvGame.getValue().getOuterBoard().getBoard(innerBoard).getWinner();
			lvOuterBoardWinners.postValue(lvOuterBoardWinners.getValue());
			if (lvGame.getValue().getOuterBoard().isGameOver()) {
				lvGame.getValue().setFinished(true);
			}

		}
	}
}