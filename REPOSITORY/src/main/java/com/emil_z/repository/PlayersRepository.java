package com.emil_z.repository;

import android.app.Application;

import com.google.firebase.firestore.Query;
import com.emil_z.model.Player;
import com.emil_z.model.Players;
import com.emil_z.repository.BASE.BaseRepository;

public class PlayersRepository extends BaseRepository<Player, Players> {
	public PlayersRepository(Application application) {
		super(Player.class, Players.class, application);
	}

	@Override
	protected Query getQueryForExist(Player entity) {
		return getCollection().whereEqualTo("email", entity.getEmail());
	}
}