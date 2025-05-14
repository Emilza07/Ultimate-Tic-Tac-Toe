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

import com.emil_z.model.User;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.ADPTERS.UsersAdapter;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends BaseActivity {
	private RecyclerView rvLeaderboard;
	private UsersViewModel viewModel;
	private UsersAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_leaderboard);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		initializeViews();
		setListeners();
		setViewModel();
		setAdapter();
	}

	@Override
	public void initializeViews() {
		rvLeaderboard = findViewById(R.id.rvLeaderboard);
	}

	@Override
	public void setListeners() {
	}

	@Override
	public void setViewModel() {
		viewModel = new ViewModelProvider(this).get(UsersViewModel.class);
		viewModel.getAll();

		viewModel.getLiveDataCollection().observe(this, users -> {
			List<User> sortedUsers = new ArrayList<>(users);

			sortedUsers.sort((user1, user2) ->
					Double.compare(user2.getElo(), user1.getElo()));
			int endIndex = Math.min(sortedUsers.size(), 999);
			List<User> topUsers = sortedUsers.subList(0, endIndex);

			adapter.setItems(topUsers);
		});
	}

	private void setAdapter() {
		adapter = new UsersAdapter(null,
				R.layout.user_single_layout,
				holder -> {
					holder.putView("tvRank", holder.itemView.findViewById(R.id.tvRank));
					holder.putView("ivAvatar", holder.itemView.findViewById(R.id.ivAvatar));
					holder.putView("tvUsername", holder.itemView.findViewById(R.id.tvUsername));
					holder.putView("tvElo", holder.itemView.findViewById(R.id.tvElo));
				},
				((holder, item, position) -> {
					((TextView) holder.getView("tvRank")).setText(getString(R.string.rank_format, position + 1));
					((ImageView) holder.getView("ivAvatar")).setImageBitmap(item.getPictureBitmap());
					((TextView) holder.getView("tvUsername")).setText(item.getUsername());
					((TextView) holder.getView("tvElo")).setText(getString(R.string.elo_format, Math.round(item.getElo())));
				})
		);
		rvLeaderboard.setAdapter(adapter);
		rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
	}
}