package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.emil_z.helper.PreferenceManager;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;

public class SettingsActivity extends BaseActivity {
	private Button btnLogOut;

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

	@Override
	protected void initializeViews() {
		btnLogOut = findViewById(R.id.btnLogOut);
	}

	@Override
	protected void setListeners() {
		btnLogOut.setOnClickListener(v -> {
			PreferenceManager.writeToSharedPreferences(SettingsActivity.this, "user_prefs", new Object[][]{{"UserIdFs", null, "String"}, {"Username", null, "String"}, {"Password", null, "String"}});

			BaseActivity.currentUser = null;
			Intent intent = new Intent(SettingsActivity.this, AuthActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		});
	}

	@Override
	protected void setViewModel() {
	}
}