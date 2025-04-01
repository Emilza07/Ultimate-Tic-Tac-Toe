package com.emil_z.helper.inputValidators;

import android.view.View;
import android.widget.EditText;

public class CompareRule extends Rule{

    protected View viewToCompare;

    public CompareRule(View view, View viewToCompare, RuleOperation operation, String message) {
        super(view, operation, message);

        this.viewToCompare = viewToCompare;
    }

    public View getViewToCompare(){
        return viewToCompare;
    }

    public static boolean validate(CompareRule rule){
        if (Validator.requiredValidator(rule)){
            String value;
            String valueToCompare;

            if (rule.getViewToCompare() instanceof EditText) {
                if (((EditText) rule.getView()).getText() == null || ((EditText) rule.getView()).getText().toString().trim().isEmpty())
                    rule.isValid = false;
                else {
                    value          = ((EditText) rule.getView()).getText().toString();
                    valueToCompare = ((EditText) rule.getViewToCompare()).getText().toString();

                    rule.isValid = value.equals(valueToCompare);
                }
            }
        }
        else {
            rule.isValid = false;
        }

        return rule.isValid;
    }
}