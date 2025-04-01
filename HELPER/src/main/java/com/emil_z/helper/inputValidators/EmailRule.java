package com.emil_z.helper.inputValidators;

import android.view.View;

public class EmailRule extends TextRule {

    public EmailRule(View view, RuleOperation operation, String message) {
        this(view, operation, message, null);

        regularExpression ="^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?:\\.[a-zA-Z]{2,})?$";
    }

    public EmailRule(View view, RuleOperation operation, String message, String emailRegEx) {
        super(view, operation, message, 1, 100, false, false, emailRegEx);
    }
}