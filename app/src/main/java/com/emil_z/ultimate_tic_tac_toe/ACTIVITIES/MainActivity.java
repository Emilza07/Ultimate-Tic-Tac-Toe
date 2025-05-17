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

/**
 * Main activity for the Ultimate Tic Tac Toe app.
 * <p>
 * Handles navigation to profile, leaderboard, settings, and game activities.
 * Manages user interactions for starting different game modes and handles back press events.
 */
public class MainActivity extends BaseActivity {
	public static final String EXTRA_GAME_TYPE = "com.emil_z.EXTRA_GAME_TYPE";
	public static final String EXTRA_SIGN = "com.emil_z.EXTRA_SIGN";
	public static final String EXTRA_GAME_ID_FS = "com.emil_z.EXTRA_GAME_ID_FS";

	private ImageView ivProfile;
	private ImageView ivLeaderboard;
	private ImageView ivSettings;
	private Button btnCPU;
	private Button btnLocal;
	private Button btnOnline;

	private UsersViewModel viewModel;
	private ActivityResultLauncher<Intent> gameLauncher;
	private ActivityResultLauncher<Intent> profileLauncher;

	/**
	 * Initializes the main activity, sets up UI, listeners, ViewModel, and activity result launchers.
	 *
	 * @param savedInstanceState The previously saved instance state, if any.
	 */
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

		initializeViews();
		setListeners();
		setViewModel();
		registerLaunchers();


	}

	/**
	 * Initializes view components for the main activity.
	 */
	@Override
	protected void initializeViews() {
		ivProfile = findViewById(R.id.ivProfile);
		ivLeaderboard = findViewById(R.id.ivLeaderboard);
		ivSettings = findViewById(R.id.ivSettings);
		btnCPU = findViewById(R.id.btnCpu);
		btnLocal = findViewById(R.id.btnLocal);
		btnOnline = findViewById(R.id.btnOnline);

		ivProfile.setImageBitmap(currentUser.getPictureBitmap());
	}

	/**
	 * Sets up click listeners for profile, leaderboard, settings, and game mode buttons.
	 * Handles back press with a confirmation dialog.
	 */
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
			//TODO: add an option to choose the level of the CPU (will do when the CPU will be better)
			AlertUtil.alert(this,
				getString(R.string.play_against_ai),
				getString(R.string.choose_your_sign),
				true,
				R.drawable.cpu_pfp,
				getString(R.string.cross),
				getString(R.string.nought),
				getString(R.string.random),
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
					getString(R.string.exit),
					getString(R.string.exit_confirmation),
					true,
					0,
					getString(R.string.yes),
					getString(R.string.no),
					null,
					() -> finish(),
					null,
					null);
			}
		});
	}

	/**
	 * Initializes the ViewModel and observes user data updates.
	 */
	@Override
	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(UsersViewModel.class);

		viewModel.getLiveDataEntity().observe(this, user -> {
			if (user != null) {
				currentUser = user;
			}
		});
	}

	/**
	 * Registers activity result launchers for game and profile activities.
	 */
	private void registerLaunchers() {
		gameLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
			o -> {
				GameType gameType = (GameType) o.getData().getSerializableExtra(EXTRA_GAME_TYPE);
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

	/**
	 * Starts a game activity with the specified game type and default sign.
	 *
	 * @param gameType The type of game to start.
	 */
	private void startGameActivity(GameType gameType) {
		startGameActivity(gameType, '-');
	}

	/**
	 * Starts a game activity with the specified game type and player sign.
	 *
	 * @param gameType The type of game to start.
	 * @param sign     The player's sign ('X', 'O', or '-').
	 */
	private void startGameActivity(GameType gameType, char sign) {
		Intent intent = new Intent(MainActivity.this, GameActivity.class);
		intent.putExtra(EXTRA_GAME_TYPE, gameType);
		intent.putExtra(EXTRA_SIGN, sign);
		gameLauncher.launch(intent);
	}
}