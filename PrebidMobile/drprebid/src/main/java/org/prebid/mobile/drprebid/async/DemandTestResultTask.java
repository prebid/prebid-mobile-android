package org.prebid.mobile.drprebid.async;

public class DemandTestResultTask implements Runnable {
    public interface RequestCompletionListener {
        void onRequestCompleted(String response, int responseCode);
    }

    private final RequestCompletionListener listener;
    private String response;
    private int responseCode;

    public DemandTestResultTask(RequestCompletionListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        if (listener != null) {
            listener.onRequestCompleted(response, responseCode);
        }
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
