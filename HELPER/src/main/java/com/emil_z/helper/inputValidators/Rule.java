package com.emil_z.helper.inputValidators;

import android.view.View;

public class Rule {
    protected View view;
    protected com.emil_z.helper.inputValidators.RuleOperation operation;
    protected String message;
    protected boolean isValid;
    protected String perviousMessage;

    public Rule(View view, RuleOperation operation, String message) {
        this.view      = view;
        this.operation = operation;
        this.message   = message;
    }

    public View getView() {
        return view;
    }

    public RuleOperation getOperation() {
        return operation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getPerviousMessage(){
        return perviousMessage;
    }

    public void setPerviousMessage(String message) {this.perviousMessage = message; }

    public boolean getIsValid(){
        return isValid;
    }
}