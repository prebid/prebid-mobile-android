package org.prebid.mobile.core;


import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

class NewDemandFetcher {
    enum STATE {
        STOPPED,
        SINGLE_REQUEST,
        AUTO_REFRESH
    }

    private STATE state;
    private int period;
    private Object adObject;
    private NewOnCompleteListener listener;
    private Handler handler;
    private RequestRunnable requestRunnable;
    private long lastFetchTime = -1;
    private long timePausedAt = -1;
    private String configId;
    private ArrayList<AdSize> sizes;
    private AdType adType;
    private WeakReference<Context> weakReference;

    NewDemandFetcher(@NonNull Object adObj, @NonNull Context context, @NonNull NewOnCompleteListener listener, String configId, ArrayList<AdSize> sizes, AdType adType) {
        this.state = STATE.STOPPED;
        this.period = 0;
        this.adObject = adObj;
        this.listener = listener;
        this.handler = new Handler();
        this.weakReference = new WeakReference<Context>(context);
        this.configId = configId;
        this.sizes = sizes;
        this.adType = adType;
    }


    void setPeriod(int period) {
        boolean periodChanged = this.period != period;
        this.period = period;
        if ((periodChanged) && !state.equals(STATE.STOPPED)) {
            stop();
            start();
        }
    }

    void stop() {
        requestRunnable.stopRequest();
        handler.removeCallbacks(requestRunnable);
        // cancel existing requests
        timePausedAt = System.currentTimeMillis();
        state = STATE.STOPPED;
    }

    void start() {
        switch (state) {
            case STOPPED:
                this.requestRunnable = new RequestRunnable();
                if (this.period <= 0) {
                    // start a single request
                    handler.post(requestRunnable);
                    state = STATE.SINGLE_REQUEST;
                } else {
                    // Start recurring ad requests
                    final int msPeriod = period; // refresh period
                    final long stall; // delay millis for the initial request
                    if (timePausedAt != -1 && lastFetchTime != -1) {
                        //Clamp the stall between 0 and the period. Ads should never be requested on
                        //a delay longer than the period
                        stall = Math.min(msPeriod, Math.max(0, msPeriod - (timePausedAt - lastFetchTime)));
                    } else {
                        stall = 0;
                    }
                    handler.postDelayed(requestRunnable, stall * 1000);
                    state = STATE.AUTO_REFRESH;
                }
                break;
            case SINGLE_REQUEST:
                // start a single request
                this.requestRunnable = new RequestRunnable();
                handler.post(requestRunnable);
                break;
            case AUTO_REFRESH:
                break;
        }
    }

    Object getAdObject() {
        return this.adObject;
    }

    class RequestRunnable implements Runnable {
        private NewDemandAdapter demandAdapter;

        RequestRunnable() {
            this.demandAdapter = new TestDemand();
        }

        void stopRequest() {
            this.demandAdapter.stopRequest();
        }

        @Override
        public void run() {
            lastFetchTime = System.currentTimeMillis();
            Context context = NewDemandFetcher.this.weakReference.get();
            if (TextUtils.isEmpty(configId) || context == null) {
                if (listener != null) {
                    listener.onComplete(NewResultCode.INVALID_REQUEST);
                }
                return;
            }
            if (adType == AdType.BANNER) {
                if (sizes == null || sizes.isEmpty()) {
                    if (listener != null) {
                        listener.onComplete(NewResultCode.INVALID_REQUEST);
                    }
                    return;
                }
            }
            RequestParams requestParams = new RequestParams(configId, adType, sizes);
            this.demandAdapter.requestDemand(context, requestParams, new NewDemandAdapter.NewDemandAdapterListener() {
                @Override
                public void onDemandReady(HashMap<String, String> demand) {
                    NewAdUnit.apply(demand, NewDemandFetcher.this.adObject);
                    if (NewDemandFetcher.this.listener != null) {
                        NewDemandFetcher.this.listener.onComplete(NewResultCode.SUCCESS);
                    }
                }

                @Override
                public void onDemandFailed(NewResultCode resultCode) {
                    if (NewDemandFetcher.this.listener != null) {
                        NewDemandFetcher.this.listener.onComplete(resultCode);
                    }
                }
            });
            if (state == STATE.AUTO_REFRESH) {
                handler.postDelayed(this, period * 1000);
            }
        }
    }

    class TestDemand implements NewDemandAdapter {

        @Override
        public void requestDemand(Context context, RequestParams params, NewDemandAdapterListener listener) {
            if (listener != null) {
                HashMap<String, String> keywords = new HashMap<>();
                keywords.put("hb_cache_id", "fake-id");
                keywords.put("hb_pb", "0.50");
                listener.onDemandReady(keywords);
            }
        }

        @Override
        public void stopRequest() {

        }
    }
}



