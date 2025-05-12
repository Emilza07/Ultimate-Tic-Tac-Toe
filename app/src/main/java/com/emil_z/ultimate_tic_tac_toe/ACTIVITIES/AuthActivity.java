package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.emil_z.helper.PreferenceManager;
import com.emil_z.ultimate_tic_tac_toe.R;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.viewmodel.UsersViewModel;

import java.util.Objects;

public class AuthActivity extends BaseActivity {

	private Button btnLogin;
	private Button btnRegister;
	private UsersViewModel viewModel;
	Object[][] prefsResult;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		//setContentView(R.layout.activity_main);
		getLayoutInflater().inflate(R.layout.activity_auth, findViewById(R.id.content_frame));
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		setBottomNavigationVisibility(false);
		initializeViews();
		setListeners();
		setViewModel();
		checkForLogIn();
	}

	@Override
	protected void initializeViews() {
		btnLogin	= findViewById(R.id.btnLogin);
		btnRegister = findViewById(R.id.btnRegister);
		showProgressDialog("Login", "Logging in...");
	}

	@Override
	protected void setListeners() {
		btnLogin.setOnClickListener(v -> {
			startActivity(new Intent(AuthActivity.this, LoginActivity.class));
		});
		btnRegister.setOnClickListener(v -> {
			startActivity(new Intent(AuthActivity.this, RegisterActivity.class));
		});
	}

	@Override
	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(UsersViewModel.class);

		viewModel.getLiveDataEntity().observe(this, user -> {
			if (user != null) {
				if(Objects.equals(user.getUsername(),prefsResult[1][1]) && Objects.equals(user.getPassword(), prefsResult[2][1])){
					currentUser = user;
					Intent intent = new Intent(AuthActivity.this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
				}
				hideProgressDialog();
			}
		});
	}

	private void checkForLogIn(){
		prefsResult = PreferenceManager.readFromSharedPreferences(this, "user_prefs",
				new Object[][]{{"UserIdFs", "String"}, {"Username", "String"}, {"Password", "String"}});
		String idFs;
		if (prefsResult != null && prefsResult[0] != null && prefsResult[0][1] != null && prefsResult[2][1] != null) {
			idFs = prefsResult[0][1].toString();
			viewModel.get(idFs);
		}
		else {
			hideProgressDialog();
		}
	}
}