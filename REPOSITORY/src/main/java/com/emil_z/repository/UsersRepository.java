package com.emil_z.repository;

import android.app.Application;
import android.util.Log;

import com.emil_z.helper.StringUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
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
	public Task<Boolean> exist(String username) {
		TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();

		Query query = getCollection().whereEqualTo("username", username);

		if (query != null) {
			get(query)
					.addOnSuccessListener(savedEntity -> {
						if (savedEntity == null) {
							tcs.setResult(false);
						} else {
								if (username.equals(savedEntity.getUsername())) {
									tcs.setResult(true);
								} else {
									tcs.setResult(false);
								}
						}
					})
					.addOnFailureListener(e -> {
						Log.d("qqq", "Entering addFailureListener");
						tcs.setException(e);
						tcs.setResult(false);
					});
		}
		else
			tcs.setResult(false);
		return tcs.getTask();
	}
}