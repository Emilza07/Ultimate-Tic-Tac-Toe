package com.emil_z.ultimate_tic_tac_toe.ADPTERS;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.emil_z.model.User;
import com.emil_z.ultimate_tic_tac_toe.ADPTERS.BASE.GenericAdapter;
import com.emil_z.ultimate_tic_tac_toe.R;

import java.util.List;

public class UsersAdapter extends GenericAdapter<User> {
	private static final int VIEW_TYPE_ITEM = 0;
	private static final int VIEW_TYPE_LOADING = 1;

	public UsersAdapter(List<User> items,
						int layoutId,
						InitializeViewHolder initializeViewHolder,
						BindViewHolder<User> bindViewHolder) {
		super(items,
				layoutId,
				initializeViewHolder,
				bindViewHolder);
	}

	@Override
	public int getItemViewType(int position) {
		return getItems().get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
	}

	@NonNull
	@Override
	public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == VIEW_TYPE_LOADING) {
			View view = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_loading, parent, false);
			return new GenericViewHolder(view);
		} else {
			return super.onCreateViewHolder(parent, viewType);
		}
	}

	@Override
	public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
		if (getItemViewType(position) == VIEW_TYPE_LOADING) {
			// No binding needed for loading
			return;
		}
		super.onBindViewHolder(holder, position);
	}
}