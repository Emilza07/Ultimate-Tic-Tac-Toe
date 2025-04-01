package com.emil_z.helper;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressBarUtil {

    /**
     * This function shows a spinner Progress bar default color without message
     *
     * @param context       context usually this
     * @param view          the view to display the progress bar
     * @return Progress Bar
     */
    public static ProgressBar showProgress(Context context, LinearLayout view){
        return ProgressBarUtil.showProgress(context, view, null);
    }

    /**
     * This function shows a spinner Progress bar with specified color without message
     *
     * @param context       context usually this
     * @param view          the view to display the progress bar
     * @param color         progress bar and message color
     * @return Progress Bar
     */
    public static ProgressBar showProgress(Context context, LinearLayout view, int color){
        return ProgressBarUtil.showProgress(context, view, null, color);
    }

    /**
     * This function shows a spinner Progress bar default color
     *
     * @param context       context usually this
     * @param view          the view to display the progress bar
     * @param message       optional message
     * @return Progress Bar
     */
    public static ProgressBar showProgress(Context context, LinearLayout view, String message){
        return ProgressBarUtil.showProgress(context, view, message, false, -1);
    }

    /**
     * This function shows a spinner Progress bar with specified color
     *
     * @param context       context usually this
     * @param view          the view to display the progress bar
     * @param message       optional message
     * @param color         progress bar and message color
     * @return Progress Bar
     */
    public static ProgressBar showProgress(Context context, LinearLayout view, String message, int color){
        return ProgressBarUtil.showProgress(context, view, message, false, color);
    }

    /**
     * This function shows a Progress bar
     *
     * @param context       context usually this
     * @param view          the view to display the progress bar
     * @param message       optional message
     * @param horizontal    true - horizontal, false - spinner
     * @param color         progress bar and message color
     * @return Progress Bar
     */
    public static ProgressBar showProgress(Context context, LinearLayout view, String message, boolean horizontal, int color){
        ProgressBar progressBar;
        TextView textView;

        if (view != null) {
            if (horizontal)
                progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
            else
                progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);

            progressBar.setIndeterminate(true);

            if (color != -1)
                progressBar.getIndeterminateDrawable().setColorFilter(color ,android.graphics.PorterDuff.Mode.MULTIPLY);

            view.addView(progressBar);

            if (!StringUtil.isNullOrEmpty(message)) {
                textView = new TextView(context);
                textView.setText(message);
                textView.setTextSize(18);
                if (color != -1)
                    textView.setTextColor(color);
                view.addView(textView);
            }
            return progressBar;
        }
        else
            return null;
    }
}