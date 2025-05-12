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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.AlertUtil;
import com.emil_z.model.BoardLocation;
import com.emil_z.model.Game;
import com.emil_z.model.GameType;
import com.emil_z.model.Player;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.GamesViewModel;
import com.emil_z.viewmodel.GamesViewModelFactory;

import java.util.Objects;

public class GameActivity extends BaseActivity {

	private int boardSize;
	private float conversionFactor;
	private GridLayout gridBoard;

	private ConstraintLayout clLoading;
	private Button btnAbort;
	private ImageView ivP1Avatar;
	private TextView tvP1Name;
	private TextView tvP1Elo;
	private TextView tvP1Sign;
	private ImageView ivP2Avatar;
	private TextView tvP2Name;
	private TextView tvP2Elo;
	private TextView tvP2Sign;
	private TextView tvCurrentPlayer;

	private LinearLayout llReview;
	private Button btnForward;
	private Button btnBackward;
	private int moveIndex = 0;

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
		tvCurrentPlayer = findViewById(R.id.tvCurrentPlayer);

		llReview = findViewById(R.id.llReview);
		btnForward = findViewById(R.id.btnForward);
		btnBackward = findViewById(R.id.btnBackward);

		clLoading = findViewById(R.id.clLoading);
		btnAbort = findViewById(R.id.btnAbort);

		ivP1Avatar = findViewById(R.id.ivP1Avatar);
		tvP1Name = findViewById(R.id.tvP1Name);
		tvP1Elo = findViewById(R.id.tvP1Elo);
		tvP1Sign = findViewById(R.id.tvP1Sign);
		ivP2Avatar = findViewById(R.id.ivP2Avatar);
		tvP2Name = findViewById(R.id.tvP2Name);
		tvP2Elo = findViewById(R.id.tvP2Elo);
		tvP2Sign = findViewById(R.id.tvP2Sign);

		createBoard();
	}

	protected void setListeners() {
		btnAbort.setOnClickListener(v -> {
			if (gameType.equals(GameType.ONLINE)) {
				viewModel.exitGame();
				Toast.makeText(this, "Online game aborted", Toast.LENGTH_SHORT).show();
			}
			Intent intent = new Intent();
			setResult(RESULT_CANCELED, intent);
			finish();
		});
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Game game = viewModel.getLiveDataGame().getValue();
				AlertUtil.alert(
						GameActivity.this,
						(game != null && game.isStarted())? "Resign" : "Abort",
						"Are you sure you want to exit the game?",
						true,
						0,
						"Yes",
						"No",
						null,
						(() -> {
							Intent intent = new Intent();
							if (gameType == GameType.ONLINE)
								viewModel.exitGame();
							setResult((game != null && game.getMoves().isEmpty()) ? RESULT_OK : RESULT_CANCELED, intent);
							intent.putExtra(getString(R.string.EXTRA_GAME_TYPE), gameType);
							if (gameType != GameType.ONLINE)
								finish();
						}),
						null,
						null
				);
			}
		});

		btnForward.setOnClickListener(v -> {
			Game game = viewModel.getLiveDataGame().getValue();

			if (moveIndex >= game.getMoves().size()) {
				Toast.makeText(GameActivity.this, "No more moves to review", Toast.LENGTH_SHORT).show();
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

			if (moveIndex < game.getMoves().size() - 1) {
				// The next grid is determined by the inner position of the current move
				int nextGridIndex = btnIndex;
				GridLayout nextGrid = (GridLayout) gridBoard.getChildAt(nextGridIndex);

				// Check if the next grid is already won or full (would be a free move)
				boolean isNextGridPlayable = !game.getOuterBoard().getBoard(new Point(nextGridIndex / 3, nextGridIndex % 3)).isFinished();

				if (isNextGridPlayable) {
					// Highlight the specific grid
					nextGrid.setBackgroundColor(Color.parseColor("#7F9c8852"));
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
			Game game = viewModel.getLiveDataGame().getValue();
			BoardLocation lastMove = game.getMoves().get(moveIndex);
			int innerGridIndex = lastMove.getOuter().x * 3 + lastMove.getOuter().y;
			int btnIndex = lastMove.getInner().x * 3 + lastMove.getInner().y;
			GridLayout innerGrid = (GridLayout) gridBoard.getChildAt(innerGridIndex);
			ImageView btn = (ImageView) innerGrid.getChildAt(btnIndex);
			btn.setImageDrawable(null);

			for (int i = 0; i < gridBoard.getChildCount(); i++) {
				gridBoard.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
			}

			if (moveIndex > 0) {
				BoardLocation previousMove = game.getMoves().get(moveIndex - 1);
				int prevInnerPos = previousMove.getInner().x * 3 + previousMove.getInner().y;

				boolean isNextGridPlayable = !game.getOuterBoard().getBoard(new Point(prevInnerPos / 3, prevInnerPos % 3)).isFinished();

				if (isNextGridPlayable) {
					GridLayout nextGrid = (GridLayout) gridBoard.getChildAt(prevInnerPos);
					nextGrid.setBackgroundColor(Color.parseColor("#7F9c8852"));
				}
			}
			tvCurrentPlayer.setText(moveIndex % 2 == 0 ? R.string.player_x_turn : R.string.player_o_turn);
		});
	}

	private void handleBoardButtonClick(ImageView btn) {
		String tag = (String) btn.getTag();
		// Handle button click using the tag (e.g., "btn0101" for row 3, col 4)
		viewModel.makeMove(new BoardLocation(tag.charAt(3) - '0', tag.charAt(4) - '0', tag.charAt(5) - '0', tag.charAt(6) - '0'));
		viewModel.getLiveDataCode().observe(this, new Observer<Integer>() {
			@Override
			public void onChanged(Integer code) {
				if(code != 0)
					Toast.makeText(GameActivity.this, errorCodes[code], Toast.LENGTH_SHORT).show();
				viewModel.resetLvCode();
				viewModel.getLiveDataCode().removeObserver(this);
			}
		});
	}

	protected void setViewModel() {
		viewModel = new ViewModelProvider(this,
				new GamesViewModelFactory(getApplication(), gameType))
				.get(GamesViewModel.class);

		viewModel.getLiveDataSuccess().observe(this, success -> {
			if (success) {
			}
		});

		viewModel.getLiveDataGame().observe(this, game -> {
			if (game == null) {
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				finish();
			} else if (!game.getMoves().isEmpty() && !game.isFinished()) { //Remote player made a move
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
				btn.setImageResource(viewModel.getLiveDataGame().getValue().getOuterBoard().getCurrentPlayer() == 'O' ? R.drawable.x : R.drawable.o);
				if (!game.getOuterBoard().isFreeMove()) {
					GridLayout nextMoveGrid = (GridLayout) gridBoard.getChildAt(btnIndex);
					nextMoveGrid.setBackgroundColor(Color.parseColor("#7F9c8852"));
				}
				tvCurrentPlayer.setText(viewModel.getLiveDataGame().getValue().getOuterBoard().getCurrentPlayer() == 'X' ? R.string.player_x_turn : R.string.player_o_turn);


			}
		});

		viewModel.getLiveDataOuterBoardWinners().observe(this, new Observer<char[][]>() {
			@Override
			public void onChanged(char[][] chars) {
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						if (chars[i][j] != 0) {
							// Set the background of the outer board to indicate the winner
							if (chars[i][j] == 'X') {
								gridBoard.getChildAt(i * 3 + j).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.x, null));
							} else if (chars[i][j] == 'O') {
								gridBoard.getChildAt(i * 3 + j).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.o, null));
							} else {
								gridBoard.getChildAt(i * 3 + j).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.t, null));
							}
						}
					}
				}
			}
		});

		viewModel.getLiveDataIsFinished().observe(this, new Observer<Boolean>() {
			@Override

			public void onChanged(Boolean aBoolean) {
				String winner;

				switch (gameType) {
					case CPU:
					case LOCAL:
						winner = viewModel.getLiveDataGame().getValue().getWinnerIdFs();
						break;
					case ONLINE:
						winner = Objects.equals(viewModel.getLiveDataGame().getValue().getWinnerIdFs(), viewModel.getLiveDataGame().getValue().getCrossPlayerIdFs()) ? "X" : "O";
						break;
					default:
						throw new IllegalStateException("Unexpected value: " + gameType);
				}
				AlertUtil.alert(GameActivity.this,
						"Game Over",
						(!Objects.equals(viewModel.getLiveDataGame().getValue().getWinnerIdFs(), "T")) ? "Player " + winner + " wins!" : "Game is a tie!",
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
			}
		});

		viewModel.getLiveDataIsStarted().observe(this, new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
				if (aBoolean) {
					// Game started
					Toast.makeText(GameActivity.this, "Game started", Toast.LENGTH_SHORT).show();
					clLoading.setVisibility(View.GONE);
					tvCurrentPlayer.setVisibility(View.VISIBLE);
					gridBoard.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.board, null));
					setPlayers(viewModel.getLiveDataGame().getValue().getPlayer1(), viewModel.getLiveDataGame().getValue().getPlayer2());
					if(gameType == GameType.HISTORY)
						llReview.setVisibility(View.VISIBLE);
//					else
//						llReview.setVisibility(View.GONE);
				}
			}
		});

		viewModel.getLiveDataEntity().observe(this, game -> {
			viewModel.startHistoryGame(game);
		});
	}

	//region GameInit
	private void gameInit(GameType gameType) {
		switch (gameType) {
			case CPU:
				// Initialize SP game
				Intent intent = getIntent();
				char sign = intent.getCharExtra(getString(R.string.EXTRA_SIGN), 'X');
				viewModel.startCpuGame(sign == 'X' ? currentUser.getIdFs() : "CPU");
				break;
			case LOCAL:
				// Initialize local game
				viewModel.startLocalGame();
				break;
			case ONLINE:
				// Initialize online game as joiner
				try {
					viewModel.startOnlineGame(new Player(currentUser));
					setPlayers(new Player(currentUser), null);
				} catch (Exception e) {
					Toast.makeText(this, "Error starting game: " + e.getMessage() + " Try again", Toast.LENGTH_SHORT).show();
				}
				break;
			case HISTORY:
				intent = getIntent();
				viewModel.get(intent.getStringExtra(getString(R.string.EXTRA_GAME_IDFS)));
				break;
		}
	}

	private void setPlayers(Player p1, Player p2) {
		if (gameType == GameType.CPU || gameType == GameType.LOCAL) {
			// For local games, display as is
			ivP1Avatar.setImageBitmap(p1.getPictureBitmap());
			tvP1Name.setText(p1.getName());
			tvP1Elo.setText("");
			tvP1Sign.setText("X");
			ivP2Avatar.setImageBitmap(p2.getPictureBitmap());
			tvP2Name.setText(p2.getName());
			tvP2Elo.setText("");
			tvP2Sign.setText("O");
			if (viewModel.getLiveDataGame().getValue().getCrossPlayerIdFs().equals(currentUser.getIdFs())) {
				tvCurrentPlayer.setText(R.string.player_x_turn);
			} else {
				tvCurrentPlayer.setText(R.string.player_o_turn);
				tvP1Sign.setText("O");
				tvP2Sign.setText("X");
			}
		} else if (gameType == GameType.ONLINE){
			// For online games
			if(!viewModel.getLiveDataIsStarted().getValue())
			{
				ivP1Avatar.setImageBitmap(p1.getPictureBitmap());
				tvP1Name.setText(currentUser.getUsername());
				tvP1Elo.setText("(" + Math.round(currentUser.getElo()) + ")");
				tvP1Sign.setText("");
				ivP2Avatar.setImageBitmap((BitmapFactory.decodeResource(getResources(), R.drawable.avatar_default)));
				tvP2Name.setText( "Waiting for opponent...");
				tvP2Elo.setText("");
				tvP2Sign.setText("");
			}
			else {
				boolean isHost = Objects.equals(viewModel.getLiveDataGame().getValue().getPlayer1().getIdFs(), currentUser.getIdFs());
				if (isHost) {
					ivP2Avatar.setImageBitmap(viewModel.getLiveDataGame().getValue().getPlayer2().getPictureBitmap());
					tvP2Name.setText(p2.getName());
					tvP2Elo.setText("(" + Math.round(p2.getElo()) + ")");
					tvP1Sign.setText(Objects.equals(viewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p1.getIdFs()) ? "X" : "O");
					tvP2Sign.setText(Objects.equals(viewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p2.getIdFs()) ? "X" : "O");
				} else {
					ivP2Avatar.setImageBitmap(viewModel.getLiveDataGame().getValue().getPlayer1().getPictureBitmap());
					tvP2Name.setText(p1.getName());
					tvP2Elo.setText("(" + Math.round(p1.getElo()) + ")");
					tvP1Sign.setText(Objects.equals(viewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p1.getIdFs()) ? "O" : "X");
					tvP1Sign.setText(Objects.equals(viewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p1.getIdFs()) ? "O" : "X");
				}
			}
		} else {
			// For history games
			tvP1Name.setText(p1.getName());
			tvP1Elo.setText("(" + Math.round(p1.getElo()) + ")");
			tvP1Sign.setText(Objects.equals(viewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p1.getIdFs()) ? "O" : "X");
			tvP2Name.setText(p2.getName());
			tvP2Elo.setText("(" + Math.round(p2.getElo()) + ")");
			tvP2Sign.setText(Objects.equals(viewModel.getLiveDataGame().getValue().getCrossPlayerIdFs(), p2.getIdFs()) ? "O" : "X");
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