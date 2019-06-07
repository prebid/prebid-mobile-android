package org.prebid.mobile.drprebid.model;

public class HttpResponse {
    private int responseCode;
    private String response;
    private Exception exception;

    private HttpResponse() {
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponse() {
        return response;
    }

    public Exception getException() {
        return exception;
    }
}
