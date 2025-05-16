package com.emil_z.helper;

import android.view.View;
import android.view.ViewParent;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Utility class for handling error transfer and layout retrieval
 * for {@link TextInputEditText} and {@link TextInputLayout}.
 */
public class TextInputLayoutUtil {

	/**
	 * Transfers the error message from a {@link TextInputEditText} to its parent {@link TextInputLayout}.
	 * <p>
	 * If the provided view is a {@link TextInputEditText}, this method finds its parent
	 * {@link TextInputLayout} and sets the error message on it. The error is then cleared
	 * from the edit text. If there is no error, error display is disabled on the layout.
	 * </p>
	 *
	 * @param view the view to transfer errors from (should be a {@link TextInputEditText})
	 */
	public static void transferErrorsToTextInputLayout(View view) {
		if (view instanceof TextInputEditText) {
			TextInputEditText editText = (TextInputEditText) view;
			CharSequence error = editText.getError();

			ViewParent parent = editText.getParent();
			while (parent != null) {
				if (parent instanceof TextInputLayout) {
					TextInputLayout textInputLayout = (TextInputLayout) parent;
					textInputLayout.setError(error);
					if (error != null)
						editText.setError(null);
					else
						textInputLayout.setErrorEnabled(false);
					break;
				}
				parent = parent.getParent();
			}
		}
	}

	/**
	 * Finds the parent {@link TextInputLayout} of a given {@link TextInputEditText} view.
	 *
	 * @param view the view whose parent layout is to be found (should be a {@link TextInputEditText})
	 * @return the parent {@link TextInputLayout} if found, otherwise {@code null}
	 */
	public static TextInputLayout getTextInputLayout(View view) {
		if (view instanceof TextInputEditText) {
			ViewParent parent = view.getParent();
			while (parent != null) {
				if (parent instanceof TextInputLayout) {
					return (TextInputLayout) parent;
				}
				parent = parent.getParent();
			}
		}
		return null;
	}
}