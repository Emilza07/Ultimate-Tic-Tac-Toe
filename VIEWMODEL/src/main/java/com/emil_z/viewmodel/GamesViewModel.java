package com.emil_z.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.emil_z.model.BoardLocation;
import com.emil_z.model.Game;
import com.emil_z.model.GameType;
import com.emil_z.model.Games;

import com.emil_z.model.OnlineGame;
import com.emil_z.model.Player;
import com.emil_z.repository.BASE.BaseRepository;
import com.emil_z.repository.CpuGamesRepository;
import com.emil_z.repository.BaseGamesRepository;
import com.emil_z.repository.HistoryGameRepository;
import com.emil_z.repository.LocalGamesRepository;
import com.emil_z.repository.OnlineGamesRepository;
import com.emil_z.viewmodel.BASE.BaseViewModel;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Objects;
import java.util.concurrent.Executors;

public class GamesViewModel extends BaseViewModel<Game, Games> {
	private BaseGamesRepository repository;
	private final MutableLiveData<Integer> lvCode;
	private final MediatorLiveData<Game> lvGame;
	private final MediatorLiveData<char[][]> lvOuterBoardWinners;
	private final MediatorLiveData<Boolean> lvIsFinished;
	private final MediatorLiveData<Boolean> lvIsStarted;
	private final GameType gameType;

	public GamesViewModel(Application application, GameType gameType) {
		super(Game.class, Games.class, application);
		this.gameType 		= gameType;
		lvGame				= new MediatorLiveData<>();
		lvCode				= new MutableLiveData<>();
		lvOuterBoardWinners = new MediatorLiveData<>();
		lvIsFinished		= new MediatorLiveData<>();
		lvIsStarted			= new MediatorLiveData<>(false);
		super.repository	= createRepository(application);
		isStartedObserver();
	}

	@Override
	protected BaseRepository<Game, Games> createRepository(Application application) {
		if (gameType == null) {
			// We're being called from parent constructor before gameType is initialized
			return null;
		}

		if (repository == null) {
			switch (gameType) {
				case CPU:
					repository = new CpuGamesRepository(application);
					break;
				case LOCAL:
					repository = new LocalGamesRepository(application);
					break;
				case ONLINE:
					repository = new OnlineGamesRepository(application);
					break;
				case HISTORY:
					repository = new HistoryGameRepository(application);
					break;
				default:
					throw new IllegalStateException("Unknown GameType: " + gameType);
			}
		}
		return repository;
	}

	public LiveData<Game> getLiveDataGame() {
		return lvGame;
	}

	public LiveData<Integer> getLiveDataCode() {
		return lvCode;
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

	public void getUserGames(String userId) {
		lvCollection = repository.getUserGames(userId);
	}

	public void getOnlineGame(String gameId) {
		OnlineGame game = null;
		((HistoryGameRepository) repository).getOnlineGame(gameId).addOnSuccessListener(new OnSuccessListener<OnlineGame>() {
			@Override
			public void onSuccess(OnlineGame onlineGame) {
				lvEntity.setValue(onlineGame);
			}
		});
	}

	//region start game
	public void startCpuGame(String crossPlayerIdFs) {
		repository.startGame(null, crossPlayerIdFs);
		if (Objects.equals(crossPlayerIdFs, "CPU")) {
			Executors.newSingleThreadExecutor().execute(() -> {
				((CpuGamesRepository) repository).makeCpuMove();
			});
		}
	}

	public void startLocalGame(){
		repository.startGame(null, null);
	}

	public void startOnlineGame(Player player) {
		Executors.newSingleThreadExecutor().execute(() -> {
			repository.startGame(player, null);
		});
	}

	public void startHistoryGame(Game game) {
		((HistoryGameRepository) repository).startGame(game);
	}
	//endregion

	private void isStartedObserver(){
		lvIsStarted.addSource(
				repository.getLiveDataIsStarted(), aBoolean -> {
					if (aBoolean)
						setObservers();
					lvIsStarted.setValue(aBoolean);
				});
	}

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

	public void exitGame(){
		((OnlineGamesRepository) repository).exitGame().addOnSuccessListener(aBoolean -> lvSuccess.setValue(aBoolean))
				.addOnFailureListener(e -> {
					lvSuccess.setValue(false); //TODO: notify the user about error in deleting game
				});
	}

	public void makeMove(BoardLocation boardLocation) {
		repository.makeMove(boardLocation)
				.addOnSuccessListener(voidTask -> {
					if(repository instanceof OnlineGamesRepository)
						repository.update(lvGame.getValue());
					else if (repository instanceof CpuGamesRepository)
					{
						if(!lvGame.getValue().isFinished())
							Executors.newSingleThreadExecutor().execute(() -> {
								((CpuGamesRepository) repository).makeCpuMove();
							});					}
				})
				.addOnFailureListener(e -> lvCode.setValue(Integer.valueOf(e.getMessage())));
	}

	public void resetLvCode() {
		lvCode.setValue(0);
	}
}