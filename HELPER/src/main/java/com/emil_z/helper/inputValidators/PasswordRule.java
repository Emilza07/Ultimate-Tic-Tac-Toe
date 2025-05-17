package com.emil_z.helper.inputValidators;

import android.view.View;

public class PasswordRule extends TextRule{
    public PasswordRule(View view, RuleOperation operation, String message) {
        super(view, operation, message);

        regularExpression = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[a-zA-Z\\d!@#$%^&*()_+]{4,8}$";
        minimumLength = 4;
        maximumLength = 8;
    }

    public PasswordRule(View view, RuleOperation operation, String message, int minimumLength, int maximumLength) {
        this(view, operation, message, minimumLength, maximumLength, null);

        regularExpression = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[`!@#$%^&*()-_+={}|;:',.?<>/])[a-zA-Z\\d`!@#$%^&*()-_+={}|;:',.?<>/]{" + minimumLength + "," + maximumLength +"}$";
    }

    public PasswordRule(View view, RuleOperation operation, String message, int minimumLength, int maximumLength, String regularExpression) {
        super(view, operation, message, minimumLength, maximumLength, false);

        this.regularExpression = regularExpression;
    }

     public static boolean validate(PasswordRule rule){
        if (Validator.requiredValidator(rule)) {
            rule.isValid = TextRule.validate(rule);
        }
        else {
            rule.isValid = false;
        }

        return rule.isValid;
    }
}