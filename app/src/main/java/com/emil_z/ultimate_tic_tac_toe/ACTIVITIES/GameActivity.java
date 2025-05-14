package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.AlertUtil;
import com.emil_z.model.BoardLocation;
import com.emil_z.model.Game;
import com.emil_z.model.GameType;
import com.emil_z.model.OuterBoard;
import com.emil_z.model.Player;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.ultimate_tic_tac_toe.SERVICES.AppMonitorService;
import com.emil_z.viewmodel.GamesViewModel;
import com.emil_z.viewmodel.GamesViewModelFactory;
import com.emil_z.viewmodel.UsersViewModel;

import java.util.Objects;

public class GameActivity extends BaseActivity {

	GameType gameType;
	String[] errorCodes;
	private int boardSize;
	private float conversionFactor;
	private GridLayout gridBoard;
	private ConstraintLayout clLoading;
	private Button btnAbort;
	private LinearLayout llP1;
	private ImageView ivP1Pfp;
	private TextView tvP1Name;
	private TextView tvP1Elo;
	private TextView tvP1Sign;
	private LinearLayout llP2;
	private ImageView ivP2Pfp;
	private TextView tvP2Name;
	private TextView tvP2Elo;
	private TextView tvP2Sign;
	private TextView tvCurrentPlayer;
	private LinearLayout llReview;
	private Button btnForward;
	private Button btnBackward;
	private int moveIndex = 0;
	private GamesViewModel gamesViewModel;
	private UsersViewModel usersViewModel;
	private char[][] outerBoardState;
	private boolean monitorServiceStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
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
		tvCurrentPlayer = findViewById(R.id.tvCurrentPlayer);

		llReview = findViewById(R.id.llReview);
		btnForward = findViewById(R.id.btnForward);
		btnBackward = findViewById(R.id.btnBackward);

		clLoading = findViewById(R.id.clLoading);
		btnAbort = findViewById(R.id.btnAbort);

		llP1 = findViewById(R.id.llP1);
		ivP1Pfp = findViewById(R.id.ivP1Pfp);
		tvP1Name = findViewById(R.id.tvP1Name);
		tvP1Elo = findViewById(R.id.tvP1Elo);
		tvP1Sign = findViewById(R.id.tvP1Sign);
		llP2 = findViewById(R.id.llP2);
		ivP2Pfp = findViewById(R.id.ivP2Pfp);
		tvP2Name = findViewById(R.id.tvP2Name);
		tvP2Elo = findViewById(R.id.tvP2Elo);
		tvP2Sign = findViewById(R.id.tvP2Sign);

		createBoard();
		outerBoardState = new char[3][3];
	}

	@SuppressWarnings("ConstantConditions")
	protected void setListeners() {
		btnAbort.setOnClickListener(v -> {
			if (gameType.equals(GameType.ONLINE)) {
				gamesViewModel.exitGame();
				Toast.makeText(this, "Online game aborted", Toast.LENGTH_SHORT).show();
			}
			Intent intent = new Intent();
			setResult(RESULT_CANCELED, intent);
			finish();
		});
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (gameType == GameType.HISTORY) {
					finish();
					return;
				}
				Game game = gamesViewModel.getLiveDataGame().getValue();
				AlertUtil.alert(
						GameActivity.this,
						(game != null && game.isStarted()) ? "Resign" : "Abort",
						"Are you sure you want to exit the game?",
						true,
						0,
						"Yes",
						"No",
						null,
						(() -> {
							Intent intent = new Intent();
							if (gameType == GameType.ONLINE)
								gamesViewModel.exitGame();
							setResult((game != null && game.getMoves().isEmpty()) ? RESULT_OK : RESULT_CANCELED, intent);
							intent.putExtra(getString(R.string.EXTRA_GAME_TYPE), gameType);
							if (gameType != GameType.ONLINE || game == null)
								finish();
						}),
						null,
						null
				);
			}
		});

		btnForward.setOnClickListener(v -> {
			Game game = gamesViewModel.getLiveDataGame().getValue();

			if (moveIndex >= game.getMoves().size()) {
				Toast.makeText(GameActivity.this, "No more moves to review", Toast.LENGTH_SHORT).show();
				return;
			}

			// Reset all inner board visuals to ensure clean state
			for (int i = 0; i < gridBoard.getChildCount(); i++) {
				gridBoard.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
			}

			// Make the current move
			BoardLocation lastMove = game.getMoves().get(moveIndex);
			int innerGridIndex = lastMove.getOuter().x * 3 + lastMove.getOuter().y;
			int btnIndex = lastMove.getInner().x * 3 + lastMove.getInner().y;
			GridLayout innerGrid = (GridLayout) gridBoard.getChildAt(innerGridIndex);
			ImageView btn = (ImageView) innerGrid.getChildAt(btnIndex);
			btn.setImageResource(moveIndex % 2 == 1 ? R.drawable.o : R.drawable.x);

			// Rebuild game state up to this point to check for inner board winners
			rebuildGameStateToMove(moveIndex + 1);

			// Check next move highlighting
			if (moveIndex < game.getMoves().size() - 1) {
				// The next grid is determined by the inner position of the current move
				GridLayout nextGrid = (GridLayout) gridBoard.getChildAt(btnIndex);

				// Check if the next grid is already won or full (would be a free move)
				boolean isNextGridPlayable = outerBoardState[btnIndex / 3][btnIndex % 3] == 0;

				if (isNextGridPlayable) {
					// Highlight the specific grid
					nextGrid.setBackgroundResource(R.drawable.border);
				}
			}

			tvCurrentPlayer.setText(moveIndex % 2 == 1 ? R.string.player_x_turn : R.string.player_o_turn);
			moveIndex++;
		});

		btnBackward.setOnClickListener(v -> {
			moveIndex--;
			if (moveIndex < 0) {
				Toast.makeText(GameActivity.this, "No more moves to review", Toast.LENGTH_SHORT).show();
				moveIndex = 0;
				return;
			}

			Game game = gamesViewModel.getLiveDataGame().getValue();

			// Clear all board state
			resetBoardDisplay();

			// Rebuild game state up to current moveIndex
			rebuildGameStateToMove(moveIndex);

			// Apply highlights for next move if needed
			if (moveIndex > 0) {
				BoardLocation previousMove = game.getMoves().get(moveIndex - 1);
				int prevInnerPos = previousMove.getInner().x * 3 + previousMove.getInner().y;

				boolean isNextGridPlayable = outerBoardState[prevInnerPos / 3][prevInnerPos % 3] == 0;

				if (isNextGridPlayable) {
					GridLayout nextGrid = (GridLayout) gridBoard.getChildAt(prevInnerPos);
					nextGrid.setBackgroundResource(R.drawable.border);
				}
			}

			tvCurrentPlayer.setText(moveIndex % 2 == 0 ? R.string.player_x_turn : R.string.player_o_turn);
		});
	}

	private void handleBoardButtonClick(ImageView btn) {
		String tag = (String) btn.getTag();
		// Handle button click using the tag (e.g., "btn0101" for row 3, col 4)
		gamesViewModel.makeMove(new BoardLocation(tag.charAt(3) - '0', tag.charAt(4) - '0', tag.charAt(5) - '0', tag.charAt(6) - '0'));
	}

	@SuppressWarnings("ConstantConditions")
	protected void setViewModel() {
		gamesViewModel = new ViewModelProvider(this,
				new GamesViewModelFactory(getApplication(), gameType))
				.get(GamesViewModel.class);
		usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);

		gamesViewModel.getLiveDataCode().observe(this, code -> {
			if (code != 0) {
				Toast.makeText(GameActivity.this, errorCodes[code], Toast.LENGTH_SHORT).show();
				gamesViewModel.resetLvCode();
			}
		});

		gamesViewModel.getLiveDataGame().observe(this, game -> {
			if (game == null) {
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				finish();
			} else if (!game.getMoves().isEmpty() && !game.isFinished()) {
				if (game.getMoves().size() > 1) {
					int previewsMoveIndex = game.getMoves().get(game.getMoves().size() - 1).getOuter().x * 3 + game.getMoves().get(game.getMoves().size() - 1).getOuter().y;
					GridLayout prevInnerGrid = (GridLayout) gridBoard.getChildAt(previewsMoveIndex);
					prevInnerGrid.setBackgroundColor(Color.TRANSPARENT);
				}
				BoardLocation lastMove = game.getMoves().get(game.getMoves().size() - 1);
				int innerGridIndex = lastMove.getOuter().x * 3 + lastMove.getOuter().y;
				int btnIndex = lastMove.getInner().x * 3 + lastMove.getInner().y;
				GridLayout innerGrid = (GridLayout) gridBoard.getChildAt(innerGridIndex);
				ImageView btn = (ImageView) innerGrid.getChildAt(btnIndex);
				btn.setImageResource(gamesViewModel.getLiveDataGame().getValue().getOuterBoard().getCurrentPlayer() == 'O' ? R.drawable.x : R.drawable.o);
				if (!game.getOuterBoard().isFreeMove()) {
					GridLayout nextMoveGrid = (GridLayout) gridBoard.getChildAt(btnIndex);
					nextMoveGrid.setBackgroundResource(R.drawable.border);

				}
				tvCurrentPlayer.setText(gamesViewModel.getLiveDataGame().getValue().getOuterBoard().getCurrentPlayer() == 'X' ? R.string.player_x_turn : R.string.player_o_turn);


			}
		});

		gamesViewModel.getLiveDataOuterBoardWinners().observe(this, chars -> {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if (chars[i][j] != 0) {
						GridLayout innerGrid = (GridLayout) gridBoard.getChildAt(i * 3 + j);
						// Set the background of the outer board to indicate the winner
						if (chars[i][j] == 'X') {
							innerGrid.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.x_blurred, null));
						} else if (chars[i][j] == 'O') {
							innerGrid.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.o_blurred, null));
						} else {
							innerGrid.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.tie, null));
						}

						for (int k = 0; k < innerGrid.getChildCount(); k++) {
							View child = innerGrid.getChildAt(k);
							child.setAlpha(0.05f);
						}
					}
				}
			}
		});

		gamesViewModel.getLiveDataIsFinished().observe(this, aBoolean -> {
			String winner;

			switch (gameType) {
				case CPU:
				case LOCAL:
					winner = gamesViewModel.getLiveDataGame().getValue().getWinnerIdFs();
					break;
				case ONLINE:
					winner = Objects.equals(gamesViewModel.getLiveDataGame().getValue().getWinnerIdFs(), gamesViewModel.getLiveDataGame().getValue().getCrossPlayerIdFs()) ? "X" : "O";
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + gameType);
			}
			AlertUtil.alert(GameActivity.this,
					"Game Over",
					(!Objects.equals(gamesViewModel.getLiveDataGame().getValue().getWinnerIdFs(), "T")) ? "Player " + winner + " wins!" : "Game is a tie!",
					false,
					0,
					"Return",
					null,
					null,
					(() -> {
						Intent intent = new Intent();
						intent.putExtra(getString(R.string.EXTRA_GAME_TYPE), gameType);
						setResult(RESULT_OK, intent);
						finish();
					}),
					null,
					null);
		});

		gamesViewModel.getLiveDataIsStarted().observe(this, aBoolean -> {
			if (aBoolean) {
				Game game = gamesViewModel.getLiveDataGame().getValue();
				if (gameType == GameType.ONLINE || gameType == GameType.HISTORY)
					usersViewModel.get(Objects.equals(game.getPlayer1().getIdFs(), currentUser.getIdFs()) ? game.getPlayer2().getIdFs() : game.getPlayer1().getIdFs());
				else {
					setPlayers(game.getPlayer1(), game.getPlayer2());
					clLoading.setVisibility(View.GONE);
					tvCurrentPlayer.setVisibility(View.VISIBLE);
					gridBoard.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.board, null));
				}

				if (gameType == GameType.ONLINE) {
					if (!monitorServiceStarted) {
						monitorServiceStarted = true;
						AppMonitorService.startService(
								this,
								true,
								game.getIdFs(),
								game.getPlayer1().getIdFs(),
								game.getPlayer2().getIdFs(),
								Objects.equals(game.getPlayer1().getIdFs(), currentUser.getIdFs())
						);
					} else {
						AppMonitorService.updateGameState(
								true,
								game.getIdFs(),
								game.getPlayer1().getIdFs(),
								game.getPlayer2().getIdFs(),
								Objects.equals(game.getPlayer1().getIdFs(), currentUser.getIdFs())
						);
					}
				}
			}
		});

		gamesViewModel.getLiveDataGameIdFs().observe(this, gameIdFs -> {
			if (gameIdFs != null && !monitorServiceStarted) {
				monitorServiceStarted = true;
				AppMonitorService.startService(
						this,
						true,
						gameIdFs,
						null,
						null,
						false
				);
			}
		});

		gamesViewModel.getLiveDataEntity().observe(this, game -> gamesViewModel.startHistoryGame(game));

		usersViewModel.getLiveDataEntity().observe(this, user -> {
			clLoading.setVisibility(View.GONE);
			tvCurrentPlayer.setVisibility(View.VISIBLE);
			gridBoard.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.board, null));
			gridBoard.setVisibility(View.VISIBLE);
			if (user != null) {
				if (Objects.equals(gamesViewModel.getLiveDataGame().getValue().getPlayer1().getIdFs(), currentUser.getIdFs()))
					setPlayers(new Player(currentUser), new Player(user));
				else
					setPlayers(new Player(user), new Player(currentUser));
			} else
				setPlayers(gamesViewModel.getLiveDataGame().getValue().getPlayer1(), gamesViewModel.getLiveDataGame().getValue().getPlayer2());
			if (gameType == GameType.HISTORY)
				llReview.setVisibility(View.VISIBLE);
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// When activity is destroyed normally, we don't want exitGame to run on task removed
		AppMonitorService.userClosedActivity(this);
	}

	//region GameInit
	private void gameInit(GameType gameType) {
		Intent intent = getIntent();
		switch (gameType) {
			case CPU:
				// Initialize SP game
				char sign = intent.getCharExtra(getString(R.string.EXTRA_SIGN), 'X');
				gamesViewModel.startCpuGame(sign == 'X' ? currentUser.getIdFs() : "CPU");
				break;
			case LOCAL:
				// Initialize local game
				gamesViewModel.startLocalGame();
				break;
			case ONLINE:
				// Initialize online game as joiner
				try {
					clLoading.setVisibility(View.VISIBLE);
					gamesViewModel.startOnlineGame(new Player(currentUser));
					setPlayers(new Player(currentUser), null);
				} catch (Exception e) {
					Toast.makeText(this, "Error starting game: " + e.getMessage() + " Try again", Toast.LENGTH_SHORT).show();
				}
				break;
			case HISTORY:
				gridBoard.setVisibility(View.INVISIBLE);
				intent = getIntent();
				gamesViewModel.get(intent.getStringExtra(getString(R.string.EXTRA_GAME_IDFS)));
				break;
		}
	}

	@SuppressWarnings("ConstantConditions")
	private void setPlayers(Player p1, Player p2) {
		llP1.setVisibility(View.VISIBLE);
		llP2.setVisibility(View.VISIBLE);
		if (gameType == GameType.CPU || gameType == GameType.LOCAL) {
			// For local games, display as is
			ivP1Pfp.setImageResource(R.drawable.default_pfp);
			tvP1Name.setText(p1.getName());
			tvP1Elo.setText("");
			tvP1Sign.setText("X");
			ivP2Pfp.setImageResource(gameType == GameType.LOCAL ? R.drawable.default_pfp : R.drawable.cpu_pfp);
			tvP2Name.setText(p2.getName());
			tvP2Elo.setText("");
			tvP2Sign.setText("O");
			if (gamesViewModel.getLiveDataGame().getValue().getCrossPlayerIdFs().equals(currentUser.getIdFs())) {
				tvCurrentPlayer.setText(R.string.player_x_turn);
			} else {
				tvCurrentPlayer.setText(R.string.player_o_turn);
				tvP1Sign.setText("O");
				tvP2Sign.setText("X");
			}
		} else if (gameType == GameType.ONLINE) {
			// For online games
			if (!gamesViewModel.getLiveDataIsStarted().getValue()) {
				ivP1Pfp.setImageBitmap(p1.getPictureBitmap());
				tvP1Name.setText(currentUser.getUsername());
				tvP1Elo.setText(getString(R.string.player_elo_format, Math.round(currentUser.getElo())));
				tvP1Sign.setText("");
				ivP2Pfp.setImageBitmap((BitmapFactory.decodeResource(getResources(), R.drawable.default_pfp)));
				tvP2Name.setText(R.string.searching_for_opponent);
				tvP2Elo.setText("");
				tvP2Sign.setText("");
			} else {
				boolean isHost = Objects.equals(gamesViewModel.getLiveDataGame().getValue().getPlayer1().getIdFs(), currentUser.getIdFs());
				if (isHost) {
					ivP2Pfp.setImageBitmap(p2.getPictureBitmap());
					tvP2Name.setText(p2.getName());
					tvP2Elo.setText(getString(R.string.player_elo_format, Math.round(p2.getElo())));
					tvP1Sign.setText(Objects.equals(gamesViewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p1.getIdFs()) ? "X" : "O");
					tvP2Sign.setText(Objects.equals(gamesViewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p2.getIdFs()) ? "X" : "O");
				} else {
					ivP2Pfp.setImageBitmap(p1.getPictureBitmap());
					tvP2Name.setText(p1.getName());
					tvP2Elo.setText(getString(R.string.player_elo_format, Math.round(p1.getElo())));
					tvP1Sign.setText(Objects.equals(gamesViewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p1.getIdFs()) ? "O" : "X");
					tvP2Sign.setText(Objects.equals(gamesViewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p1.getIdFs()) ? "O" : "X");
				}
			}
		} else {
			boolean isHost = Objects.equals(gamesViewModel.getLiveDataGame().getValue().getPlayer1().getIdFs(), currentUser.getIdFs());
			if (isHost) {
				ivP1Pfp.setImageBitmap(p1.getPictureBitmap());
				tvP1Name.setText(p1.getName());
				tvP1Elo.setText(getString(R.string.player_elo_format, Math.round(p1.getElo())));
				tvP1Sign.setText(Objects.equals(gamesViewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p1.getIdFs()) ? "O" : "X");
				ivP2Pfp.setImageBitmap(p2.getPictureBitmap());
				tvP2Name.setText(p2.getName());
				tvP2Elo.setText(getString(R.string.player_elo_format, Math.round(p2.getElo())));
				tvP2Sign.setText(Objects.equals(gamesViewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p2.getIdFs()) ? "O" : "X");
			} else {
				ivP1Pfp.setImageBitmap(p2.getPictureBitmap());
				tvP1Name.setText(p2.getName());
				tvP1Elo.setText(getString(R.string.player_elo_format, Math.round(p2.getElo())));
				tvP1Sign.setText(Objects.equals(gamesViewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p2.getIdFs()) ? "X" : "O");
				ivP2Pfp.setImageBitmap(p1.getPictureBitmap());
				tvP2Name.setText(p1.getName());
				tvP2Elo.setText(getString(R.string.player_elo_format, Math.round(p1.getElo())));
				tvP2Sign.setText(Objects.equals(gamesViewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p1.getIdFs()) ? "X" : "O");
			}
		}
	}

	//region BoardInitialization
	private void createBoard() {
		final float BOARD_DRAWABLE_SIZE = 653;
		boardSize = getBoardSize();
		conversionFactor = boardSize / BOARD_DRAWABLE_SIZE;

		gridBoard = findViewById(R.id.gridBoard);
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
			for (int iCol = 0; iCol < 3; iCol++) {
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
		btn.setTag("btn" + row + col + iRow + iCol); // Set a tag for identification
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

	/**
	 * Resets the entire board display (clears moves and winners)
	 */
	private void resetBoardDisplay() {
		// Clear all inner board winners
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				outerBoardState[i][j] = 0;
				gridBoard.getChildAt(i * 3 + j).setBackground(null);
				gridBoard.getChildAt(i * 3 + j).setBackgroundColor(Color.TRANSPARENT);

				// Clear all moves
				GridLayout innerGrid = (GridLayout) gridBoard.getChildAt(i * 3 + j);
				for (int k = 0; k < innerGrid.getChildCount(); k++) {
					ImageView btn = (ImageView) innerGrid.getChildAt(k);
					btn.setImageDrawable(null);
					btn.setAlpha(1.0f);
				}
			}
		}
	}

	/**
	 * Rebuilds the game state up to the specified move index
	 */
	@SuppressWarnings("ConstantConditions")
	private void rebuildGameStateToMove(int targetMoveIndex) {
		Game game = gamesViewModel.getLiveDataGame().getValue();
		OuterBoard tempBoard = new OuterBoard();

		// Replay all moves up to the target index
		for (int i = 0; i < targetMoveIndex; i++) {
			BoardLocation move = game.getMoves().get(i);
			tempBoard.makeMove(move);

			// Display the move
			int innerGridIndex = move.getOuter().x * 3 + move.getOuter().y;
			int btnIndex = move.getInner().x * 3 + move.getInner().y;
			GridLayout innerGrid = (GridLayout) gridBoard.getChildAt(innerGridIndex);
			ImageView btn = (ImageView) innerGrid.getChildAt(btnIndex);
			btn.setImageResource(i % 2 == 0 ? R.drawable.x : R.drawable.o);

			// Check if this move caused an inner board win
			Point outerPoint = new Point(move.getOuter().x, move.getOuter().y);
			if (tempBoard.getBoard(outerPoint).isFinished()) {
				char winner = tempBoard.getBoard(outerPoint).getWinner();
				if (winner != 0) {
					outerBoardState[move.getOuter().x][move.getOuter().y] = winner;

					// Update the UI for the winner
					if (winner == 'X') {
						innerGrid.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.x_blurred, null));
					} else if (winner == 'O') {
						innerGrid.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.o_blurred, null));
					} else if (winner == 'T') {
						innerGrid.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.tie, null));
					}

					for (int k = 0; k < innerGrid.getChildCount(); k++) {
						View child = innerGrid.getChildAt(k);
						child.setAlpha(0.05f);
					}
				}
			}
		}
	}
}