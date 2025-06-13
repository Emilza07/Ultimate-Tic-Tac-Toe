package com.emil_z.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.emil_z.repository.BASE.FirebaseImageStorage;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.emil_z.model.User;
import com.emil_z.model.Users;
import com.emil_z.repository.BASE.BaseRepository;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for managing user data and queries in Firestore.
 * Extends {@link BaseRepository} to provide CRUD operations for {@link User} and {@link Users}.
 */
public class UsersRepository extends BaseRepository<User, Users> {
	private final MutableLiveData<Users> lvTopUsers;
	private FirebaseAuth mAuth;

	/**
	 * Constructs a UsersRepository with the given application context.
	 *
	 * @param application the application context
	 */
	public UsersRepository(Application application) {
		super(User.class, Users.class, application);
		lvTopUsers = new MutableLiveData<>();
		mAuth = FirebaseAuth.getInstance();
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
	 * Checks if an email already exists in the system using a Firebase Function.
	 *
	 * @param email the email to check
	 * @return a Task that completes when the check is done
	 */
	public Task<Void> exist(String email) {
		Map<String, Object> data = new HashMap<>();
		data.put("email", email);

		return function
			.getHttpsCallable("check_email_exist")
			.call(data).continueWith(task -> {
				if (task.getException() instanceof FirebaseFunctionsException) {
					if (((FirebaseFunctionsException) task.getException()).getCode() == FirebaseFunctionsException.Code.ALREADY_EXISTS)
						throw new FirebaseAuthUserCollisionException("EMAIL_ALREADY_EXISTS", "Email already exists");
				} else if (!task.isSuccessful()) {
					throw task.getException();
				}
				return null;
			});
	}

	/**
	 * Registers a new user by calling a Firebase Function and logging them in.
	 *
	 * @param email    the user's email
	 * @param username the user's username
	 * @param password the user's password
	 * @param picture  the user's profile picture
	 * @return a Task that resolves to the registered User
	 */
	public Task<User> register(String email, String username, String password, String picture) {
		TaskCompletionSource<User> tcs = new TaskCompletionSource<>();
		Map<String, Object> data = new HashMap<>();
		data.put("email", email);
		data.put("username", username);
		data.put("password", password);
		data.put("picture", picture);

		function
			.getHttpsCallable("create_user")
			.call(data).addOnSuccessListener(httpsCallableResult -> logIn(email, password)
				.addOnSuccessListener(tcs::setResult)
				.addOnFailureListener(tcs::setException));
		return tcs.getTask();
	}

	/**
	 * Logs in a user with the given email and password.
	 *
	 * @param email    the user's email
	 * @param password the user's password
	 * @return a Task that resolves to the logged-in User
	 */
	public Task<User> logIn(String email, String password) {
		TaskCompletionSource<User> tcs = new TaskCompletionSource<>();
		mAuth.signInWithEmailAndPassword(email, password)
			.addOnCompleteListener(task -> {
				if (task.isSuccessful()) {
					FirebaseUser fbUser = mAuth.getCurrentUser();
					get(fbUser.getUid(), "profilePicture", "profilePictureUrl")
						.addOnSuccessListener(user -> tcs.setResult(new User(user)))
						.addOnFailureListener(tcs::setException);
				} else {
					tcs.setException(task.getException());
				}
			});
		return tcs.getTask();
	}

	/**
	 * Checks if a user is currently logged in.
	 *
	 * @return a Task that resolves to the logged-in User or null if no user is logged in
	 */
	public Task<User> checkLoggedIn() {
		TaskCompletionSource<User> tcs = new TaskCompletionSource<>();
		FirebaseUser fbUser = mAuth.getCurrentUser();
		if (fbUser != null) {
			get(fbUser.getUid(), "profilePicture", "profilePictureUrl")
				.addOnSuccessListener(user -> tcs.setResult(new User(user)))
				.addOnFailureListener(tcs::setException);
		} else {
			tcs.setResult(null);
		}
		return tcs.getTask();
	}

	/**
	 * Logs out the currently logged-in user.
	 *
	 * @return a Task that completes when the user is logged out
	 */
	public Task<Void> logOut() {
		TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
		mAuth.signOut();
		tcs.setResult(null);
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

			if (queryDocumentSnapshots.isEmpty()) {
				lvTopUsers.postValue(users);
				return;
			}

			processNextDocument(queryDocumentSnapshots.getDocuments(), 0, users);
		});

		return lvTopUsers;
	}

	/**
	 * Processes the next document in a list of Firestore documents.
	 * Loads the user's profile picture and adds the user to the list.
	 *
	 * @param documents the list of Firestore documents
	 * @param index     the current index in the list
	 * @param users     the list of users being built
	 */
	private void processNextDocument(java.util.List<DocumentSnapshot> documents, int index, Users users) {
		if (index >= documents.size()) {
			lvTopUsers.postValue(users);
			return;
		}

		DocumentSnapshot doc = documents.get(index);

		FirebaseImageStorage.loadFromStorage(doc.getId(), "images/" + getCollectionName())
			.addOnSuccessListener(picture -> {
				User user = doc.toObject(User.class);
				if (user != null) {
					user.setProfilePicture(picture);
					users.add(user);
				}

				processNextDocument(documents, index + 1, users);
			})
			.addOnFailureListener(e -> {
				User user = doc.toObject(User.class);
				users.add(user);
				processNextDocument(documents, index + 1, users);
			});
	}
}