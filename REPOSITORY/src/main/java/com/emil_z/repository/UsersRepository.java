package com.emil_z.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.emil_z.model.User;
import com.emil_z.model.Users;
import com.emil_z.repository.BASE.BaseRepository;


public class UsersRepository extends BaseRepository<User, Users> {
	private final MutableLiveData<Users> lvTopUsers;

	public UsersRepository(Application application) {
		super(User.class, Users.class, application);

		lvTopUsers = new MutableLiveData<>();
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

	public LiveData<Users> getTopPlayersPaginated(int limit, float lastElo, String lastIdFs) {
		Query query = getCollection()
				.orderBy("elo", Query.Direction.DESCENDING)
				.orderBy("idFs")
				.limit(limit);

		if (lastElo != -1) {
			query = query.startAfter(lastElo, lastIdFs);
		}

		query.get().addOnSuccessListener(queryDocumentSnapshots -> {
			Users users = new Users();
			for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
				User user = doc.toObject(User.class);
				if (user != null) {
					users.add(user);
				}
			}
			lvTopUsers.setValue(users);
		});

		return lvTopUsers;
	}
}