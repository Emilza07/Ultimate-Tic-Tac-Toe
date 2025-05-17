package com.emil_z.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.emil_z.model.User;
import com.emil_z.model.Users;
import com.emil_z.repository.BASE.BaseRepository;

/**
 * Repository for managing user data and queries in Firestore.
 * Extends {@link BaseRepository} to provide CRUD operations for {@link User} and {@link Users}.
 */
public class UsersRepository extends BaseRepository<User, Users> {
	private final MutableLiveData<Users> lvTopUsers;

	/**
	 * Constructs a UsersRepository with the given application context.
	 *
	 * @param application the application context
	 */
	public UsersRepository(Application application) {
		super(User.class, Users.class, application);

		lvTopUsers = new MutableLiveData<>();
	}

	/**
	 * Returns a Firestore query to check if a user entity exists by its idFs.
	 *
	 * @param entity the user entity to check
	 * @return the Firestore query
	 */
	@Override
	protected Query getQueryForExist(User entity) {
		return getCollection().whereEqualTo("idFs", entity.getIdFs());
	}

	/**
	 * Checks asynchronously if a username exists in the users collection.
	 *
	 * @param username the username to check
	 * @return a Task resolving to true if the username exists, false otherwise
	 */
	public Task<Boolean> exist(String username) {
		TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
		Query query = getCollection().whereEqualTo("username", username);

		get(query)
			.addOnSuccessListener(savedEntity -> {
				if (savedEntity != null && username.equals(savedEntity.getUsername())) {
					tcs.setResult(true);
					return;
				}
				tcs.setResult(false);
			})
			.addOnFailureListener(tcs::setException);

		return tcs.getTask();
	}

	/**
	 * Retrieves a paginated list of top players ordered by ELO rating.
	 * Supports pagination using the last ELO and last idFs.
	 * Updates the {@link #lvTopUsers} LiveData with the result.
	 *
	 * @param limit    the maximum number of users to retrieve
	 * @param lastElo  the ELO of the last user from the previous page, or -1 for the first page
	 * @param lastIdFs the idFs of the last user from the previous page
	 * @return LiveData containing the list of top users
	 */
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