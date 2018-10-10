package org.prebid.mobile;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.UUID;

class DemandFetcher {
    enum STATE {
        STOPPED,
        RUNNING,
        DESTROYED
    }

    private STATE state;
    private int periodMillis;
    private Object adObject;
    private OnCompleteListener listener;
    private Handler fetcherHandler;
    private RequestRunnable requestRunnable;
    private long lastFetchTime = -1;
    private long timePausedAt = -1;
    private WeakReference<Context> weakReference;
    private RequestParams requestParams;

    DemandFetcher(@NonNull Object adObj, @NonNull Context context) {
        this.state = STATE.STOPPED;
        this.periodMillis = 0;
        this.adObject = adObj;
        HandlerThread fetcherThread = new HandlerThread("FetcherThread");
        fetcherThread.start();
        this.fetcherHandler = new Handler(fetcherThread.getLooper());
        this.weakReference = new WeakReference<Context>(context);
        this.requestRunnable = new RequestRunnable();
    }

    void setListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    void setRequestParams(RequestParams requestParams) {
        this.requestParams = requestParams;
    }


    void setPeriodMillis(int periodMillis) {
        boolean periodChanged = this.periodMillis != periodMillis;
        this.periodMillis = periodMillis;
        if ((periodChanged) && !state.equals(STATE.STOPPED)) {
            stop();
            start();
        }
    }

    private void stop() {
        this.requestRunnable.cancelRequest();
        this.fetcherHandler.removeCallbacks(requestRunnable);
        // cancel existing requests
        timePausedAt = System.currentTimeMillis();
        state = STATE.STOPPED;
    }

    void start() {
        switch (state) {
            case STOPPED:
                if (this.periodMillis <= 0) {
                    // start a single request
                    fetcherHandler.post(requestRunnable);
                } else {
                    // Start recurring ad requests
                    final int msPeriod = periodMillis; // refresh periodMillis
                    final long stall; // delay millis for the initial request
                    if (timePausedAt != -1 && lastFetchTime != -1) {
                        //Clamp the stall between 0 and the periodMillis. Ads should never be requested on
                        //a delay longer than the periodMillis
                        stall = Math.min(msPeriod, Math.max(0, msPeriod - (timePausedAt - lastFetchTime)));
                    } else {
                        stall = 0;
                    }
                    fetcherHandler.postDelayed(requestRunnable, stall * 1000);
                }
                state = STATE.RUNNING;
                break;
            case RUNNING:
                if (this.periodMillis <= 0) {
                    // start a single request
                    fetcherHandler.post(requestRunnable);
                }
                break;
            case DESTROYED:
                break;
        }
    }

    void destroy() {
        if (state != STATE.DESTROYED) {
            this.adObject = null;
            this.listener = null;
            this.requestRunnable.cancelRequest();
            this.fetcherHandler.removeCallbacks(requestRunnable);
            this.requestRunnable = null;
            state = STATE.DESTROYED;
        }
    }

    private void notifyListener(final ResultCode resultCode) {
        if (listener != null) {
            final OnCompleteListener listenerFinal = listener;
            Handler uiThread = new Handler(Looper.getMainLooper());
            uiThread.post(new Runnable() {
                @Override
                public void run() {
                    listenerFinal.onComplete(resultCode);

                }
            });
        }
        // for single request, if done, finish current fetcher,
        // let ad unit create a new fetcher for next request
        if (periodMillis <= 0) {
            destroy();
        }
    }

    class RequestRunnable implements Runnable {
        private DemandAdapter demandAdapter;
        private boolean finished = false;
        private String auctionId;
        private Handler demandHandler;

        RequestRunnable() {
            // Using a separate thread for making demand request so that waiting on currently thread doesn't block actual fetching
            HandlerThread demandThread = new HandlerThread("DemandThread");
            demandThread.start();
            this.demandHandler = new Handler(demandThread.getLooper());
            this.demandAdapter = new PrebidServerAdapter();
            auctionId = UUID.randomUUID().toString();
        }

        void cancelRequest() {
            this.demandAdapter.stopRequest(auctionId);
        }

        @Override
        public void run() {
            // reset state
            auctionId = UUID.randomUUID().toString();
            finished = false;
            lastFetchTime = System.currentTimeMillis();
            // check input values
            final Context context = weakReference.get();
            if (context == null) {
                return;
            }
            demandHandler.post(new Runnable() {
                final String auctionIdFinal = auctionId;

                @Override
                public void run() {
                    demandAdapter.requestDemand(context, requestParams, new DemandAdapter.DemandAdapterListener() {
                        @Override
                        public void onDemandReady(final HashMap<String, String> demand, String auctionId) {
                            if (!finished && RequestRunnable.this.auctionId.equals(auctionId)) {
                                AdUnit.apply(demand, DemandFetcher.this.adObject);
                                notifyListener(ResultCode.SUCCESS);
                                finished = true;
                            }
                        }

                        @Override
                        public void onDemandFailed(ResultCode resultCode, String auctionId) {

                            if (!finished && RequestRunnable.this.auctionId.equals(auctionId)) {
                                notifyListener(resultCode);
                                finished = true;
                            }
                        }
                    }, auctionIdFinal);
                }
            });
            if (periodMillis > 0) {
                fetcherHandler.postDelayed(this, periodMillis);
            }
            while (!finished) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFetchTime >= PrebidMobile.getTimeout()) {
                    finished = true;
                    notifyListener(ResultCode.TIME_OUT);
                }
                if (Thread.interrupted()) {
                    return;
                }
            }
        }
    }
}



