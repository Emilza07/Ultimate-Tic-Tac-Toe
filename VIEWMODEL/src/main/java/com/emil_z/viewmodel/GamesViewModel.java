package com.emil_z.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.emil_z.model.BoardLocation;
import com.emil_z.model.Game;
import com.emil_z.model.Games;

import com.emil_z.model.OnlineGame;
import com.emil_z.model.Player;
import com.emil_z.repository.BASE.BaseRepository;
import com.emil_z.repository.GamesRepository;
import com.emil_z.viewmodel.BASE.BaseViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.Executors;

public class GamesViewModel extends BaseViewModel<Game, Games> {
	private GamesRepository repository;
	private MutableLiveData<Integer> lvCode;
	private MediatorLiveData<Game> lvGame;
	private MediatorLiveData<char[][]> lvOuterBoardWinners;
	private MediatorLiveData<Boolean> lvIsFinished;
	private MediatorLiveData<Boolean> lvIsStarted;

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

	//region start gaFme
	public void startLocalGame(){
		repository.startLocalGame();
	}

	public void startOnlineGame(Player player) throws Exception {
		Tasks.call(Executors.newSingleThreadExecutor(), () -> {
			Tasks.await(repository.startOnlineGame(player));
			return true;
		}).addOnSuccessListener(new OnSuccessListener<Boolean>() {
			@Override
			public void onSuccess(Boolean aBoolean) {
				Log.d("qqq", "started");
			}
		});
	}

	//endregion

	private void isStartedObserver(){
		lvIsStarted.addSource(
				repository.getLvIsStarted(), new Observer<Boolean>() {
					@Override
					public void onChanged(Boolean aBoolean) {
						if (aBoolean)
							setObservers();
						lvIsStarted.setValue(aBoolean);
					}
				});
	}

	private void setObservers() {
		lvGame.addSource(
				repository.getLvGame(),
				game -> lvGame.setValue(game));
		lvOuterBoardWinners.addSource(
				repository.getLvOuterBoardWinners(),
				chars -> lvOuterBoardWinners.setValue(chars));
		lvIsFinished.addSource(
				repository.getLvIsFinished(),
				aBoolean -> lvIsFinished.setValue(aBoolean));
	}

	private void removeLvGameObserver() {
		lvGame.removeSource(repository.getLvGame());
	}

	public void removeGame(){
		removeLvGameObserver();
		repository.deleteOnlineGame().addOnSuccessListener(new OnSuccessListener<Boolean>() {
				@Override
				public void onSuccess(Boolean aBoolean) {
					lvGame.setValue(null);
					lvSuccess.setValue(true);
				}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				setObservers();
				lvSuccess.setValue(false);
			}
		});
	}

	public void makeMove(int oRow, int oCol, int iRow, int iCol) {
		repository.makeMove(new BoardLocation(oRow, oCol, iRow, iCol)).addOnSuccessListener( new OnSuccessListener<Boolean>() {;
			@Override
			public void onSuccess(Boolean aBoolean) {
				lvCode.setValue(0);
				if(lvGame.getValue() instanceof OnlineGame)
					repository.update(lvGame.getValue());
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				lvCode.setValue(Integer.valueOf(e.getMessage()));
			}
		});
	}

	public void resetLvCode() {
		lvCode.setValue(null);
	}
}