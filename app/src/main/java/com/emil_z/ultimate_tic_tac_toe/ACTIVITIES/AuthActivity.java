package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.BitMapHelper;
import com.emil_z.helper.NetworkUtil;
import com.emil_z.model.User;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;

/**
 * Activity that handles user authentication on application startup.
 * <p>
 * Provides login and registration options, and checks for existing sessions.
 */
public class AuthActivity extends BaseActivity {
	private Button btnLogin;
	private Button btnRegister;
	private TextView tvPlayOffline;
	private TextView tvUsingOffline;
	private UsersViewModel viewModel;


	/**
	 * Initializes the authentication activity, sets up the UI,
	 * and checks for existing login sessions.
	 *
	 * @param savedInstanceState The previously saved instance state, if any.
	 */
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

		initializeViews();
		setListeners();
		setViewModel();
		if (!NetworkUtil.isInternetAvailable(this))
			noInternet();
		else
			checkForLogIn();
	}

	/**
	 * Initializes view components and shows the login progress dialog.
	 */
	@Override
	protected void initializeViews() {
		btnLogin = findViewById(R.id.btnLogin);
		btnRegister = findViewById(R.id.btnRegister);
		tvPlayOffline = findViewById(R.id.tvPlayOffline);
		tvUsingOffline = findViewById(R.id.tvUsingOffline);

		showProgressDialog(getString(R.string.logging_in), "");
	}

	/**
	 * Sets up click listeners for login and registration buttons.
	 */
	@Override
	protected void setListeners() {
		btnLogin.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, LoginActivity.class)));
		btnRegister.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, Register1Activity.class)));
		tvPlayOffline.setOnClickListener(v -> {
			Intent intent = new Intent(AuthActivity.this, MainActivity.class);
			User offlineUser = new User("Player", BitMapHelper.encodeTobase64(BitmapFactory.decodeResource(getResources(), R.drawable.default_pfp)));
			offlineUser.setIdFs("offline_user");
			currentUser = offlineUser;
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		});
	}

	/**
	 * Initializes the ViewModel and sets up observers for authentication data.
	 */
	@Override
	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(UsersViewModel.class);

		viewModel.getLiveDataEntity().observe(this, user -> {
			if (user != null) {
				currentUser = user;
				Intent intent = new Intent(AuthActivity.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
			}
			hideProgressDialog();
		});
	}

	private void noInternet() {
		hideProgressDialog();
		btnLogin.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.buttonDisabledColor)));
		btnLogin.setTextColor(getColor(R.color.textDisabledColor));
		btnLogin.setClickable(false);
		btnRegister.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.buttonDisabledColor)));
		btnRegister.setTextColor(getColor(R.color.textDisabledColor));
		btnRegister.setClickable(false);
		tvUsingOffline.setVisibility(TextView.VISIBLE);
	}

	/**
	 * Checks if user has a valid session and attempts to log in automatically.
	 */
	private void checkForLogIn() {
		viewModel.checkLoggedIn();
	}
}