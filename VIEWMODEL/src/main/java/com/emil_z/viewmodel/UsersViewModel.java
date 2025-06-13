package com.emil_z.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;

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
	 * Registers a new user with the provided credentials and profile picture.
	 * Updates LiveData with the user entity on success, or sets success to false on failure.
	 *
	 * @param email The user's email address.
	 * @param username The user's username.
	 * @param password The user's password.
	 * @param picture The user's profile picture.
	 */
	public void register(String email, String username, String password, String picture) {

		repository.register(email, username, password, picture)
				.addOnSuccessListener(lvEntity::setValue)
				.addOnFailureListener(e -> lvSuccess.setValue(false)); //TODO: handle specific exceptions (especially unable to login)
	}

	/**
	 * Attempts to log in a user with the provided email and password.
	 * Updates LiveData with the user entity on success, or sets success to false on failure.
	 *
	 * @param email The email to log in with.
	 * @param password The password to verify.
	 */
	public void logIn(String email, String password) {
		repository.logIn(email, password)
				.addOnSuccessListener(user -> {
							lvEntity.setValue(user);
					})
				.addOnFailureListener(e -> lvSuccess.setValue(false));
	}

	/**
	 * Checks if a user is currently logged in.
	 * Updates LiveData with the user entity if logged in, or null if not.
	 */
	public void checkLoggedIn() {
		repository.checkLoggedIn()
				.addOnSuccessListener(user -> {
					lvEntity.setValue(user);
				})
				.addOnFailureListener(e -> lvEntity.setValue(null));
	}

	/**
	 * Logs out the currently logged-in user.
	 * Updates LiveData to indicate success or failure.
	 */
	public void logOut() {
		repository.logOut()
				.addOnSuccessListener(unused -> lvSuccess.setValue(true))
				.addOnFailureListener(e -> lvSuccess.setValue(false));
	}

	/**
	 * Checks if a user with the given username exists.
	 * Updates LiveData with the result.
	 * @param username The username to check for existence.
	 */
	public void exist(String username) {
		repository.exist(username)
				.addOnSuccessListener(unused -> lvExist.setValue(false))
				.addOnFailureListener(e -> lvExist.setValue(true));
	}
}