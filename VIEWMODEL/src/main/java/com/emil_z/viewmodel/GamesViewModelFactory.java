package com.emil_z.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import com.emil_z.model.GameType;
import com.emil_z.viewmodel.BASE.GenericViewModelFactory;

public class GamesViewModelFactory extends GenericViewModelFactory<GamesViewModel> {
	private final GameType gameType;

	public GamesViewModelFactory(Application application, GameType gameType) {
		super(application, app -> new GamesViewModel(app, gameType));
		this.gameType = gameType;
	}

	@NonNull
	@Override
	public <T extends ViewModel> T create(
			@NonNull Class<T> modelClass) {
		if (modelClass.isAssignableFrom(GamesViewModel.class)) {
			return (T) new GamesViewModel(getApplication(), gameType);
		}
		return super.create(modelClass);
	}
}