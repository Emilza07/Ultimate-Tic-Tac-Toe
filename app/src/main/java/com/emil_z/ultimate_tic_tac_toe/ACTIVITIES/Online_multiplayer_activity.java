package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.os.Bundle;
import android.util.DisplayMetrics;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.gridlayout.widget.GridLayout;

import com.emil_z.ultimate_tic_tac_toe.R;

public class Online_multiplayer_activity extends AppCompatActivity {

	private final float BOARD_DRAWABLE_SIZE = 653;
	private int boardSize;
	private float conversionFactor;
	private GridLayout gridBoard;
	private ImageButton[][] buttons = new ImageButton[3][3];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_online_multiplayer);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		initializeViews();
	}

	private void initializeViews() {
		createBoard();
	}

	private void handleButtonClick(ImageView btn) {
		String tag = (String) btn.getTag();
		// Handle button click using the tag (e.g., "ibtn34" for row 3, col 4)
	}

	private void createBoard() {
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
		btn.setImageResource(R.drawable.o);
		GridLayout.LayoutParams btnParams = new GridLayout.LayoutParams();
		btnParams.width = INNER_CELL_DRAWABLE_REF_SIZE;
		btnParams.height = INNER_CELL_DRAWABLE_REF_SIZE;
		btn.setPadding(PADDING, PADDING, PADDING, PADDING);
		btnParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
		btnParams.rowSpec = GridLayout.spec(iRow);
		btnParams.columnSpec = GridLayout.spec(iCol);
		btn.setLayoutParams(btnParams);
		btn.setOnClickListener(v -> handleButtonClick((ImageView) v));
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

}