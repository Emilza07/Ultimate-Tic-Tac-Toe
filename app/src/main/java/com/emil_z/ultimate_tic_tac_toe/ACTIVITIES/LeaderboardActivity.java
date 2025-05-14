package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
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
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
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

		DividerItemDecoration divider = new DividerItemDecoration(rvLeaderboard.getContext(), LinearLayoutManager.VERTICAL);
		divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.dividor));
		rvLeaderboard.addItemDecoration(divider);
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
					holder.putView("ivPfp", holder.itemView.findViewById(R.id.ivPfp));
					holder.putView("tvUsername", holder.itemView.findViewById(R.id.tvUsername));
					holder.putView("tvElo", holder.itemView.findViewById(R.id.tvElo));
				},
				((holder, item, position) -> {
					((TextView) holder.getView("tvRank")).setText(getString(R.string.rank_format, position + 1));
					((ImageView) holder.getView("ivPfp")).setImageBitmap(item.getPictureBitmap());
					((TextView) holder.getView("tvUsername")).setText(item.getUsername());
					((TextView) holder.getView("tvElo")).setText(String.valueOf(Math.round(item.getElo())));

					if (currentUser != null && item.getUsername().equals(currentUser.getUsername())) {
						holder.itemView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
						((TextView) holder.getView("tvUsername")).setTypeface(null, Typeface.BOLD);
						((TextView) holder.getView("tvElo")).setTypeface(null, Typeface.BOLD);
					} else {
						holder.itemView.setBackgroundColor(Color.TRANSPARENT);
						((TextView) holder.getView("tvUsername")).setTypeface(null, Typeface.NORMAL);
						((TextView) holder.getView("tvElo")).setTypeface(null, Typeface.NORMAL);
					}
				})
		);
		rvLeaderboard.setAdapter(adapter);
		rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
	}
}