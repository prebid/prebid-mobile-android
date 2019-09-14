package org.prebid.mobile;

public class BidLog {
    private BidLogEntry mLastEntry;

    private static BidLog sInstance;

    public static BidLog getInstance() {
        if (sInstance == null) {
            sInstance = new BidLog();
        }
        return sInstance;
    }

    private BidLog() {

    }

    public BidLogEntry getLastBid() {
        return mLastEntry;
    }

    public void setLastEntry(BidLogEntry entry) {
        this.mLastEntry = entry;
    }

    public void cleanLog() {
        this.mLastEntry = null;
    }

    public static class BidLogEntry {
        private String requestUrl;
        private String requestBody;
        private int responseCode;
        private boolean containsTopBid;
        private String response;

        public BidLogEntry() {
            this.requestUrl = "";
            this.requestBody = "";
            this.responseCode = -1;
            this.containsTopBid = false;
            this.response = "";
        }

        public String getRequestUrl() {
            return requestUrl;
        }

        public void setRequestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        public String getRequestBody() {
            return requestBody;
        }

        public void setRequestBody(String requestBody) {
            this.requestBody = requestBody;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public boolean containsTopBid() {
            return containsTopBid;
        }

        public void setContainsTopBid(boolean containsTopBid) {
            this.containsTopBid = containsTopBid;
        }
    }
}
