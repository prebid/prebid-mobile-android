/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.rendering.bidding.loader;

import android.content.Context;

import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.modelcontrollers.BidRequester;
import org.prebid.mobile.rendering.networking.parameters.AdRequestInput;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.helpers.RefreshTimerTask;
import org.prebid.mobile.rendering.utils.logger.LogUtil;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.max;

public class BidLoader {
    private final static String TAG = BidLoader.class.getSimpleName();

    private final static String TMAX_REQUEST_KEY = "tmaxrequest";
    private static boolean sTimeoutHasChanged = false;

    private WeakReference<Context> mContextReference;
    private AdConfiguration mAdConfiguration;
    private BidRequester mBidRequester;
    private AtomicBoolean mCurrentlyLoading;

    private BidRequesterListener mRequestListener;
    private BidRefreshListener mBidRefreshListener;

    private final ResponseHandler mResponseHandler = new ResponseHandler() {
        @Override
        public void onResponse(BaseNetworkTask.GetUrlResult response) {
            mCurrentlyLoading.set(false);
            BidResponse bidResponse = new BidResponse(response.responseString);
            if (bidResponse.hasParseError()) {
                failedToLoadBid(bidResponse.getParseError());
                return;
            }
            checkTmax(response, bidResponse);
            if (mRequestListener != null) {
                setupRefreshTimer();
                mRequestListener.onFetchCompleted(bidResponse);
            }
            else {
                cancelRefresh();
            }
        }

        @Override
        public void onError(String msg, long responseTime) {
            failedToLoadBid(msg);
        }

        @Override
        public void onErrorWithException(Exception e, long responseTime) {
            failedToLoadBid(e.getMessage());
        }
    };

    private final RefreshTimerTask mRefreshTimerTask = new RefreshTimerTask(() -> {
        if (mAdConfiguration == null) {
            LogUtil.error(TAG, "handleRefresh(): Failure. AdConfiguration is null");
            return;
        }

        if (mBidRefreshListener == null) {
            LogUtil.error(TAG, "RefreshListener is null. No refresh or load will be performed.");
            return;
        }

        if (!mBidRefreshListener.canPerformRefresh()) {
            LogUtil.debug(TAG, "handleRefresh(): Loading skipped, rescheduling timer. View is not visible.");
            setupRefreshTimer();
            return;
        }

        LogUtil.debug(TAG, "refresh triggered: load() being called ");
        load();
    });

    public BidLoader(Context context, AdConfiguration adConfiguration, BidRequesterListener requestListener) {
        mContextReference = new WeakReference<>(context);
        mAdConfiguration = adConfiguration;
        mRequestListener = requestListener;
        mCurrentlyLoading = new AtomicBoolean();
    }

    public void setBidRefreshListener(BidRefreshListener bidRefreshListener) {
        mBidRefreshListener = bidRefreshListener;
    }

    public void load() {
        if (mRequestListener == null) {
            LogUtil.warn(TAG, "Listener is null");
            return;
        }
        if (mAdConfiguration == null) {
            LogUtil.warn(TAG, "No ad request configuration to load");
            return;
        }
        if (mContextReference.get() == null) {
            LogUtil.warn(TAG, "Context is null");
            return;
        }

        // If mCurrentlyLoading == false, set it to true and return true; else return false
        // If compareAndSet returns false, it means mCurrentlyLoading was already true and therefore we should skip loading
        if (!mCurrentlyLoading.compareAndSet(false, true)) {
            LogUtil.warn(TAG, "Previous load is in progress. Load() ignored.");
            return;
        }

        sendBidRequest(mContextReference.get(), mAdConfiguration);
    }

    public void setupRefreshTimer() {
        LogUtil.debug(TAG, "Schedule refresh timer");

        boolean isRefreshAvailable = mAdConfiguration != null
                                     && mAdConfiguration.isAdType(AdConfiguration.AdUnitIdentifierType.BANNER);
        if (!isRefreshAvailable) {
            LogUtil.debug(TAG, "setupRefreshTimer: Failed. AdConfiguration is null or AdType is not Banner");
            return;
        }

        int refreshTimeMillis = mAdConfiguration.getAutoRefreshDelay();
        //for user or server values <= 0, no refreshtask should be created.
        //for such invalid values, refreshTimeMillis has been set to Integer.MAX_VALUE already.
        //So, check it against it to stop it from creating a refreshtask

        if (refreshTimeMillis == Integer.MAX_VALUE || refreshTimeMillis <= 0) {
            LogUtil.debug(TAG, "setupRefreshTimer(): refreshTimeMillis is: "
                               + refreshTimeMillis + ". Skipping refresh timer initialization");
            return;
        }

        int reloadTime = max(refreshTimeMillis, 1000);

        mRefreshTimerTask.scheduleRefreshTask(reloadTime);
    }

    public void cancelRefresh() {
        LogUtil.debug(TAG, "Cancel refresh timer");
        mRefreshTimerTask.cancelRefreshTimer();
    }

    public void destroy() {
        cancelRefresh();
        mRefreshTimerTask.destroy();

        if (mBidRequester != null) {
            mBidRequester.destroy();
        }
        mRequestListener = null;
        mBidRefreshListener = null;
    }

    private void sendBidRequest(Context context, AdConfiguration config) {
        mCurrentlyLoading.set(true);
        if (mBidRequester == null) {
            mBidRequester = new BidRequester(context, config, new AdRequestInput(), mResponseHandler);
        }
        mBidRequester.startAdRequest();
    }

    private void failedToLoadBid(String msg) {
        LogUtil.error(TAG, "Invalid bid response: " + msg);
        mCurrentlyLoading.set(false);

        if (mRequestListener == null) {
            LogUtil.warn(TAG, "onFailedToLoad: Listener is null.");
            cancelRefresh();
            return;
        }

        setupRefreshTimer();
        mRequestListener.onError(new AdException(AdException.INTERNAL_ERROR, "Invalid bid response: " + msg));
    }

    private void checkTmax(BaseNetworkTask.GetUrlResult response, BidResponse parsedResponse) {
        Map<String, Object> extMap = parsedResponse.getExt().getMap();
        if (!sTimeoutHasChanged && extMap.containsKey(TMAX_REQUEST_KEY)) {
            int tmaxRequest = (int) extMap.get(TMAX_REQUEST_KEY);
            // adding 200ms as safe time
            int timeout = (int) Math.min(response.responseTime + tmaxRequest + 200, BaseNetworkTask.TIMEOUT_DEFAULT);
            PrebidRenderingSettings.setTimeoutMillis(timeout);
            sTimeoutHasChanged = true;
        }
    }

    public interface BidRefreshListener {
        boolean canPerformRefresh();
    }
}
