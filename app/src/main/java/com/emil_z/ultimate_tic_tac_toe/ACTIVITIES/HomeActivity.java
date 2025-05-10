package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.emil_z.helper.AlertUtil;
import com.emil_z.model.GameType;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;

import java.util.Random;

public class HomeActivity extends BaseActivity {
	private Button btnCPU;
	private Button btnLocal;
	private Button btnOnline;
	private ImageButton iBtnProfile;

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
								if (gameType == GameType.Online && o.getResultCode() == RESULT_OK) {
									viewModel.get(currentUser.getIdFs());
								}
							}
						}
				);
	}

	@Override
	protected void initializeViews() {
		btnCPU = findViewById(R.id.btnCpu);
		btnLocal = findViewById(R.id.btnLocal);
		btnOnline = findViewById(R.id.btnOnline);
		iBtnProfile = findViewById(R.id.iBtnProfile);
	}

	@Override
	protected void setListeners() {
		iBtnProfile.setOnClickListener(v -> {
			Intent intent = new Intent(HomeActivity.this, UserActivity.class);
			startActivity(intent);
		});
		btnCPU.setOnClickListener(v -> {
			//TODO: add an option to choose the sign here instead of game activity because of player names
			//TODO: add an option to choose the level of the CPU
			AlertUtil.alert(this,
					"Start game",
					"choose your sign",
					false,
					0,
					"Cross",
					"Nought",
					"Random",
					() -> {
						startCpuActivity(GameType.CPU, 'X');
					},
					() -> {
						startCpuActivity(GameType.CPU, 'O');
					},
					() -> {
							startCpuActivity(GameType.CPU, new Random().nextBoolean() ? 'X' : 'O');
					});

		});
		btnLocal.setOnClickListener(v -> {
			startGameActivity(GameType.LOCAL);
		});
		btnOnline.setOnClickListener(v -> {
			startGameActivity(GameType.Online);
		});
	}

	private void startCpuActivity(GameType gameType, char sign) {
		Intent intent = new Intent(HomeActivity.this, GameActivity.class);
		intent.putExtra(getString(R.string.EXTRA_GAME_TYPE), gameType);
		intent.putExtra(getString(R.string.EXTRA_SIGN), sign);
		launcher.launch(intent);
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