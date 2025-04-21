package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.emil_z.model.GameType;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;

public class HomeActivity extends BaseActivity {
	private Button btnLocal;
	private Button btnOnline;

	private UsersViewModel viewModel;
	private ActivityResultLauncher<Intent> launcher;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_home);
		ViewCompat.setOnApplyWindowInsetsListener(
			findViewById(R.id.main),
			(v, insets) -> {
				Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
				v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
				return insets;
		});

		setBottomNavigationVisibility(true);
		initializeViews();
		setListeners();
		setViewModel();
		launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
						new ActivityResultCallback<ActivityResult>() {
							@Override
							public void onActivityResult(ActivityResult o) {
								GameType gameType = (GameType) o.getData().getSerializableExtra(getString(R.string.EXTRA_GAME_TYPE));
								if (o.getResultCode() == RESULT_OK && gameType == GameType.Online) {
									viewModel.get(currentUser.getIdFs());
								}
							}
						}
				);
	}

	@Override
	protected void initializeViews() {
		btnLocal = findViewById(R.id.btnLocal);
		btnOnline = findViewById(R.id.btnOnline);
	}

	@Override
	protected void setListeners() {
		btnLocal.setOnClickListener(v -> {
			startGameActivity(GameType.LOCAL);
		});
		btnOnline.setOnClickListener(v -> {
			startGameActivity(GameType.Online);
		});
	}
	private void startGameActivity(GameType gameType) {


		Intent intent = new Intent(HomeActivity.this, GameActivity.class);
		intent.putExtra(getString(R.string.EXTRA_GAME_TYPE), gameType);
		launcher.launch(intent);
	}
	@Override
	protected void setViewModel() {
		viewModel = new UsersViewModel(getApplication());
		viewModel.getLiveDataEntity().observe(this, user -> {
			if (user != null) {
				currentUser = user;
			}
		});
	}
}