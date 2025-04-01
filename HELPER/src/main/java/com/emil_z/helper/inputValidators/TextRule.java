package com.emil_z.helper.inputValidators;

import android.view.View;
import android.widget.EditText;

import java.util.regex.Pattern;

public class TextRule extends Rule {
    protected int minimumLength;
    protected int maximumLength;
    protected boolean includeNumbers;
    protected boolean startsWithUpperCase;
    protected String regularExpression;

    public TextRule(View view, RuleOperation operation, String message) {
        super(view, operation, message);
    }

    public TextRule(View view, RuleOperation operation, String message, int minimumLength, int maximumLength, boolean includenumbers) {
        this(view, operation, message, minimumLength, maximumLength, includenumbers, false);
    }

    public TextRule(View view, RuleOperation operation, String message, int minimumLength, int maximumLength, boolean includenumbers, boolean startsWithUpperCase) {
        this(view, operation, message, minimumLength, maximumLength, includenumbers, startsWithUpperCase, null);
    }

    public TextRule(View view, RuleOperation operation, String message, int minimumLength, int maximumLength, boolean includenumbers, boolean startsWithUpperCase, String regularExpression){
        super(view, operation, message);

        this.minimumLength = minimumLength;
        this.maximumLength = maximumLength;

        if (minimumLength != 0)
            if (minimumLength >= 1)
                this.minimumLength = minimumLength;
            else
                this.minimumLength = 1;

        if (maximumLength != 0)
            if (maximumLength <= 1000)
                this.maximumLength = maximumLength;
            else
                this.maximumLength = 1000;

        this.includeNumbers = includenumbers;

        this.startsWithUpperCase = startsWithUpperCase;

        if (regularExpression == null || regularExpression.isEmpty()){
            this.regularExpression = "[a-zA-Z \\-]{" + minimumLength + "," + maximumLength + "}$";

            if (includenumbers)
                this.regularExpression = "[a-zA-Z0-9 \\-]{" + minimumLength + "," + maximumLength + "}$";
        }
        else{
            this.regularExpression = regularExpression;
        }
    }

    public int getMinimumLength() {
        return minimumLength;
    }

    public void setMinimumLength(int minimumLength) {
        this.minimumLength = minimumLength;
    }

    public int getMaximumLength() {
        return maximumLength;
    }

    public void setMaximumLength(int maximumLength) {
        this.maximumLength = maximumLength;
    }

    public String getRegularExpression() {
        return regularExpression;
    }

    public void setRegularExpression(String regularExpression) {
        this.regularExpression = regularExpression;
    }

    public boolean getStartsWithUpperCase(){
        return startsWithUpperCase;
    }

    public void setStartsWithUpperCase(boolean startsWithUpperCase){
        this.startsWithUpperCase = startsWithUpperCase;
    }

    public static boolean validate(TextRule rule){
        int     textLength = ((EditText)rule.getView()).getText().length();

        if (rule.getMinimumLength() > 0 && rule.getMaximumLength() > 0)
            if (rule.getMinimumLength() <= textLength && textLength <= rule.getMaximumLength())
                rule.isValid = true;
            else {
                rule.isValid = false;

                rule.setPerviousMessage(rule.getMessage());
                if (rule.getMinimumLength() != rule.getMaximumLength()) {
                    rule.setMessage("The length should be " + rule.getMinimumLength() + "-" + rule.getMaximumLength());
                }
                else {
                    rule.setMessage("The length should be at least " + rule.getMinimumLength());
                }
            }

        if (rule.isValid)
            if (rule.getRegularExpression() != null){
                rule.isValid = Pattern.compile(rule.getRegularExpression())
                               .matcher(((EditText)rule.getView()).getText())
                               .matches();
            }

        if (rule.isValid)
            if (rule.getStartsWithUpperCase()) {
                String s = String.valueOf(((EditText) rule.getView()).getText().charAt(0));
                //if (s != s.toUpperCase()) {
                if (!s.equals(s.toUpperCase())) {
                    rule.isValid = false;
                    rule.setMessage("First letter should be capital");
                }
            }

        return rule.isValid;
    }
}