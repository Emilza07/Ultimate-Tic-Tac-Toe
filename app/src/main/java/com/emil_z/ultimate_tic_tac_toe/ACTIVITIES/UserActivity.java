package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emil_z.model.Game;
import com.emil_z.model.Player;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.ADPTERS.BASE.GenericAdapter;
import com.emil_z.ultimate_tic_tac_toe.ADPTERS.GamesAdapter;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.GamesViewModel;

import java.util.Objects;

public class UserActivity extends BaseActivity {
	private ImageView ivAvatar;
	private TextView tvUsername;
	private TextView tvElo;
	private RecyclerView rvGames;

	private GamesViewModel viewModel;
	private GamesAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_user);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		initializeViews();
		setViewModel();
		setAdapter();

	}
	public void initializeViews() {
		ivAvatar = findViewById(R.id.ivAvatar);
		tvUsername = findViewById(R.id.tvUsername);
		tvElo = findViewById(R.id.tvElo);
		rvGames = findViewById(R.id.rvGames);

		tvUsername.setText(currentUser.getName());
		tvElo.setText("ELO: " + Math.round(currentUser.getElo()));
		setListeners();
	}

	@Override
	protected void setListeners() {

	}

	@Override
	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(GamesViewModel.class);
		showProgressDialog("Games", "Loading games...");
		viewModel.getUserGames(currentUser.getIdFs());

		viewModel.getLiveDataCollection().observe(this, games -> {
			if (games != null) {
				adapter.setItems(games);
				hideProgressDialog();
			}
		});
	}

	public void setAdapter(){
		adapter = new GamesAdapter(null,
				R.layout.game_single_layout,
				holder -> {
					holder.putView("ivAvatar", holder.itemView.findViewById(R.id.ivAvatar));
					holder.putView("tvUsername", holder.itemView.findViewById(R.id.tvUsername));
					holder.putView("tvElo", holder.itemView.findViewById(R.id.tvElo));
					holder.putView("ivGameResult", holder.itemView.findViewById(R.id.ivGameResult));
				},
				((holder, item, position) -> {
					Player opponent = (Objects.equals(item.getPlayer1().getIdFs(), currentUser.getIdFs())? item.getPlayer2(): item.getPlayer1());
					//((ImageView) holder.getView("ivAvatar")).setImageBitmap(opponent.getAvatar()); TODO: add avatar
					((TextView) holder.getView("tvUsername")).setText(opponent.getName());
					((TextView) holder.getView("tvElo")).setText("(" + Math.round(opponent.getElo()) + ")");
					((ImageView) holder.getView("ivGameResult")).setImageResource(Objects.equals(item.getWinnerIdFs(), currentUser.getIdFs()) ? R.drawable.ok : R.drawable.not_ok);
				})
		);

		rvGames.setAdapter(adapter);
		rvGames.setLayoutManager(new LinearLayoutManager(this));

		adapter.setOnItemClickListener((item, position) -> {
			//TODO: start watching game;
		});
	}


}