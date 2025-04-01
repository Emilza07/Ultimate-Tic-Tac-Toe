package com.emil_z.viewmodel;

import android.app.Application;

import com.emil_z.model.User;
import com.emil_z.model.Users;
import com.emil_z.repository.BASE.BaseRepository;
import com.emil_z.repository.UsersRepository;
import com.emil_z.viewmodel.BASE.BaseViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

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
	public void logIn(String Username, String password) {
		repository.getCollection().whereEqualTo("Username", Username).whereEqualTo("password", password).get()
				.addOnSuccessListener(queryDocumentSnapshots -> {
					if (queryDocumentSnapshots.size() > 0) {
						User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
						lvEntity.setValue(user);
					}
				})
				.addOnFailureListener(e -> {
					lvEntity.setValue(null);
				});
	}

	public void getFirstUserByUsername(String username, OnSuccessListener<User> onSuccessListener, OnFailureListener onFailureListener) {
		repository.getUsersByUsername(username).get()
				.addOnSuccessListener(queryDocumentSnapshots -> {
					if (!queryDocumentSnapshots.isEmpty()) {
						User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
						onSuccessListener.onSuccess(user);
					} else {
						onSuccessListener.onSuccess(null);
					}
				})
				.addOnFailureListener(onFailureListener);
	}
}