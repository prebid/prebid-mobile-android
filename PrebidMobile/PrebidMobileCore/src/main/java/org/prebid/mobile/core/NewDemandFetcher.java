package org.prebid.mobile.core;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
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
        HandlerThread fetcherThread = new HandlerThread("FetcherThread");
        fetcherThread.start();
        this.handler = new Handler(fetcherThread.getLooper());
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
        requestRunnable.cancelRequest();
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

    private void notifyListener(final NewResultCode resultCode) {
        if (listener != null) {
            Handler uiThread = new Handler(Looper.getMainLooper());
            uiThread.post(new Runnable() {
                @Override
                public void run() {
                    listener.onComplete(resultCode);
                }
            });
        }
    }


    enum DEMAND_STATUS {
        NOT_STARTED,
        REQUESTED,
        RESPONDED,
        CANCELLED
    }

    class Test implements NewDemandAdapter {
        @Override
        public void requestDemand(Context context, RequestParams params, NewDemandAdapterListener listener) {

        }

        @Override
        public void stopRequest() {

        }
    }

    class RequestRunnable implements Runnable {
        private NewDemandAdapter demandAdapter;
        private DEMAND_STATUS demandStatus;
        private Runnable timeOutRunnable;

        RequestRunnable() {
            this.demandAdapter = new NewPrebidServerAdapter();
            this.demandStatus = DEMAND_STATUS.NOT_STARTED;
            this.timeOutRunnable = new Runnable() {
                @Override
                public void run() {
                    if (demandStatus != DEMAND_STATUS.RESPONDED) {
                        cancelRequest();
                        notifyListener(NewResultCode.TIME_OUT);
                    }
                }
            };
        }

        void cancelRequest() {
            this.demandAdapter.stopRequest();
            demandStatus = DEMAND_STATUS.CANCELLED;
        }

        @Override
        public void run() {
            // reset state
            demandStatus = DEMAND_STATUS.NOT_STARTED;
            handler.removeCallbacks(timeOutRunnable);
            lastFetchTime = System.currentTimeMillis();
            // check input values
            Context context = weakReference.get();
            if (TextUtils.isEmpty(configId) || context == null) {
                notifyListener(NewResultCode.INVALID_REQUEST);
                return;
            }
            if (adType == AdType.BANNER) {
                if (sizes == null || sizes.isEmpty()) {
                    notifyListener(NewResultCode.INVALID_REQUEST);
                    return;
                }
            }
            // make actual demand request
            // by default use server side cache, use local cache for DFP
            RequestParams requestParams = new RequestParams(configId, adType, sizes, false);
            if (adObject.getClass() == Util.getClassFromString("com.google.android.gms.ads.doubleclick.PublisherAdRequest")) {
                requestParams = new RequestParams(configId, adType, sizes, true);
            }
            this.demandAdapter.requestDemand(context, requestParams, new NewDemandAdapter.NewDemandAdapterListener() {
                @Override
                public void onDemandReady(final HashMap<String, String> demand) {
                    if (demandStatus != DEMAND_STATUS.CANCELLED && demandStatus != DEMAND_STATUS.RESPONDED) {
                        NewAdUnit.apply(demand, NewDemandFetcher.this.adObject);
                        notifyListener(NewResultCode.SUCCESS);
                        demandStatus = DEMAND_STATUS.RESPONDED;
                    }
                }

                @Override
                public void onDemandFailed(NewResultCode resultCode) {
                    if (demandStatus != DEMAND_STATUS.CANCELLED && demandStatus != DEMAND_STATUS.RESPONDED) {
                        notifyListener(resultCode);
                        demandStatus = DEMAND_STATUS.RESPONDED;
                    }
                }
            });
            demandStatus = DEMAND_STATUS.REQUESTED;
            // cancel request after timeout
            handler.postDelayed(timeOutRunnable, NewPrebid.getTimeOut());
            if (state == STATE.AUTO_REFRESH) {
                handler.postDelayed(this, period * 1000);
            }
        }
    }
}



