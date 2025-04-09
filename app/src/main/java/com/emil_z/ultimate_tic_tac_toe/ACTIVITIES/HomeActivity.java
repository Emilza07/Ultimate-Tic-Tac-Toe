package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.AlertUtil;
import com.emil_z.model.Game;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.GamesViewModel;

public class HomeActivity extends BaseActivity {
	private Button btnLocal;
	private Button btnHost;
	private Button btnJoin;
	private GamesViewModel viewModel;

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
		setViewModel();
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
			viewModel.startLocalGame();
		});
		btnHost.setOnClickListener(v -> {
			viewModel.hostOnlineGame(BaseActivity.currentUser);
		});
		btnJoin.setOnClickListener(v -> {
			viewModel.joinOnlineGame(BaseActivity.currentUser);

		});
	}

	@Override
	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(GamesViewModel.class);

		viewModel.getSuccess().observe(this, new Observer<Boolean>() {
			public void onChanged(Boolean aBoolean) {
				if (aBoolean && viewModel.getLvGame().getValue() != null) {
					gameUploaded();
					setlvGameObserver();
				}
				else if (aBoolean && viewModel.getLvGame() == null){
					Toast.makeText(HomeActivity.this, "Game removed successfully", Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	private void setlvGameObserver() {
		viewModel.getLvGame().observe(this, new Observer<Game>() {
			@Override
			public void onChanged(Game game) {
				if (game != null) {
					if (!game.getPlayer2().getIdFs().isEmpty()) {
						Toast.makeText(HomeActivity.this, "Game Created", Toast.LENGTH_SHORT).show();
						startActivity(new Intent(HomeActivity.this, GameActivity.class));
					}
				}
			}
		});
	}

	private void gameUploaded() {
		AlertUtil.alert(
				this,
				"Game uploaded successfully",
				"Waiting for other player to join",
				false,
				R.drawable.board,
				"Delete game",         // positive button
				null,             // no negative button
				null,             // no neutral button
				() -> viewModel.removeGame(),            // positive action
				null,                             // no negative action
				null                              // no neutral action
		);                           // no neutral action	}
	}
}