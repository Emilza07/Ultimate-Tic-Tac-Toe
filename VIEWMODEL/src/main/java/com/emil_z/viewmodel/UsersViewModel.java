package com.emil_z.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.emil_z.helper.PasswordUtil;
import com.emil_z.model.User;
import com.emil_z.model.Users;
import com.emil_z.repository.BASE.BaseRepository;
import com.emil_z.repository.UsersRepository;
import com.emil_z.viewmodel.BASE.BaseViewModel;

/**
 * ViewModel for managing user-related operations such as authentication,
 * checking user existence, and retrieving top players.
 */
public class UsersViewModel extends BaseViewModel<User, Users> {
	private UsersRepository repository;

	/**
	 * Constructs a UsersViewModel with the given application context.
	 * @param application The application context.
	 */
	public UsersViewModel(Application application) {
		super(User.class, Users.class, application);
	}

	/**
	 * Creates and returns the UsersRepository instance.
	 * @param application The application context.
	 * @return The UsersRepository instance.
	 */
	@Override
	protected BaseRepository<User, Users> createRepository(Application application) {
		repository = new UsersRepository(application);
		return repository;
	}

	/**
	 * Returns LiveData indicating whether a user exists.
	 * @return LiveData of Boolean representing user existence.
	 */
	public LiveData<Boolean> getLiveDataExist() {
		return lvExist;
	}

	/**
	 * Retrieves a paginated list of top players based on ELO rating.
	 * @param limit The maximum number of players to retrieve.
	 * @param lastElo The ELO rating to start after (for pagination).
	 * @param lastIdFs The IdFs to start after (for pagination).
	 */
	public void getTopPlayersPaginated(int limit, float lastElo, String lastIdFs) {
		lvCollection = repository.getTopPlayersPaginated(limit, lastElo, lastIdFs);
	}

	/**
	 * Attempts to log in a user with the provided username and password.
	 * Updates LiveData with the user entity on success, or sets success to false on failure.
	 * @param Username The username to log in with.
	 * @param password The password to verify.
	 */
	public void logIn(String Username, String password) {
		repository.getCollection().whereEqualTo("username", Username).get()
				.addOnSuccessListener(queryDocumentSnapshots -> {
					if (!queryDocumentSnapshots.isEmpty()) {
						if (PasswordUtil.verifyPassword(password, queryDocumentSnapshots.getDocuments().get(0).getString("hashedPassword"))) {
							User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
							lvEntity.setValue(user);
						}
						else {
							lvSuccess.setValue(false);
						}
					} else {
						lvSuccess.setValue(false);
					}
				})
				.addOnFailureListener(e -> lvEntity.setValue(null));
	}

	/**
	 * Checks if a user with the given username exists.
	 * Updates LiveData with the result.
	 * @param username The username to check for existence.
	 */
	public void exist(String username) {
		repository.exist(username)
				.addOnSuccessListener(lvExist::setValue)
				.addOnFailureListener(e -> lvExist .setValue(false));
	}
}