package com.emil_z.repository;

import android.app.Application;
import android.graphics.Point;

import com.emil_z.model.BoardLocation;
import com.emil_z.model.CPU;
import com.emil_z.model.CpuGame;
import com.emil_z.model.Player;

public class CpuGamesRepository extends BaseGamesRepository {
	public CpuGamesRepository(Application application) {
		super(application);
	}

	@Override
	public void startGame(Player player, String crossPlayerIdFs) {
		lvGame.setValue(new CpuGame(localPlayerIdFs, crossPlayerIdFs));
		lvIsStarted.setValue(true);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	protected void checkInnerBoardFinish(Point innerBoard) {
		super.checkInnerBoardFinish(innerBoard);
		if (lvGame.getValue().isFinished())
			lvIsFinished.postValue(true);
	}

	@SuppressWarnings("ConstantConditions")
	public void makeCpuMove() {
		BoardLocation location = CPU.findBestMove(lvGame.getValue().getOuterBoard());
		lvGame.getValue().makeMove(location);
		lvGame.postValue(lvGame.getValue());
		checkInnerBoardFinish(location.getOuter());
	}
}