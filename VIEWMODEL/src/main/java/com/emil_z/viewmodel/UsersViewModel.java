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
 * ViewModel class for managing user-related operations.
 * Extends the BaseViewModel to provide functionality for handling User and Users entities.
 */
public class UsersViewModel extends BaseViewModel<User, Users> {
	private UsersRepository repository;


	public UsersViewModel(Application application) {
		super(User.class, Users.class, application);
	}

	@Override
	protected BaseRepository<User, Users> createRepository(Application application) {
		repository = new UsersRepository(application);
		return repository;
	}

	public LiveData<Boolean> getLiveDataExist() {
		return lvExist;
	}

	/**
	 * Logs in a user by verifying the provided username and password.
	 * Updates the LiveData with the user entity if successful, or sets success to false if not.
	 *
	 * @param Username The username of the user.
	 * @param password The password of the user.
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

	public void exist(String username) {
		repository.exist(username)
				.addOnSuccessListener(exist -> {
					if (exist) {
						lvExist.setValue(true);
					} else {
						lvExist.setValue(false);
					}
				}).addOnFailureListener(e -> {
				});
	}

	public void getTopPlayersPaginated(int limit, float lastElo, String lastIdFs) {
		lvCollection = repository.getTopPlayersPaginated(limit, lastElo, lastIdFs);
	}
}