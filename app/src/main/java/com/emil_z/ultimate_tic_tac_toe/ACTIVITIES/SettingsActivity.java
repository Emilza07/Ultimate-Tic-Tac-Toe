package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.emil_z.helper.UserSessionPreference;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;

/**
 * Activity for managing user settings, including logging out.
 * <p>
 * Handles UI initialization, button listeners, and user session management.
 */
public class SettingsActivity extends BaseActivity {
	private Button btnLogOut;

	/**
	 * Initializes the settings activity, sets up UI, listeners, and window insets.
	 *
	 * @param savedInstanceState The previously saved instance state, if any.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		initializeViews();
		setListeners();
	}

	/**
	 * Initializes view components for the settings screen.
	 */
	@Override
	protected void initializeViews() {
		btnLogOut = findViewById(R.id.btnLogOut);
	}

	/**
	 * Sets up click listeners for the settings screen.
	 */
	@Override
	protected void setListeners() {
		btnLogOut.setOnClickListener(v -> logOut());
	}

	/**
	 * No ViewModel setup required for this activity.
	 */
	@Override
	protected void setViewModel() {
	}

	/**
	 * Logs out the current user by clearing session data and navigating to the authentication screen.
	 * Clears the activity stack to prevent navigation back to previous activities.
	 */
	private void logOut() {
		new UserSessionPreference(this).clearLoginCredentials();
		BaseActivity.currentUser = null;
		Intent intent = new Intent(this, AuthActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}
}