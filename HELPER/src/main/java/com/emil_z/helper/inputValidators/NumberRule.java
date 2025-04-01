package com.emil_z.helper.inputValidators;

import android.view.View;
import android.widget.EditText;

public class NumberRule extends Rule{

    private double  lowBound;
    private double  upBound;
    private boolean isInt;

     public NumberRule(View view, RuleOperation operation, String message, int lowBound, int upBound) {
        super(view, operation, message);

        this.lowBound = lowBound;
        this.upBound = upBound;
        isInt = true;
    }

    public NumberRule(View view, RuleOperation operation, String message, double lowBound, double upBound) {
        super(view, operation, message);

        this.lowBound = lowBound;
        this.upBound = upBound;
        isInt = false;
    }

     public double getLowBound() {
        return lowBound;
    }

    public double getUpBound() {
        return upBound;
    }

    public boolean getIsInt() { return isInt; }

    public boolean isInteger(double number){
        return Math.ceil(number) == Math.floor(number);
    }

    public static boolean validate(NumberRule rule){
        if(Validator.requiredValidator(rule)){
            String value =  ((EditText) rule.getView()).getText().toString();
            int     checkedInt;
            int     intLowBound;
            int     intUpBound;
            double  checkedDouble;

            if (rule.getIsInt()) {
                try{
                    checkedInt = Integer.parseInt(value);
                    intLowBound = (int)rule.getLowBound();
                    intUpBound  = (int)rule.getUpBound();

                    rule.isValid = intLowBound <= checkedInt && checkedInt <= intUpBound;
                }
                catch (Exception e){
                    rule.isValid = false;
                }
            }
            else {
                try{
                    checkedDouble = Double.parseDouble(value);

                    rule.isValid = rule.getLowBound() <= checkedDouble && checkedDouble <= rule.getUpBound();
                }
                catch (Exception e){
                    rule.isValid = false;
                }
            }

            return rule.isValid;
        }
        else
            return false;
    }
}