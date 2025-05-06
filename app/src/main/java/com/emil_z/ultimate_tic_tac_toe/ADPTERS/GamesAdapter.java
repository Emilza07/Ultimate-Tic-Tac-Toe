package com.emil_z.ultimate_tic_tac_toe.ADPTERS;

import com.emil_z.model.Game;
import com.emil_z.ultimate_tic_tac_toe.ADPTERS.BASE.GenericAdapter;

import java.util.List;

public class GamesAdapter extends GenericAdapter<Game> {
	public GamesAdapter(List<Game> items,
						int layoutId,
						InitializeViewHolder initializeViewHolder,
						BindViewHolder<Game> bindViewHolder) {
		super(items,
				layoutId,
				initializeViewHolder,
				bindViewHolder);
	}
}