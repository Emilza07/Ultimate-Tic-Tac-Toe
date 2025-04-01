package com.emil_z.helper.inputValidators;

import android.view.View;

public class NameRule extends TextRule{

    public NameRule(View view, RuleOperation operation, String message) {
        super(view, operation, message);

        minimumLength       = 2;
        maximumLength       = 20;
        includeNumbers      = false;
        startsWithUpperCase = true;
    }
}