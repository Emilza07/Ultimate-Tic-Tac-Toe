package com.emil_z.helper.inputValidators;

import android.view.View;

public class PhoneRule extends TextRule {

    public PhoneRule(View view, RuleOperation operation, String message) {
        this(view, operation, message, null);

        regularExpression = "(\\+?[1-9]\\d{0,2})?[- ]?(\\(\\d{0,3}\\)|\\d{0,3})[- ]?\\d{3}[- ]?\\d{2}[- ]?\\d{2}$";
    }

    public PhoneRule(View view, RuleOperation operation, String message, String emailRegEx) {
        super(view, operation, message, 1, 100, false, false, emailRegEx);
    }
}