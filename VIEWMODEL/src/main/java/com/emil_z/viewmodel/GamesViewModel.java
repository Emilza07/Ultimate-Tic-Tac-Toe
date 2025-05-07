package com.emil_z.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.emil_z.model.BoardLocation;
import com.emil_z.model.CpuGame;
import com.emil_z.model.Game;
import com.emil_z.model.Games;

import com.emil_z.model.OnlineGame;
import com.emil_z.model.Player;
import com.emil_z.repository.BASE.BaseRepository;
import com.emil_z.repository.GamesRepository;
import com.emil_z.viewmodel.BASE.BaseViewModel;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.Executors;

public class GamesViewModel extends BaseViewModel<Game, Games> {
	private GamesRepository repository;
	private final MutableLiveData<Integer> lvCode;
	private final MediatorLiveData<Game> lvGame;
	private final MediatorLiveData<char[][]> lvOuterBoardWinners;
	private final MediatorLiveData<Boolean> lvIsFinished;
	private final MediatorLiveData<Boolean> lvIsStarted;

	public GamesViewModel(Application application) {
		super(Game.class, Games.class, application);
		lvGame				= new MediatorLiveData<>();
		lvCode				= new MutableLiveData<>();
		lvOuterBoardWinners = new MediatorLiveData<>();
		lvIsFinished		= new MediatorLiveData<>();
		lvIsStarted			= new MediatorLiveData<>(false);
		isStartedObserver();
	}

	@Override
	protected BaseRepository<Game, Games> createRepository(Application application) {
		repository = new GamesRepository(application);
		return repository;
	}

	public LiveData<Game> getLvGame() {
		return lvGame;
	}

	public LiveData<Integer> getLvCode() {
		return lvCode;
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

	public void getUserGames(String userId) {
		lvCollection = repository.getUserGames(userId);
	}

	//region start game
	public void startCpuGame(){
		repository.startCpuGame();
	}

	public void startLocalGame(){
		repository.startLocalGame();
	}

	public void startOnlineGame(Player player) throws Exception {
		Tasks.call(Executors.newSingleThreadExecutor(), () -> {
			Tasks.await(repository.startOnlineGame(player));
			return true;
		}).addOnSuccessListener(aBoolean -> Log.d("qqq", "started"))
				.addOnFailureListener(e -> {
					//TODO: handle error
				});
	}
	//endregion

	private void isStartedObserver(){
		lvIsStarted.addSource(
				repository.getLvIsStarted(), aBoolean -> {
					if (aBoolean)
						setObservers();
					lvIsStarted.setValue(aBoolean);
				});
	}

	private void setObservers() {
		lvGame.addSource(
				repository.getLvGame(),
				lvGame::setValue);
		lvOuterBoardWinners.addSource(
				repository.getLvOuterBoardWinners(),
				lvOuterBoardWinners::setValue);
		lvIsFinished.addSource(
				repository.getLvIsFinished(),
				lvIsFinished::setValue);
	}

	private void removeLvGameObserver() {
		lvGame.removeSource(repository.getLvGame());
	}

	public void removeGame(){
		removeLvGameObserver();
		repository.deleteOnlineGame().addOnSuccessListener(aBoolean -> lvSuccess.setValue(aBoolean))
				.addOnFailureListener(e -> {
					setObservers();
					lvSuccess.setValue(false);
				});
	}

	public void makeMove(int oRow, int oCol, int iRow, int iCol) {
		repository.makeMove(new BoardLocation(oRow, oCol, iRow, iCol))
				.addOnSuccessListener(aBoolean -> {
					lvCode.setValue(0);
					if(lvGame.getValue() instanceof OnlineGame)
						repository.update(lvGame.getValue());
					else if (lvGame.getValue() instanceof CpuGame)
					{
						//TODO: make move for cpu
					}
				})
				.addOnFailureListener(e -> lvCode.setValue(Integer.valueOf(e.getMessage())));
	}

	public void resetLvCode() {
		lvCode.setValue(null);
	}
}