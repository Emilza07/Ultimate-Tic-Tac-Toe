package com.emil_z.ultimate_tic_tac_toe.ADPTERS.BASE;

import android.graphics.Color;

import com.emil_z.ultimate_tic_tac_toe.R;

public class SwipeConfig {

	//region LEFT
	public int leftBackgroundColor = Color.RED;
	public String leftText = "Delete";
	public int textLeftColor = Color.WHITE;
	public int leftIconResId = R.drawable.trashcan;
	//endregion

	//region Right
	public int rightBackgroundColor = Color.GREEN;
	public String rightText = "Archive";
	public int texRightColor = Color.WHITE;
	public int rightIconResId = R.drawable.archive;
	//endregion

	public float textSize = 0.3f; // As a fraction of item height
	public float iconSizeRatio = 0.5f; // Icon size as a fraction of item height
	public float iconMarginRatio = 0.15f; // Icon margin as a fraction of item height
}