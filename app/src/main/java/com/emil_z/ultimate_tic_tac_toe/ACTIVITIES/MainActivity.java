package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.emil_z.ultimate_tic_tac_toe.R;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;

public class MainActivity extends BaseActivity {

	private Button btnLogin;
	private Button btnRegister;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		//setContentView(R.layout.activity_main);
		getLayoutInflater().inflate(R.layout.activity_main, findViewById(R.id.content_frame));
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		setBottomNavigationVisibility(false);
		initializeViews();
	}

	@Override
	protected void initializeViews() {
		btnLogin	= findViewById(R.id.btnLogin);
		btnRegister = findViewById(R.id.btnRegister);

		setListeners();
	}

	@Override
	protected void setListeners() {
		btnLogin.setOnClickListener(v -> {
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
		});
		btnRegister.setOnClickListener(v -> {
			startActivity(new Intent(MainActivity.this, RegisterActivity.class));
		});
	}

	@Override
	protected void setViewModel() {

	}
}