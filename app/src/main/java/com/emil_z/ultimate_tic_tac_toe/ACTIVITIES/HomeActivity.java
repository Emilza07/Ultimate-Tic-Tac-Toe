package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;

public class HomeActivity extends BaseActivity {
	private Button btnLocal;
	private Button btnHost;
	private Button btnJoin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_home);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		setBottomNavigationVisibility(true);

		initializeViews();
		setListeners();
	}

	@Override
	protected void initializeViews() {
		btnLocal = findViewById(R.id.btnLocal);
		btnHost = findViewById(R.id.btnHost);
		btnJoin = findViewById(R.id.btnJoin);
	}

	@Override
	protected void setListeners() {
		btnLocal.setOnClickListener(v -> {
			Intent intent = new Intent(HomeActivity.this, GameActivity.class);
			intent.putExtra(getString(R.string.EXTRA_GAME_TYPE), getResources().getStringArray(R.array.game_types)[1]);
			startActivity(intent);
		});
		btnHost.setOnClickListener(v -> {
			Intent intent = new Intent(HomeActivity.this, GameActivity.class);
			intent.putExtra(getString(R.string.EXTRA_GAME_TYPE), getResources().getStringArray(R.array.game_types)[2]);
		});
		btnJoin.setOnClickListener(v -> {
			Intent intent = new Intent(HomeActivity.this, GameActivity.class);
			intent.putExtra(getString(R.string.EXTRA_GAME_TYPE), getResources().getStringArray(R.array.game_types)[3]);
		});
	}

	@Override
	protected void setViewModel() {}
}