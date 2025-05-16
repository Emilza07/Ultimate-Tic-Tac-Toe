package com.emil_z.ultimate_tic_tac_toe.ADPTERS;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.emil_z.model.Game;
import com.emil_z.ultimate_tic_tac_toe.ADPTERS.BASE.GenericAdapter;
import com.emil_z.ultimate_tic_tac_toe.R;

import java.util.List;

/**
 * Adapter for displaying a list of {@link Game} objects in a RecyclerView.
 * <p>
 * Supports two view types: a regular item view and a loading indicator view.
 * Extends {@link GenericAdapter} to provide generic binding and initialization logic.
 */
public class GamesAdapter extends GenericAdapter<Game> {
	private static final int VIEW_TYPE_ITEM = 0;
	private static final int VIEW_TYPE_LOADING = 1;

	/**
	 * Constructs a new GamesAdapter.
	 *
	 * @param items                The list of Game items to display.
	 * @param layoutId             The layout resource ID for item views.
	 * @param initializeViewHolder Callback to initialize the view holder.
	 * @param bindViewHolder       Callback to bind data to the view holder.
	 */
	public GamesAdapter(List<Game> items,
						int layoutId,
						InitializeViewHolder initializeViewHolder,
						BindViewHolder<Game> bindViewHolder) {
		super(items,
				layoutId,
				initializeViewHolder,
				bindViewHolder);
	}

	/**
	 * Returns the view type for the item at the given position.
	 * If the item is null, returns VIEW_TYPE_LOADING; otherwise, returns VIEW_TYPE_ITEM.
	 *
	 * @param position The position of the item.
	 * @return The view type constant.
	 */
	@Override
	public int getItemViewType(int position) {
		return getItems().get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
	}

	/**
	 * Creates a new view holder for the specified view type.
	 * Inflates a loading view if the view type is VIEW_TYPE_LOADING; otherwise, delegates to the superclass.
	 *
	 * @param parent   The parent ViewGroup.
	 * @param viewType The type of view to create.
	 * @return A new GenericViewHolder instance.
	 */
	@NonNull
	@Override
	public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == VIEW_TYPE_LOADING) {
			View view = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_loading, parent, false);
			return new GenericViewHolder(view);
		}
		return super.onCreateViewHolder(parent, viewType);
	}

	/**
	 * Binds data to the view holder at the specified position.
	 * Skips binding if the view type is VIEW_TYPE_LOADING.
	 *
	 * @param holder   The view holder to bind.
	 * @param position The position of the item.
	 */
	@Override
	public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
		if (getItemViewType(position) == VIEW_TYPE_LOADING) return;
		super.onBindViewHolder(holder, position);
	}
}