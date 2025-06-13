package com.emil_z.ultimate_tic_tac_toe.ADPTERS.BASE;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeCallback<T> extends ItemTouchHelper.SimpleCallback {

	private GenericAdapter<T> adapter;
	private Paint paint;// To redraw the Item while swipe
	private Context context;
	private Drawable leftIcon;
	private Drawable rightIcon;
	private int swipeDirs;
	private SwipeConfig config;


	public SwipeCallback(GenericAdapter<T> adapter, Context context, SwipeConfig config) {
		this(adapter, context, config, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
	}

	public SwipeCallback(GenericAdapter<T> adapter, Context context, SwipeConfig config, int swipeDirs) {
		super(0, swipeDirs);
		//  super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
		this.adapter = adapter;
		this.context = context;
		paint = new Paint();
		paint.setAntiAlias(true);
		this.swipeDirs = swipeDirs;
		this.config = config;

		// Initialize icons
		////leftIcon = ContextCompat.getDrawable(context, R.drawable.trashcan); // Make sure you have this drawable
		////rightIcon = ContextCompat.getDrawable(context, R.drawable.archive);

		leftIcon = ContextCompat.getDrawable(context, config.leftIconResId);
		rightIcon = ContextCompat.getDrawable(context, config.rightIconResId);

	}

	@Override
	public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
		return swipeDirs;
	}

	@Override
	public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
		return false; // We don't want to support moving items up/down
	}

	@Override
	public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
		int position = viewHolder.getAdapterPosition(); // getBindingAdapterPosition()
		if (position == RecyclerView.NO_POSITION) {
			return; // Item has been removed from the adapter
		}

		if (adapter.getSwipeListener() != null) {
			T item = adapter.getItems().get(position);
			if (direction == ItemTouchHelper.RIGHT) {
				adapter.getSwipeListener().onItemSwipeRight(item, position);
			} else if (direction == ItemTouchHelper.LEFT) {
				adapter.getSwipeListener().onItemSwipeLeft(item, position);
			}
		}
	}

	@Override
	public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
			View itemView = viewHolder.itemView;
			float height = (float) itemView.getBottom() - (float) itemView.getTop();
			float width = height / 3;
			float cornerRadius = height / 4; // You can adjust this value to change the roundness

			Path path = new Path();
			float[] corners = new float[]{
				cornerRadius, cornerRadius,        // Top left radius in px
				cornerRadius, cornerRadius,        // Top right radius in px
				cornerRadius, cornerRadius,          // Bottom right radius in px
				cornerRadius, cornerRadius           // Bottom left radius in px
			};

			if (dX > 0) {
				// Swiping to the right
				//paint.setColor(Color.GREEN);
				paint.setColor(config.rightBackgroundColor);
				RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
				path.addRoundRect(background, corners, Path.Direction.CW);
				c.drawPath(path, paint);

				// Draw archive icon
				if (rightIcon != null) {
					//int iconSize = (int) (height * 0.5);
					int iconSize = (int) (height * config.iconSizeRatio);
					//int iconMargin = (int) (height * 0.15);
					int iconMargin = (int) (height * config.iconMarginRatio);
					int iconTop = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
					int iconRight = itemView.getLeft() + (int) dX - iconMargin;
					int iconLeft = iconRight - iconSize;
					int iconBottom = iconTop + iconSize;

					rightIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
					rightIcon.draw(c);

					// Draw "Archive" text
					//paint.setColor(Color.WHITE);
					paint.setColor(config.texRightColor);
					//paint.setTextSize(height * 0.3f);
					paint.setTextSize(height * config.textSize);
					//String archiveText = "Archive";
					String rightText = config.rightText;
					float textWidth = paint.measureText(rightText);
					float textRight = iconLeft - iconMargin;
					float textLeft = textRight - textWidth;
					float textY = itemView.getTop() + (itemView.getHeight() / 2f) + (paint.getTextSize() / 2f) - paint.descent();

					// Only draw text if there's enough space
					if (textLeft > itemView.getLeft()) {
						c.drawText(rightText, textLeft, textY, paint);
					}
				}
			} else if (dX < 0) {
				// Swiping to the left
				//paint.setColor(Color.RED);
				paint.setColor(config.leftBackgroundColor);
				RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
				path.addRoundRect(background, corners, Path.Direction.CW);
				c.drawPath(path, paint);

				if (leftIcon != null) {
					//int iconSize = (int) (height * 0.5);
					int iconSize = (int) (height * config.iconSizeRatio);
					//int iconMargin = (int) (height * 0.15);
					int iconMargin = (int) (height * config.iconMarginRatio);
					int iconTop = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
					int iconLeft = itemView.getRight() + (int) dX + iconMargin;
					int iconRight = iconLeft + iconSize;
					int iconBottom = iconTop + iconSize;

					leftIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
					leftIcon.draw(c);

					// Draw "Delete" text
					//paint.setColor(Color.WHITE);
					paint.setColor(config.textLeftColor);
					//paint.setTextSize(height * 0.3f);
					paint.setTextSize(height * config.textSize);
					//String deleteText = "Delete";
					String leftText = config.leftText;
					float textWidth = paint.measureText(leftText);
					float textX = iconRight + iconMargin;
					float textY = itemView.getTop() + (itemView.getHeight() / 2f) + (paint.getTextSize() / 2f) - paint.descent();

					// Only draw text if there's enough space
					if (textX + textWidth < itemView.getRight()) {
						c.drawText(leftText, textX, textY, paint);
					}
				}
			}
		}
		super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
	}
}