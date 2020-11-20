package org.prebid.mobile.utils;

import java.util.List;
import java.util.Map;

public class HTTPResponse {
    private boolean succeeded;
    private String responseBody;
    private Map<String, List<String>> headers;
    private HttpErrorCode errorCode;

    public HTTPResponse() {

    }

    public HTTPResponse(boolean succeeded, String responseBody, Map<String, List<String>> headers) {
        this.succeeded = succeeded;
        this.responseBody = responseBody;
        this.headers = headers;
    }

    public boolean getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public void setErrorCode(HttpErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public HttpErrorCode getErrorCode() {
        return errorCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }
}
