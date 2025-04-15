package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.gridlayout.widget.GridLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.AlertUtil;
import com.emil_z.model.GameType;
import com.emil_z.model.Player;
import com.emil_z.model.PlayerType;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.GamesViewModel;

public class GameActivity extends BaseActivity {

	private int boardSize;
	private float conversionFactor;
	private GridLayout gridBoard;

	private ConstraintLayout clLoading;
	private Button btnAbort;
	private TextView tvP1Name;
	private TextView tvP1Elo;
	private TextView tvP2Name;
	private TextView tvP2Elo;

	private GamesViewModel viewModel;
	GameType gameType;
	String[] errorCodes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_game);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		initializeViews();
		setListeners();
		setViewModel();
		gameInit(gameType);
	}

	protected void initializeViews() {
		errorCodes = getResources().getStringArray(R.array.error_codes);
		Intent intent = getIntent();
		gameType = (GameType) intent.getSerializableExtra(getString(R.string.EXTRA_GAME_TYPE));

		clLoading = findViewById(R.id.clLoading);
		btnAbort = findViewById(R.id.btnAbort);

		tvP1Name = findViewById(R.id.tvP1Name);
		tvP1Elo = findViewById(R.id.tvP1Elo);
		tvP2Name = findViewById(R.id.tvP2Name);
		tvP2Elo = findViewById(R.id.tvP2Elo);

		createBoard();
	}

	protected void setListeners() {
		btnAbort.setOnClickListener(v -> {
			if (gameType.equals(GameType.Online)) {
				viewModel.removeGame();
				Toast.makeText(this, "Online game aborted", Toast.LENGTH_SHORT).show();
			}
			finish();
		});
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				AlertUtil.alert(
						GameActivity.this,
						viewModel.getLvGame().getValue().isStarted() ? "Resign" : "Abort",
						"Are you sure you want to exit the game?",
						true,
						0,
						"Yes",
						"No",
						null,
						(() -> {
							if (gameType == GameType.Online) {
								viewModel.removeGame();
							}
							finish();
						}),
						null,
						null
				);
			}
		});
	}

	private void handleBoardButtonClick(ImageView btn) {
		String tag = (String) btn.getTag();
		// Handle button click using the tag (e.g., "btn0101" for row 3, col 4)
		Toast.makeText(this, "Button clicked: " + tag, Toast.LENGTH_SHORT).show();
		viewModel.makeMove(tag.charAt(3) - '0', tag.charAt(4) - '0', tag.charAt(5) - '0', tag.charAt(6) - '0');
		viewModel.getLvCode().observe(this, new Observer<Integer>() {
			@Override
			public void onChanged(Integer code) {
				if (code != null) {
					if(code == 0)
						btn.setImageResource(viewModel.getLvGame().getValue().getOuterBoard().getCurrentPlayer() == 'O' ? R.drawable.x : R.drawable.o);
					else
						Toast.makeText(GameActivity.this, errorCodes[code], Toast.LENGTH_SHORT).show();
					viewModel.resetLvCode();
					viewModel.getLvCode().removeObserver(this);
					}
				}
		});
	}

	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(GamesViewModel.class);

		viewModel.getSuccess().observe(this, success -> {
			//if LocalGame
			if (success) {
				switch (gameType) {
					case Online:
						if (viewModel.getLvGame() == null) {
							Toast.makeText(GameActivity.this, "Game removed successfully", Toast.LENGTH_SHORT).show();
						}
						break;
				}
			}
		});

		viewModel.getLvGame().observe(this, game -> {
			if (game == null)
				finish();
		});

		viewModel.getLvOuterBoardWinners().observe(this, new Observer<char[][]>() {
			@Override
			public void onChanged(char[][] chars) {
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						if (chars[i][j] != 0) {
							// Set the background of the outer board to indicate the winner
							gridBoard.getChildAt(i * 3 + j).setBackground(ResourcesCompat.getDrawable(getResources(), chars[i][j] == 'X' ? R.drawable.x : R.drawable.o, null));
							//((GridLayout) gridBoard.getChildAt(1)).getChildAt(1)
						}
					}
				}
			}
		});

		viewModel.getLvIsFinished().observe(this, new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
				AlertUtil.alert(GameActivity.this,
						"Game Over",
						 "Player " + viewModel.getLvGame().getValue().getWinner() + " wins!",
						false,
						0,
						"Return",
						null,
						null,
						() -> finish(),
						null,
						null);
			}
		});

		viewModel.getLvIsStarted().observe(this, new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
				if (aBoolean) {
					// Game started
					Toast.makeText(GameActivity.this, "Game started", Toast.LENGTH_SHORT).show();
					clLoading.setVisibility(View.GONE);
					gridBoard.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.board, null));
					setPlayers(viewModel.getLvGame().getValue().getPlayer1(), viewModel.getLvGame().getValue().getPlayer2());
				}
			}
		});
	}

	//region GameInit
	private void gameInit(GameType gameType) {
		switch (gameType) {
			case CPU:
				// Initialize SP game
				break;
			case LOCAL:
				// Initialize local game
				viewModel.startLocalGame();
				break;
			case Online:
				// Initialize online game as joiner
				try {
					viewModel.startOnlineGame(new Player(currentUser));
					setPlayers(new Player(currentUser), null);
				} catch (Exception e) {
					Toast.makeText(this, "Error starting game: " + e.getMessage() + " Try again", Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}

	private void setPlayers(Player p1, Player p2) {
		if (gameType == GameType.CPU || gameType == GameType.LOCAL) {
			// For local games, display as is
			tvP1Name.setText(p1.getName());
			tvP1Elo.setText("");
			tvP2Name.setText(p2.getName());
			tvP2Elo.setText("");
		} else {
			// For online games
			if(!viewModel.getLvIsStarted().getValue())
			{
				tvP1Name.setText(currentUser.getName());
				tvP1Elo.setText("(" + currentUser.getElo() + ")");
				tvP2Name.setText( "Waiting for opponent...");
				tvP2Elo.setText("");
			}
			else {
				boolean isHost = viewModel.getLvGame().getValue().getPlayer1().getPlayerType() == PlayerType.LOCAL;
				if (isHost) {
					tvP2Name.setText(p2.getName());
					tvP2Elo.setText("(" + p2.getElo() + ")");
				} else {
					tvP2Name.setText(p1.getName());
					tvP2Elo.setText("(" + p1.getElo() + ")");
				}
			}
		}
	}

	//region BoardInitialization
	private void createBoard() {
		final float BOARD_DRAWABLE_SIZE = 653;
		boardSize = getBoardSize();
		conversionFactor = boardSize / BOARD_DRAWABLE_SIZE;

		gridBoard= findViewById(R.id.gridBoard);
		setOuterGrid(gridBoard);
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				gridBoard.addView(createInnerGrid(row, col));
			}
		}
	}

	private GridLayout createInnerGrid(int row, int col) {
		GridLayout grid = new GridLayout(this);
		setInnerGrid(row, col, grid);
		for (int iRow = 0; iRow < 3; iRow++) {
			for (int iCol = 0; iCol < 3; iCol++){
				grid.addView(createBoardButton(row, col, iRow, iCol));
			}
		}
		return grid;
	}

	private ImageView createBoardButton(int row, int col, int iRow, int iCol) {
		final float INNER_CELL_DRAWABLE_SIZE = 55;
		final int INNER_CELL_DRAWABLE_REF_SIZE = (int) (INNER_CELL_DRAWABLE_SIZE * conversionFactor);
		final int MARGIN = (int) (1 * conversionFactor);
		final int PADDING = (int) (7 * conversionFactor);
		ImageView btn = new ImageView(this);
		btn.setId(View.generateViewId());
		btn.setLayoutParams(new GridLayout.LayoutParams());
		btn.setScaleType(ImageButton.ScaleType.FIT_XY);
		btn.setForegroundGravity(ImageButton.TEXT_ALIGNMENT_CENTER);
		btn.setClickable(true);
		GridLayout.LayoutParams btnParams = new GridLayout.LayoutParams();
		btnParams.width = INNER_CELL_DRAWABLE_REF_SIZE;
		btnParams.height = INNER_CELL_DRAWABLE_REF_SIZE;
		btn.setPadding(PADDING, PADDING, PADDING, PADDING);
		btnParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
		btnParams.rowSpec = GridLayout.spec(iRow);
		btnParams.columnSpec = GridLayout.spec(iCol);
		btn.setLayoutParams(btnParams);
		btn.setOnClickListener(v -> handleBoardButtonClick((ImageView) v));
		btn.setTag("btn" + row + col +  iRow +  iCol); // Set a tag for identification
		return btn;
	}

	private void setOuterGrid(GridLayout gridLayout) {
		final int PADDING = (int) (21 * conversionFactor);
		gridLayout.setPadding(PADDING, PADDING, PADDING, PADDING);
		gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
		ViewGroup.LayoutParams params = gridLayout.getLayoutParams();
		params.width = boardSize;
		params.height = boardSize;
		gridLayout.setLayoutParams(params);
	}

	private void setInnerGrid(int row, int col, GridLayout grid) {
		final int INNER_BOARD_DRAWABLE_SIZE = 167;
		final int INNER_BOARD_DRAWABLE_REF_SIZE = (int) (INNER_BOARD_DRAWABLE_SIZE * conversionFactor);
		final int MARGIN = (int) (18.5f * conversionFactor);
		final int PADDING = (int) (1 * conversionFactor);
		grid.setId(View.generateViewId());
		grid.setColumnCount(3);
		grid.setRowCount(3);
		grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
		grid.setPadding(PADDING, PADDING, PADDING, PADDING);
		GridLayout.LayoutParams params = new GridLayout.LayoutParams();
		params.width = INNER_BOARD_DRAWABLE_REF_SIZE;
		params.height = INNER_BOARD_DRAWABLE_REF_SIZE;
		params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
		params.rowSpec = GridLayout.spec(row);
		params.columnSpec = GridLayout.spec(col);
		grid.setLayoutParams(params);
	}

	private int getBoardSize() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int screenWidth = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
		return (int) (screenWidth * 0.9);
	}
	//endregion
	//endregion
}