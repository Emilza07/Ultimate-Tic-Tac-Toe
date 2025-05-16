package com.emil_z.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.emil_z.model.GameType;
import com.emil_z.viewmodel.BASE.GenericViewModelFactory;

/**
 * Factory class for creating instances of {@link GamesViewModel}.
 * Extends {@link GenericViewModelFactory} to provide custom construction logic
 * for {@link GamesViewModel} with a specific {@link GameType}.
 */
public class GamesViewModelFactory extends GenericViewModelFactory<GamesViewModel> {
	private final GameType gameType;

	/**
	 * Constructs a new {@code GamesViewModelFactory}.
	 *
	 * @param application the application context
	 * @param gameType the type of game to be used in the ViewModel
	 */
	public GamesViewModelFactory(Application application, GameType gameType) {
		super(application, app -> new GamesViewModel(app, gameType));
		this.gameType = gameType;
	}

	/**
	 * Creates a new instance of the specified {@link ViewModel} class.
	 * If the requested class is {@link GamesViewModel}, returns a new instance
	 * with the provided application and game type. Otherwise, delegates to the superclass.
	 *
	 * @param modelClass the class of the ViewModel to create
	 * @param <T> the type parameter for the ViewModel
	 * @return a new instance of the requested ViewModel
	 */
	@NonNull
	@Override
	public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
		if (modelClass.isAssignableFrom(GamesViewModel.class))
			return (T) new GamesViewModel(getApplication(), gameType);
		return super.create(modelClass);
	}
}