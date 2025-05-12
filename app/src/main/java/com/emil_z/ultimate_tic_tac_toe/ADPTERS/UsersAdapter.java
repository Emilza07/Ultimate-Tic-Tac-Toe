package com.emil_z.ultimate_tic_tac_toe.ADPTERS;

import com.emil_z.model.User;
import com.emil_z.ultimate_tic_tac_toe.ADPTERS.BASE.GenericAdapter;

import java.util.List;

public class UsersAdapter extends GenericAdapter<User> {
	public UsersAdapter(List<User> items,
						int layoutId,
						InitializeViewHolder initializeViewHolder,
						BindViewHolder<User> bindViewHolder) {
		super(items,
				layoutId,
				initializeViewHolder,
				bindViewHolder);
	}
}