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

/**
 * Activity for managing and displaying a single Ultimate Tic Tac Toe game.
 * Handles game initialization, board creation, move handling, player display, and game state updates.
 * Supports different game types: CPU, LOCAL, ONLINE, and REPLAY.
 */
public class GameActivity extends BaseActivity {

	private GridLayout gridBoard;
	private LinearLayout llP2;
	private ImageView ivP2Pfp;
	private TextView tvP2Name;
	private TextView tvP2Elo;
	private TextView tvP2Sign;
	private LinearLayout llP1;
	private ImageView ivP1Pfp;
	private TextView tvP1Name;
	private TextView tvP1Elo;
	private TextView tvP1Sign;
	private TextView tvCurrentPlayer;
	private ConstraintLayout clLoading;
	private Button btnAbort;
	private LinearLayout llReview;
	private Button btnForward;
	private Button btnBackward;

	private GamesViewModel gamesViewModel;
	private UsersViewModel usersViewModel;
	private boolean monitorServiceStarted = false;

	private String[] errorCodes;

	private int moveIndex = 0;
	private char[][] outerBoardWinners;

	private GameType gameType;
	private int boardSize;
	private float conversionFactor;

	private Intent intent;

	/**
	 * Initializes the activity, sets up UI, listeners, ViewModels, and game state.
	 *
	 * @param savedInstanceState The previously saved instance state, if any.
	 */
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

	/**
	 * Initializes all view components for the game screen.
	 */
	@Override
	protected void initializeViews() {

		llP2 = findViewById(R.id.llP2);
		ivP2Pfp = findViewById(R.id.ivP2Pfp);
		tvP2Name = findViewById(R.id.tvP2Name);
		tvP2Elo = findViewById(R.id.tvP2Elo);
		tvP2Sign = findViewById(R.id.tvP2Sign);
		llP1 = findViewById(R.id.llP1);
		ivP1Pfp = findViewById(R.id.ivP1Pfp);
		tvP1Name = findViewById(R.id.tvP1Name);
		tvP1Elo = findViewById(R.id.tvP1Elo);
		tvP1Sign = findViewById(R.id.tvP1Sign);
		tvCurrentPlayer = findViewById(R.id.tvCurrentPlayer);
		clLoading = findViewById(R.id.clLoading);
		btnAbort = findViewById(R.id.btnAbort);
		llReview = findViewById(R.id.llReview);
		btnForward = findViewById(R.id.btnForward);
		btnBackward = findViewById(R.id.btnBackward);


		intent = getIntent();
		gameType = (GameType) intent.getSerializableExtra(MainActivity.EXTRA_GAME_TYPE);
		intent.putExtra(MainActivity.EXTRA_GAME_TYPE, gameType);
		errorCodes = getResources().getStringArray(R.array.error_codes);
		createBoard();
		outerBoardWinners = new char[3][3];
	}

	/**
	 * Sets up click listeners for game controls, including abort, move review, and back navigation.
	 */
	@SuppressWarnings("ConstantConditions")
	@Override
	protected void setListeners() {
		btnAbort.setOnClickListener(v -> {
			if (gameType.equals(GameType.ONLINE)) {
				gamesViewModel.exitGame();
			}
			setResult(RESULT_CANCELED, intent);
			finish();
		});

		btnForward.setOnClickListener(v -> {
			Game game = gamesViewModel.getLiveDataGame().getValue();

			if (moveIndex >= game.getMoves().size()) {
				Toast.makeText(GameActivity.this, R.string.last_move, Toast.LENGTH_SHORT).show();
				return;
			}

			for (int i = 0; i < gridBoard.getChildCount(); i++) {
				gridBoard.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
			}

			BoardLocation lastMove = game.getMoves().get(moveIndex);
			int innerGridIndex = lastMove.getOuter().x * 3 + lastMove.getOuter().y;
			int btnIndex = lastMove.getInner().x * 3 + lastMove.getInner().y;
			GridLayout innerGrid = (GridLayout) gridBoard.getChildAt(innerGridIndex);
			ImageView btn = (ImageView) innerGrid.getChildAt(btnIndex);
			btn.setImageResource(moveIndex % 2 == 1 ? R.drawable.o : R.drawable.x);

			rebuildGameStateToMove(moveIndex + 1);

			if (moveIndex < game.getMoves().size() - 1) {
				boolean isNextGridPlayable = outerBoardWinners[btnIndex / 3][btnIndex % 3] == 0;
				if (isNextGridPlayable) {
					GridLayout nextInnerGrid = (GridLayout) gridBoard.getChildAt(btnIndex);
					nextInnerGrid.setBackgroundResource(R.drawable.border);
				}
			}

			tvCurrentPlayer.setText(moveIndex % 2 == 1 ? R.string.player_x_turn : R.string.player_o_turn);
			moveIndex++;
		});

		btnBackward.setOnClickListener(v -> {
			moveIndex--;
			if (moveIndex < 0) {
				Toast.makeText(GameActivity.this, R.string.last_move, Toast.LENGTH_SHORT).show();
				moveIndex = 0;
				return;
			}

			Game game = gamesViewModel.getLiveDataGame().getValue();
			resetBoardDisplay();
			rebuildGameStateToMove(moveIndex);

			if (moveIndex > 0) {
				BoardLocation previousMove = game.getMoves().get(moveIndex - 1);
				int prevInnerPos = previousMove.getInner().x * 3 + previousMove.getInner().y;

				boolean isNextGridPlayable = outerBoardWinners[prevInnerPos / 3][prevInnerPos % 3] == 0;
				if (isNextGridPlayable) {
					GridLayout nextInnerGrid = (GridLayout) gridBoard.getChildAt(prevInnerPos);
					nextInnerGrid.setBackgroundResource(R.drawable.border);
				}
			}

			tvCurrentPlayer.setText(moveIndex % 2 == 0 ? R.string.player_x_turn : R.string.player_o_turn);
		});

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (gameType == GameType.REPLAY) {
					finish();
					return;
				}
				Game game = gamesViewModel.getLiveDataGame().getValue();
				AlertUtil.alert(
					GameActivity.this,
					(game != null && game.getMoves().size() > 1) ? "Resign" : "Abort",
					"Are you sure you want to exit the game?",
					true,
					0,
					"Yes",
					"No",
					null,
					(() -> {
						if (gameType == GameType.ONLINE)
							gamesViewModel.exitGame();
						setResult((game != null && game.getMoves().isEmpty()) ? RESULT_OK : RESULT_CANCELED, intent);
						if (gameType != GameType.ONLINE || game == null)
							finish();
					}),
					null,
					null
				);
			}
		});

	}

	/**
	 * Handles a board button click, parses the tag, and makes a move via the ViewModel.
	 *
	 * @param btn The ImageView button that was clicked.
	 */
	private void handleBoardButtonClick(ImageView btn) {
		String tag = (String) btn.getTag();
		// Handle button click using the tag (e.g., "btn0101" for 1st outerRow, 2st outerColumn and 1th innerRow, 2st innerColumn)
		gamesViewModel.makeMove(new BoardLocation(tag.charAt(3) - '0', tag.charAt(4) - '0', tag.charAt(5) - '0', tag.charAt(6) - '0'));
	}

	/**
	 * Sets up ViewModels, observes LiveData for game and user state, and updates the UI accordingly.
	 */
	@SuppressWarnings("ConstantConditions")
	@Override
	protected void setViewModel() {
		gamesViewModel = new ViewModelProvider(this, new GamesViewModelFactory(getApplication(), gameType)).get(GamesViewModel.class);
		usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);

		gamesViewModel.getLiveDataErrorCode().observe(this, code -> {
			if (code != 0) {
				Toast.makeText(GameActivity.this, errorCodes[code], Toast.LENGTH_SHORT).show();
				gamesViewModel.resetLiveDataErrorCode();
				if (code == 4) {
					setResult(RESULT_CANCELED, intent);
					finish();
				}
			}
		});

		gamesViewModel.getLiveDataGame().observe(this, game -> {
			if (game == null) {
				setResult(RESULT_OK, intent);
				finish();
			} else if (!game.getMoves().isEmpty() && !game.isFinished()) {
				BoardLocation lastMove = game.getMoves().get(game.getMoves().size() - 1);
				if (game.getMoves().size() > 1) {
					int previousMoveIndex = lastMove.getOuter().x * 3 + lastMove.getOuter().y;
					GridLayout prevInnerGrid = (GridLayout) gridBoard.getChildAt(previousMoveIndex);
					prevInnerGrid.setBackgroundColor(Color.TRANSPARENT);
				}
				int innerGridIndex = lastMove.getOuter().x * 3 + lastMove.getOuter().y;
				int btnIndex = lastMove.getInner().x * 3 + lastMove.getInner().y;
				GridLayout innerGrid = (GridLayout) gridBoard.getChildAt(innerGridIndex);
				ImageView btn = (ImageView) innerGrid.getChildAt(btnIndex);

				char currentPlayer = game.getOuterBoard().getCurrentPlayer();
				btn.setImageResource(currentPlayer == 'O' ? R.drawable.x : R.drawable.o);
				if (!game.getOuterBoard().isFreeMove()) {
					GridLayout nextMoveGrid = (GridLayout) gridBoard.getChildAt(btnIndex);
					nextMoveGrid.setBackgroundResource(R.drawable.border);
				}
				tvCurrentPlayer.setText(currentPlayer == 'X' ? R.string.player_x_turn : R.string.player_o_turn);
			}
		});

		gamesViewModel.getLiveDataOuterBoardWinners().observe(this, boardWinners -> {
			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 3; col++) {
					char winner = boardWinners[row][col];
					if (winner != 0) {
						GridLayout innerGrid = (GridLayout) gridBoard.getChildAt(row * 3 + col);
						if (winner == 'X') {
							innerGrid.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.x_blurred, null));
						} else if (winner == 'O') {
							innerGrid.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.o_blurred, null));
						} else {
							innerGrid.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.tie, null));
						}

						for (int i = 0; i < innerGrid.getChildCount(); i++) {
							innerGrid.getChildAt(i).setAlpha(0.05f);
						}
					}
				}
			}
		});

		gamesViewModel.getLiveDataIsFinished().observe(this, aBoolean -> {
			Game game = gamesViewModel.getLiveDataGame().getValue();
			String winner;

			switch (gameType) {
				case CPU:
				case LOCAL:
					winner = game.getWinnerIdFs();
					break;
				case ONLINE:
					if (Objects.equals(game.getWinnerIdFs(), "T"))
						winner = "T";
					else
						winner = Objects.equals(game.getWinnerIdFs(), game.getCrossPlayerIdFs()) ? "X" : "O";
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + gameType);
			}
			AlertUtil.alert(GameActivity.this,
				getString(R.string.match_complete),
				Objects.equals(winner, "T") ?
					getString(R.string.game_outcome_tie) :
					getString(R.string.game_outcome_win, winner),
				false,
				0,
				getString(R.string.return_to_menu),
				null,
				null,
				(() -> {
					intent.putExtra(MainActivity.EXTRA_GAME_TYPE, gameType);
					setResult(RESULT_OK, intent);
					finish();
				}),
				null,
				null);
		});

		gamesViewModel.getLiveDataIsStarted().observe(this, aBoolean -> {
			if (aBoolean) {
				Game game = gamesViewModel.getLiveDataGame().getValue();
				boolean isHost = Objects.equals(game.getPlayer1().getIdFs(), currentUser.getIdFs());

				if (gameType == GameType.ONLINE || gameType == GameType.REPLAY)
					usersViewModel.get(isHost ? game.getPlayer2().getIdFs() : game.getPlayer1().getIdFs());
				else {
					setPlayers(game.getPlayer1(), game.getPlayer2());
					clLoading.setVisibility(View.GONE);
					tvCurrentPlayer.setVisibility(View.VISIBLE);
					gridBoard.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.board, null));
				}

				if (gameType == GameType.ONLINE) {
					String gameIdFs = game.getIdFs();
					String player1IdFs = game.getPlayer1().getIdFs();
					String player2IdFs = game.getPlayer2().getIdFs();

					if (!monitorServiceStarted) {
						monitorServiceStarted = true;
						AppMonitorService.startService(this, true, gameIdFs, player1IdFs, player2IdFs, isHost);
					} else {
						AppMonitorService.updateGameState(true, gameIdFs, player1IdFs, player2IdFs, isHost);
					}
				}
			}
		});

		gamesViewModel.getLiveDataGameIdFs().observe(this, gameIdFs -> {
			if (gameIdFs != null && !monitorServiceStarted) {
				monitorServiceStarted = true;
				AppMonitorService.startService(this, true, gameIdFs, null, null, false);
			}
		});

		gamesViewModel.getLiveDataEntity().observe(this, game -> gamesViewModel.startReplayGame(game));

		usersViewModel.getLiveDataEntity().observe(this, user -> {
			clLoading.setVisibility(View.GONE);
			tvCurrentPlayer.setVisibility(View.VISIBLE);
			gridBoard.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.board, null));
			gridBoard.setVisibility(View.VISIBLE);

			Game game = gamesViewModel.getLiveDataGame().getValue();
			boolean isHost = Objects.equals(game.getPlayer1().getIdFs(), currentUser.getIdFs());
			if (user != null) {
				Player p1 = isHost ? new Player(currentUser) : new Player(user);
				Player p2 = isHost ? new Player(user) : new Player(currentUser);
				setPlayers(p1, p2);
			} else
				setPlayers(game.getPlayer1(), game.getPlayer2());

			if (gameType == GameType.REPLAY)
				llReview.setVisibility(View.VISIBLE);
		});
	}

	/**
	 * Cleans up resources and notifies the monitor service when the activity is destroyed.
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppMonitorService.userClosedActivity(this);
	}

	/**
	 * Initializes the game based on the selected game type.
	 *
	 * @param gameType The type of game (CPU, LOCAL, ONLINE, REPLAY).
	 */
	private void gameInit(GameType gameType) {
		switch (gameType) {
			case CPU:
				char sign = intent.getCharExtra(MainActivity.EXTRA_SIGN, 'X');
				gamesViewModel.startCpuGame(sign == 'X' ? currentUser.getIdFs() : "CPU");
				break;
			case LOCAL:
				gamesViewModel.startLocalGame();
				break;
			case ONLINE:
				try {
					clLoading.setVisibility(View.VISIBLE);
					gamesViewModel.startOnlineGame(new Player(currentUser));
					setPlayers(new Player(currentUser), null);
				} catch (Exception e) {
					Toast.makeText(this, R.string.game_connection_error, Toast.LENGTH_SHORT).show();
					setResult(RESULT_CANCELED, intent);
					finish();
				}
				break;
			case REPLAY:
				gridBoard.setVisibility(View.INVISIBLE);
				gamesViewModel.get(intent.getStringExtra(MainActivity.EXTRA_GAME_ID_FS));
				break;
		}
	}

	/**
	 * Sets up the player views and information for the game screen based on the game type and player roles.
	 * Handles display of player names, profile pictures, ELO ratings, and X/O signs for both players.
	 * Adjusts the UI for CPU, LOCAL, ONLINE, and REPLAY game types, including handling host/opponent logic.
	 *
	 * @param p1 The first player (may be the current user or opponent depending on context).
	 * @param p2 The second player (may be the current user or opponent depending on context).
	 */
	@SuppressWarnings("ConstantConditions")
	private void setPlayers(Player p1, Player p2) {
		llP1.setVisibility(View.VISIBLE);
		llP2.setVisibility(View.VISIBLE);
		Game game = gamesViewModel.getLiveDataGame().getValue();

		if (gameType == GameType.CPU || gameType == GameType.LOCAL) {
			ivP1Pfp.setImageResource(R.drawable.default_pfp);
			tvP1Name.setText(p1.getName());
			tvP1Elo.setText("");
			tvP1Sign.setText("X");

			ivP2Pfp.setImageResource(gameType == GameType.LOCAL ? R.drawable.default_pfp : R.drawable.cpu_pfp);
			tvP2Name.setText(p2.getName());
			tvP2Elo.setText("");
			tvP2Sign.setText("O");
			if (!game.getCrossPlayerIdFs().equals(currentUser.getIdFs())) {
				tvCurrentPlayer.setText(R.string.player_o_turn);
				tvP1Sign.setText("O");
				tvP2Sign.setText("X");
			} else {
				tvCurrentPlayer.setText(R.string.player_x_turn);
			}
		} else if (gameType == GameType.ONLINE) {
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
					tvP2Sign.setText(Objects.equals(gamesViewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p2.getIdFs()) ? "O" : "X");
				}
			}
		} else {
			boolean isHost = Objects.equals(gamesViewModel.getLiveDataGame().getValue().getPlayer1().getIdFs(), currentUser.getIdFs());
			Player firstPlayer = isHost ? p1 : p2;
			Player secondPlayer = isHost ? p2 : p1;

			ivP1Pfp.setImageBitmap(firstPlayer.getPictureBitmap());
			tvP1Name.setText(firstPlayer.getName());
			tvP1Elo.setText(getString(R.string.player_elo_format, Math.round(firstPlayer.getElo())));

			ivP2Pfp.setImageBitmap(secondPlayer.getPictureBitmap());
			tvP2Name.setText(secondPlayer.getName());
			tvP2Elo.setText(getString(R.string.player_elo_format, Math.round(secondPlayer.getElo())));

			boolean isFirstPlayerX = Objects.equals(game.getCrossPlayerIdFs(), firstPlayer.getIdFs());
			tvP1Sign.setText(isFirstPlayerX ? "X" : "O");
			tvP2Sign.setText(isFirstPlayerX ? "O" : "X");
		}
	}

	//region BoardInitialization

	/**
	 * Creates the main 3x3 outer board for Ultimate Tic Tac Toe.
	 * Initializes the board size, conversion factor, and adds inner grids to the main board.
	 */
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

	/**
	 * Creates a 3x3 inner grid (sub-board) at the specified outer board position.
	 *
	 * @param row The row index of the outer board.
	 * @param col The column index of the outer board.
	 * @return The initialized inner GridLayout.
	 */
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

	/**
	 * Creates a single cell button for the inner grid.
	 * Sets up size, padding, margins, click listener, and a unique tag for identification.
	 *
	 * @param row  Outer board row index.
	 * @param col  Outer board column index.
	 * @param iRow Inner grid row index.
	 * @param iCol Inner grid column index.
	 * @return The initialized ImageView button.
	 */
	private ImageView createBoardButton(int row, int col, int iRow, int iCol) {
		final float INNER_CELL_DRAWABLE_SIZE = 55;
		final int INNER_CELL_DRAWABLE_REF_SIZE = (int) (INNER_CELL_DRAWABLE_SIZE * conversionFactor);
		final int MARGIN = (int) (1 * conversionFactor);
		final int PADDING = (int) (7 * conversionFactor);

		ImageView btn = new ImageView(this);
		btn.setId(View.generateViewId());
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
		btn.setTag("btn" + row + col + iRow + iCol);
		return btn;
	}

	/**
	 * Configures the outer GridLayout for the main board, including padding, alignment, and size.
	 *
	 * @param gridLayout The GridLayout to configure.
	 */
	private void setOuterGrid(GridLayout gridLayout) {
		final int PADDING = (int) (21 * conversionFactor);

		gridLayout.setPadding(PADDING, PADDING, PADDING, PADDING);
		gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);

		ViewGroup.LayoutParams params = gridLayout.getLayoutParams();
		params.width = boardSize;
		params.height = boardSize;
		gridLayout.setLayoutParams(params);
	}

	/**
	 * Configures an inner GridLayout (sub-board) with size, padding, margins, and alignment.
	 *
	 * @param row  Outer board row index.
	 * @param col  Outer board column index.
	 * @param grid The inner GridLayout to configure.
	 */
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

	/**
	 * Resets the entire board display (clears moves and winners)
	 */
	private void resetBoardDisplay() {
		// Clear all inner board winners
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				outerBoardWinners[row][col] = 0;
				GridLayout innerGrid = (GridLayout) gridBoard.getChildAt(row * 3 + col);

				innerGrid.setBackground(null);
				innerGrid.setBackgroundColor(Color.TRANSPARENT);

				for (int i = 0; i < innerGrid.getChildCount(); i++) {
					ImageView btn = (ImageView) innerGrid.getChildAt(i);
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

		for (int moveIndex = 0; moveIndex < targetMoveIndex; moveIndex++) {
			BoardLocation move = game.getMoves().get(moveIndex);
			tempBoard.makeMove(move);

			int outerRow = move.getOuter().x, outerCol = move.getOuter().y;
			int innerRow = move.getInner().x, innerCol = move.getInner().y;
			int innerGridIndex = outerRow * 3 + outerCol;
			int btnIndex = innerRow * 3 + innerCol;

			GridLayout innerGrid = (GridLayout) gridBoard.getChildAt(innerGridIndex);
			ImageView btn = (ImageView) innerGrid.getChildAt(btnIndex);
			btn.setImageResource(moveIndex % 2 == 0 ? R.drawable.x : R.drawable.o);

			Point outerPoint = new Point(outerRow, outerCol);
			if (tempBoard.getBoard(outerPoint).isFinished()) {
				char winner = tempBoard.getBoard(outerPoint).getWinner();
				if (winner != 0) {
					outerBoardWinners[outerRow][outerCol] = winner;

					int drawableId = winner == 'X' ? R.drawable.x_blurred :
						winner == 'O' ? R.drawable.o_blurred :
							R.drawable.tie;
					innerGrid.setBackground(ResourcesCompat.getDrawable(getResources(), drawableId, null));

					for (int i = 0; i < innerGrid.getChildCount(); i++) {
						innerGrid.getChildAt(i).setAlpha(0.05f);
					}
				}
			}
		}
	}
}