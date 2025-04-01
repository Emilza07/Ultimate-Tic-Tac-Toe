package com.emil_z.viewmodel;

import android.app.Application;

import com.emil_z.model.Player;
import com.emil_z.model.Players;
import com.emil_z.repository.BASE.BaseRepository;
import com.emil_z.repository.PlayersRepository;
import com.emil_z.viewmodel.BASE.BaseViewModel;

public class PlayersViewModel extends BaseViewModel<Player, Players> {
	private PlayersRepository repository;

	public PlayersViewModel(Application application) {
		super(Player.class, Players.class, application);
	}

	@Override
	protected BaseRepository<Player, Players> createRepository(Application application) {
		repository = new PlayersRepository(application);
		return repository;
	}
	public void logIn(String email, String password) {
		repository.getCollection().whereEqualTo("email", email).whereEqualTo("password", password).get()
				.addOnSuccessListener(queryDocumentSnapshots -> {
					if (queryDocumentSnapshots.size() > 0) {
						Player player = queryDocumentSnapshots.getDocuments().get(0).toObject(Player.class);
						lvEntity.setValue(player);
					}
				})
				.addOnFailureListener(e -> {
					lvEntity.setValue(null);
				});
	}
}