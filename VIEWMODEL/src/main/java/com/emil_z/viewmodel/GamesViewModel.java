package com.emil_z.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

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

public class GamesViewModel extends BaseViewModel<Game, Games> {
	private GamesRepository repository;
	private MediatorLiveData<Game> lvGame;

	public GamesViewModel(Application application) {
		super(Game.class, Games.class, application);
		lvGame = new MediatorLiveData<>();
	}

	@Override
	protected BaseRepository<Game, Games> createRepository(Application application) {
		repository = new GamesRepository(application);
		return repository;
	}

	public LiveData<Game> getLvGame() {
		return lvGame;
	}

	public void startLocalGame(){
		setLvGameObserver();
		repository.startLocalGame().addOnSuccessListener(new OnSuccessListener<Boolean>() {
			@Override
			public void onSuccess(Boolean aBoolean) {
				lvSuccess.setValue(true);
			}
		});
	}

	public void hostOnlineGame(User user) {
		setLvGameObserver();
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

	public void joinOnlineGame(User user) {
		repository.joinOnlineGame(user, new OnSuccessListener<String>() {
			@Override
			public void onSuccess(String s) {
				if (s == "Joined") {
					lvSuccess.setValue(true); //inform the activity that successfully joined the game
					setLvGameObserver();
				}
			}
		}, new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {

			}
		});
	}

	private void setLvGameObserver() {
		lvGame.addSource(repository.getLvGame(), new Observer<Game>() {
			@Override
			public void onChanged(Game game) {
				lvGame.setValue(game);
			}
		});
	}

	private void removeLvGameObserver() {
		lvGame.removeSource(repository.getLvGame());
	}

	public void removeGame(){
		removeLvGameObserver();
		repository.abortOnlineGame().addOnSuccessListener(new OnSuccessListener<Boolean>() {
				@Override
				public void onSuccess(Boolean aBoolean) {
					lvGame.setValue(null);
					lvSuccess.setValue(true);
				}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				setLvGameObserver();
				lvSuccess.setValue(false);
			}
		});
	}
}