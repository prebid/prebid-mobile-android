package org.prebid.mobile.drprebid.model;

import java.util.HashMap;
import java.util.Map;

public class DemandTestResults {
    private Map<String, Bidder> bidders;
    private Exception error;
    private int responseStatus;
    private String request;
    private int totalBids;
    private float avgEcpm;
    private long avgResponseTime;

    public DemandTestResults(String request) {
        this.bidders = new HashMap<>();
        this.error = null;
        this.responseStatus = 200;
        this.request = request;
        this.totalBids = 0;
        this.avgEcpm = 0.0f;
        this.avgResponseTime = 0;
    }

    public Map<String, Bidder> getBidders() {
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

    public float getAvgEcpm() {
        return avgEcpm;
    }

    public void setAvgEcpm(float avgEcpm) {
        this.avgEcpm = avgEcpm;
    }

    public long getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(long avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }
}
