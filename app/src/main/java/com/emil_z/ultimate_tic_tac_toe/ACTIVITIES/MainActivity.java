package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.AlertUtil;
import com.emil_z.model.GameType;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;

import java.util.Random;

public class MainActivity extends BaseActivity {
	private Button btnCPU;
	private Button btnLocal;
	private Button btnOnline;
	private ImageView ivProfile;
	private ImageView ivLeaderboard;
	private ImageView ivSettings;

	private UsersViewModel viewModel;
	private ActivityResultLauncher<Intent> gameLauncher;
	private ActivityResultLauncher<Intent> profileLauncher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ViewCompat.setOnApplyWindowInsetsListener(
				findViewById(R.id.main),
				(v, insets) -> {
					Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
					v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
					return insets;
				});

		//setBottomNavigationVisibility(true);
		initializeViews();
		setListeners();
		setViewModel();
		registerLaunchers();


	}

	@Override
	protected void initializeViews() {
		btnCPU = findViewById(R.id.btnCpu);
		btnLocal = findViewById(R.id.btnLocal);
		btnOnline = findViewById(R.id.btnOnline);
		ivProfile = findViewById(R.id.ivProfile);
		ivProfile.setImageBitmap(currentUser.getPictureBitmap());
		ivLeaderboard = findViewById(R.id.ivLeaderboard);
		ivSettings = findViewById(R.id.ivSettings);
	}

	@Override
	protected void setListeners() {
		ivProfile.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
			profileLauncher.launch(intent);
		});
		ivLeaderboard.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
			startActivity(intent);
		});
		ivSettings.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
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
					() -> startGameActivity(GameType.CPU, 'X'),
					() -> startGameActivity(GameType.CPU, 'O'),
					() -> startGameActivity(GameType.CPU, new Random().nextBoolean() ? 'X' : 'O'));

		});
		btnLocal.setOnClickListener(v -> startGameActivity(GameType.LOCAL));
		btnOnline.setOnClickListener(v -> startGameActivity(GameType.ONLINE));

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				AlertUtil.alert(MainActivity.this,
						"Exit",
						"Are you sure you want to exit?",
						true,
						0,
						"Yes",
						"No",
						null,
						() -> finish(),
						null,
						null);
			}
		});
	}

	@Override
	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(UsersViewModel.class);
		viewModel.getLiveDataEntity().observe(this, user -> {
			if (user != null) {
				currentUser = user;
			}
		});
	}

	@SuppressWarnings("ConstantConditions")
	private void registerLaunchers() {
		gameLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
				o -> {
					GameType gameType = (GameType) o.getData().getSerializableExtra(getString(R.string.EXTRA_GAME_TYPE));
					if (gameType == GameType.ONLINE && o.getResultCode() == RESULT_OK) {
						viewModel.get(currentUser.getIdFs());
					}
				}
		);
		profileLauncher = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				result -> {
					if (result.getResultCode() == RESULT_OK) {
						ivProfile.setImageBitmap(currentUser.getPictureBitmap());
					}
				}
		);
	}

	private void startGameActivity(GameType gameType, char sign) {
		Intent intent = new Intent(MainActivity.this, GameActivity.class);
		intent.putExtra(getString(R.string.EXTRA_GAME_TYPE), gameType);
		intent.putExtra(getString(R.string.EXTRA_SIGN), sign);
		gameLauncher.launch(intent);
	}

	private void startGameActivity(GameType gameType) {
		startGameActivity(gameType, '-');
	}


}