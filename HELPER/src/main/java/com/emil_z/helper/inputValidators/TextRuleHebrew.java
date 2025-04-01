package com.emil_z.helper.inputValidators;

import android.view.View;

public class TextRuleHebrew extends TextRule{
    public TextRuleHebrew(View view, RuleOperation operation, String message, int minimumLength, int maximumLength, boolean includenumbers) {
        super(view, operation, message, minimumLength, maximumLength, includenumbers);

        this.regularExpression = "^[א-ת\\s'-]*$";

        if (includenumbers)
            this.regularExpression = "^[א-ת0-9\\s'-]*$";
    }

    public TextRuleHebrew(View view, RuleOperation operation, String message, int minimumLength, int maximumLength, boolean includenumbers, String regularExpression) {
        super(view, operation, message, minimumLength, maximumLength, includenumbers, false, regularExpression);
    }

    public static boolean validate (TextRuleHebrew rule){
        rule.isValid = TextRule.validate(rule);
        return rule.isValid;
    }
}