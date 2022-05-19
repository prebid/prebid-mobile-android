package org.prebid.mobile.api.exceptions;

public class InitError {

    private String error;

    public InitError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

}
