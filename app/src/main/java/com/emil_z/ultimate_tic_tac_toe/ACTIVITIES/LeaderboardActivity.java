package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emil_z.model.Users;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.ADPTERS.UsersAdapter;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;

import java.util.ArrayList;

/**
 * Activity that displays the leaderboard of top players.
 * <p>
 * Shows a paginated list of users sorted by ELO, highlights the current user,
 * and loads more users as the user scrolls.
 */
public class LeaderboardActivity extends BaseActivity {
	private static final int PAGE_SIZE = 15;

	private RecyclerView rvLeaderboard;

	private UsersViewModel viewModel;
	private UsersAdapter adapter;

	private Users users;
	private boolean isLoading = false;
	private float lastLoadedElo = -1;
	private String lastLoadedIdFs = null;

	/**
	 * Initializes the leaderboard activity, sets up UI, listeners, ViewModel, and adapter.
	 *
	 * @param savedInstanceState The previously saved instance state, if any.
	 */
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
		setupRecyclerViewScrollListener();
		setViewModel();
		setAdapter();
		showLoadingMore();
	}

	/**
	 * Initializes view components for the leaderboard screen.
	 */
	@SuppressWarnings("ConstantConditions")
	@Override
	public void initializeViews() {
		rvLeaderboard = findViewById(R.id.rvLeaderboard);

		DividerItemDecoration divider = new DividerItemDecoration(rvLeaderboard.getContext(), LinearLayoutManager.VERTICAL);
		divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.dividor));
		rvLeaderboard.addItemDecoration(divider);
	}

	/**
	 * Sets up click listeners for the leaderboard (none in this implementation).
	 */
	@Override
	public void setListeners() {
	}

	/**
	 * Sets up a scroll listener for the RecyclerView to load more users when reaching the end.
	 */
	@SuppressWarnings("ConstantConditions")
	private void setupRecyclerViewScrollListener() {
		rvLeaderboard.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);

				LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
				int visibleItemCount = layoutManager.getChildCount();
				int totalItemCount = layoutManager.getItemCount();
				int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

				if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
					&& firstVisibleItemPosition >= 0
					&& totalItemCount >= PAGE_SIZE) {
					loadUsers(true);
				}
			}
		});
	}

	/**
	 * Initializes the ViewModel, loads users, and observes user data changes.
	 */
	@Override
	public void setViewModel() {
		viewModel = new ViewModelProvider(this).get(UsersViewModel.class);
		loadUsers(false);

		viewModel.getLiveDataCollection().observe(this, newUsers -> {
			if (adapter.getItems() != null) {
				int lastIndex = adapter.getItems().indexOf(null);
				if (lastIndex != -1) {
					adapter.getItems().remove(lastIndex);
					adapter.notifyItemRemoved(lastIndex);
				}
			}

			if (!newUsers.isEmpty()) {
				if (this.users == null)
					this.users = newUsers;
				else
					this.users.addAll(newUsers);
				isLoading = false;
				lastLoadedElo = newUsers.get(newUsers.size() - 1).getElo();
				lastLoadedIdFs = newUsers.get(newUsers.size() - 1).getIdFs();
				adapter.setItems(this.users);
			}
		});
	}

	/**
	 * Sets up the adapter for the leaderboard RecyclerView and handles item display.
	 */
	private void setAdapter() {
		adapter = new UsersAdapter(new ArrayList<>(),
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

				if (currentUser != null && item.getIdFs().equals(currentUser.getIdFs())) {
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

	/**
	 * Loads users for the leaderboard, paginated. Shows loading indicator if loading more.
	 *
	 * @param loadMore Whether to load more users (pagination).
	 */
	private void loadUsers(boolean loadMore) {
		isLoading = true;
		if (loadMore) {
			showLoadingMore();
		}
		viewModel.getTopPlayersPaginated(PAGE_SIZE, lastLoadedElo, lastLoadedIdFs);
	}

	/**
	 * Shows a loading indicator in the leaderboard list.
	 */
	private void showLoadingMore() {
		adapter.getItems().add(null);
		adapter.notifyItemInserted(adapter.getItemCount() - 1);
	}
}