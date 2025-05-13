package com.emil_z.repository;

import android.app.Application;
import android.graphics.Point;

import com.emil_z.model.LocalGame;
import com.emil_z.model.Player;

public class LocalGamesRepository extends BaseGamesRepository {
	public LocalGamesRepository(Application application) {
		super(application);
	}

	@Override
	public void startGame(Player player, String crossPlayerIdFs) {
		lvGame.setValue(new LocalGame(localPlayerIdFs));
		lvIsStarted.setValue(true);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	protected void checkInnerBoardFinish(Point innerBoard) {
		super.checkInnerBoardFinish(innerBoard);
		if (lvGame.getValue().isFinished())
			lvIsFinished.setValue(true);
	}
}