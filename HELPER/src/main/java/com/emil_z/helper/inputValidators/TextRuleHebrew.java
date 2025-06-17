package com.emil_z.helper.inputValidators;

import android.view.View;

public class TextRuleHebrew extends TextRule{
    public TextRuleHebrew(View view, RuleOperation operation, String message, int minimumLength, int maximumLength, boolean includeNumbers) {
        super(view, operation, message, minimumLength, maximumLength, includeNumbers);

        this.regularExpression = "^[א-ת\\s'-]*$";

        if (includeNumbers)
            this.regularExpression = "^[א-ת0-9\\s'-]*$";
    }

    public TextRuleHebrew(View view, RuleOperation operation, String message, int minimumLength, int maximumLength, boolean includeNumbers, String regularExpression) {
        super(view, operation, message, minimumLength, maximumLength, includeNumbers, false, regularExpression);
    }

    public static boolean validate (TextRuleHebrew rule){
        rule.isValid = TextRule.validate(rule);
        return rule.isValid;
    }
}