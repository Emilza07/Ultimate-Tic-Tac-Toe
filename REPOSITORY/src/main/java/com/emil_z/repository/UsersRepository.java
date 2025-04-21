package com.emil_z.repository;

import android.app.Application;

import com.google.firebase.firestore.Query;
import com.emil_z.model.User;
import com.emil_z.model.Users;
import com.emil_z.repository.BASE.BaseRepository;


public class UsersRepository extends BaseRepository<User, Users> {
	public UsersRepository(Application application) {
		super(User.class, Users.class, application);
	}

	@Override
	protected Query getQueryForExist(User entity) {
		return getCollection().whereEqualTo("idFs", entity.getIdFs());
	}
	public Query getUsersByUsername(String username) {
		return getCollection().whereEqualTo("username", username);
	}
}