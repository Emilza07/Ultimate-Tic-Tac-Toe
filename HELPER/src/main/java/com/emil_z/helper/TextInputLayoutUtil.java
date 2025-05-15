package com.emil_z.helper;

import android.view.View;
import android.view.ViewParent;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TextInputLayoutUtil {
	public static void transferErrorsToTextInputLayout(View view) {
		if (view instanceof TextInputEditText) {
			TextInputEditText editText = (TextInputEditText) view;
			CharSequence error = editText.getError();

			ViewParent parent = editText.getParent();
			while (parent != null) {
				if (parent instanceof TextInputLayout) {
					TextInputLayout textInputLayout = (TextInputLayout) parent;
					if (error != null) {
						textInputLayout.setError(error);
						editText.setError(null);
					} else {
						textInputLayout.setError(null);
						textInputLayout.setErrorEnabled(false);
					}
					break;
				}
				parent = parent.getParent();
			}
		}
	}

	public static TextInputLayout getTextInputLayout(View view) {
		if (view instanceof TextInputEditText) {
			TextInputEditText editText = (TextInputEditText) view;
			ViewParent parent = editText.getParent();
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