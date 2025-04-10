package com.emil_z.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.emil_z.model.BoardLocation;
import com.emil_z.model.Game;
import com.emil_z.model.Games;

import com.emil_z.model.User;
import com.emil_z.repository.BASE.BaseRepository;
import com.emil_z.repository.GamesRepository;
import com.emil_z.viewmodel.BASE.BaseViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

public class GamesViewModel extends BaseViewModel<Game, Games> {
	private GamesRepository repository;
	private MutableLiveData<Integer> lvCode;
	private MediatorLiveData<Game> lvGame;
	private MediatorLiveData<char[][]> lvOuterBoardWinners;
	private MediatorLiveData<Boolean> lvIsFinished;
	public GamesViewModel(Application application) {
		super(Game.class, Games.class, application);
		lvGame				= new MediatorLiveData<>();
		lvCode				= new MutableLiveData<>();
		lvOuterBoardWinners = new MediatorLiveData<>();
		lvIsFinished		= new MediatorLiveData<>();
		setObservers();
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

	//region start game
	public void startLocalGame(){
		repository.startLocalGame().addOnSuccessListener(new OnSuccessListener<Boolean>() {
			@Override
			public void onSuccess(Boolean aBoolean) {
				lvSuccess.setValue(true);
			}
		});
	}

	public void hostOnlineGame(User user) {
		repository.hostOnlineGame(user).addOnCompleteListener(new OnCompleteListener<Boolean>() {
			@Override
			public void onComplete(@NonNull Task<Boolean> task) {
				if (task.isSuccessful()) {
					lvGame.setValue(repository.getLvGame().getValue());
					lvSuccess.setValue(true); //inform the activity that the game was created and uploaded to the database
					repository.addSnapshotListener(lvGame.getValue().getIdFs());
				} else {
					// Handle failure
				}
			}
		});
	}

	public Task<String> joinOnlineGame(User user) {
		TaskCompletionSource<String> taskJoinGame = new TaskCompletionSource<>();
		repository.joinOnlineGame(user, new OnSuccessListener<String>() {
			@Override
			public void onSuccess(String s) {
				if (s == "Joined") {
					lvSuccess.setValue(true); //inform the activity that successfully joined the game
				}
			}
		}, new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				taskJoinGame.setResult(e.toString());
			}
		});
		return taskJoinGame.getTask();
	}
	//endregion

	private void setObservers() {
		lvGame.addSource(repository.getLvGame(), new Observer<Game>() {
			@Override
			public void onChanged(Game game) {
				lvGame.setValue(game);
			}
		});
		lvOuterBoardWinners.addSource(repository.getLvOuterBoardWinners(), new Observer<char[][]>() {
			@Override
			public void onChanged(char[][] chars) {
				lvOuterBoardWinners.setValue(chars);
			}
		});
		lvIsFinished.addSource(repository.getLvIsFinished(), new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
				lvIsFinished.setValue(aBoolean);
			}
		});

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