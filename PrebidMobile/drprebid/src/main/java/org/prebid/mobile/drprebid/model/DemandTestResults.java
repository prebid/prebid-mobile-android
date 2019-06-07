package org.prebid.mobile.drprebid.model;

import java.util.HashMap;
import java.util.Map;

public class DemandTestResults {
    private Map<String, String> bidders;
    private Exception error;
    private int responseStatus;
    private String request;
    private int totalBids;

    public DemandTestResults(String request) {
        this.bidders = new HashMap<>();
        this.error = null;
        this.responseStatus = 200;
        this.request = request;
        this.totalBids = 0;
    }

    public Map<String, String> getBidders() {
        return bidders;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public int getTotalBids() {
        return totalBids;
    }

    public void setTotalBids(int totalBids) {
        this.totalBids = totalBids;
    }
}
