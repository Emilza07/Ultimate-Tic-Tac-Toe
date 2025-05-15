package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.UserSessionPreference;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;

public class AuthActivity extends BaseActivity {

	private Button btnLogin;
	private Button btnRegister;
	private UsersViewModel viewModel;
	private UserSessionPreference sessionPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		setBottomNavigationVisibility(false);

		sessionPreference = new UserSessionPreference(this);

		initializeViews();
		setListeners();
		setViewModel();
		checkForLogIn();
	}

	@Override
	protected void initializeViews() {
		btnLogin = findViewById(R.id.btnLogin);
		btnRegister = findViewById(R.id.btnRegister);
		showProgressDialog("Login", "Logging in...");
	}

	@Override
	protected void setListeners() {
		btnLogin.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, LoginActivity.class)));
		btnRegister.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, RegisterActivity.class)));
	}

	@Override
	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(UsersViewModel.class);

		viewModel.getLiveDataEntity().observe(this, user -> {
			if (user != null) {
				String storedEmail = sessionPreference.getEmail();
				String storedPassword = sessionPreference.getPassword();
				if (user.getUsername().equals(storedEmail) &&
						user.getHashedPassword().equals(storedPassword)) {
					currentUser = user;
					Intent intent = new Intent(AuthActivity.this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
				}
			}
			hideProgressDialog();
		});
	}

	private void checkForLogIn() {
		// Check if we have a valid session with token
		if (sessionPreference.hasValidSession()) {
			// We have a valid session, retrieve the user
			String userIdFs = sessionPreference.getUserIdFs();
			viewModel.get(userIdFs);
		} else {
			hideProgressDialog();
		}
	}
}