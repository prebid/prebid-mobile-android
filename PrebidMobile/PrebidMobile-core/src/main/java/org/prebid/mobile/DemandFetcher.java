/*
 *    Copyright 2018-2019 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import org.prebid.mobile.tasksmanager.TasksManager;

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
    private RequestParams requestParams;

    DemandFetcher(@NonNull Object adObj) {
        this.state = STATE.STOPPED;
        this.periodMillis = 0;
        this.adObject = adObj;
        HandlerThread fetcherThread = new HandlerThread("FetcherThread");
        fetcherThread.start();
        this.fetcherHandler = new Handler(fetcherThread.getLooper());
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

    void stop() {
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
            this.requestRunnable.destroy();
            this.fetcherHandler.removeCallbacks(requestRunnable);
            if (this.fetcherHandler.getLooper() != null) {
                this.fetcherHandler.getLooper().quit();
            }
            this.requestRunnable = null;
            state = STATE.DESTROYED;
        }
    }

    @MainThread
    private void notifyListener(final ResultCode resultCode) {
        LogUtil.d("notifyListener:" + resultCode);

        if (listener != null) {
            TasksManager.getInstance().executeOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onComplete(resultCode);
                    }
                    // for single request, if done, finish current fetcher,
                    // let ad unit create a new fetcher for next request
                    if (periodMillis <= 0) {
                        destroy();
                    }
                }
            });
        }
    }

    class RequestRunnable implements Runnable {
        private DemandAdapter demandAdapter;
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

        void destroy() {
            cancelRequest();
            demandHandler.removeCallbacksAndMessages(null);
            if (demandHandler.getLooper() != null) {
                demandHandler.getLooper().quit();
            }
        }

        @Override
        public void run() {
            // reset state
            auctionId = UUID.randomUUID().toString();
            lastFetchTime = System.currentTimeMillis();
            // check input values
            demandHandler.post(new Runnable() {

                @Override
                public void run() {
                    demandAdapter.requestDemand(requestParams, new DemandAdapter.DemandAdapterListener() {
                        @Override
                        @MainThread
                        public void onDemandReady(final HashMap<String, String> demand, String auctionId) {
                            if (RequestRunnable.this.auctionId.equals(auctionId)) {
                                Util.apply(demand, DemandFetcher.this.adObject);
                                LogUtil.i("Successfully set the following keywords: " + demand.toString());
                                notifyListener(ResultCode.SUCCESS);
                            }
                        }

                        @Override
                        @MainThread
                        public void onDemandFailed(ResultCode resultCode, String auctionId) {
                            if (RequestRunnable.this.auctionId.equals(auctionId)) {
                                Util.apply(null, DemandFetcher.this.adObject);
                                LogUtil.i("Removed all used keywords from the ad object");
                                notifyListener(resultCode);
                            }
                        }
                    }, auctionId);
                }
            });
            if (periodMillis > 0) {
                fetcherHandler.postDelayed(this, periodMillis);
            }
        }
    }

    //region exposed for testing
    @VisibleForTesting
    Handler getHandler() {
        return this.fetcherHandler;
    }

    @VisibleForTesting
    Handler getDemandHandler() {
        RequestRunnable runnable = this.requestRunnable;
        return runnable.demandHandler;
    }
    //endregion
}



