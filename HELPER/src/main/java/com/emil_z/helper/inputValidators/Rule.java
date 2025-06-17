package com.emil_z.helper.inputValidators;

import android.view.View;

public class Rule {
    protected View view;
    protected com.emil_z.helper.inputValidators.RuleOperation operation;
    protected String message;
    protected boolean isValid;
    protected String previousMessage;

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

    public String getPreviousMessage(){
        return previousMessage;
    }

    public void setPreviousMessage(String message) {this.previousMessage = message; }

    public boolean getIsValid(){
        return isValid;
    }
}