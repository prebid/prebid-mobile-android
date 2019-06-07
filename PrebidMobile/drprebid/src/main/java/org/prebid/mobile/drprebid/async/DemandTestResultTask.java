package org.prebid.mobile.drprebid.async;

import org.prebid.mobile.drprebid.model.DemandTestResponse;

public class DemandTestResultTask implements Runnable {
    public interface RequestCompletionListener {
        void onRequestCompleted(DemandTestResponse response);
    }

    private final RequestCompletionListener listener;
    private DemandTestResponse response;

    public DemandTestResultTask(RequestCompletionListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        if (listener != null) {
            listener.onRequestCompleted(response);
        }
    }

    public void setResponse(DemandTestResponse response) {
        this.response = response;
    }
}
