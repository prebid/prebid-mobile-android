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

import static java.lang.Math.max;

import androidx.annotation.NonNull;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.MobileSdkPassThrough;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.modelcontrollers.BidRequester;
import org.prebid.mobile.rendering.networking.parameters.AdRequestInput;
import org.prebid.mobile.rendering.utils.helpers.RefreshTimerTask;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BidLoader {

    private final static String TAG = BidLoader.class.getSimpleName();

    private final static String TMAX_REQUEST_KEY = "tmaxrequest";
    private static boolean sTimeoutHasChanged = false;

    private AdUnitConfiguration adConfiguration;
    private BidRequester bidRequester;
    private AtomicBoolean currentlyLoading;

    private BidRequesterListener requestListener;
    private BidRefreshListener bidRefreshListener;

    private final ResponseHandler responseHandler = new ResponseHandler() {
        @Override
        public void onResponse(BaseNetworkTask.GetUrlResult response) {
            currentlyLoading.set(false);
            BidResponse bidResponse = new BidResponse(response.responseString, adConfiguration);
            if (bidResponse.hasParseError()) {
                failedToLoadBid(bidResponse.getParseError());
                return;
            }
            checkTmax(response, bidResponse);
            updateAdUnitConfiguration(bidResponse);
            if (requestListener != null) {
                setupRefreshTimer();
                requestListener.onFetchCompleted(bidResponse);
            } else {
                cancelRefresh();
            }
        }

        @Override
        public void onError(
                String msg,
                long responseTime
        ) {
            failedToLoadBid(msg);
        }

        @Override
        public void onErrorWithException(
                Exception e,
                long responseTime
        ) {
            failedToLoadBid(e.getMessage());
        }
    };

    private final RefreshTimerTask refreshTimerTask = new RefreshTimerTask(() -> {
        if (adConfiguration == null) {
            LogUtil.error(TAG, "handleRefresh(): Failure. AdConfiguration is null");
            return;
        }

        if (bidRefreshListener == null) {
            LogUtil.error(TAG, "RefreshListener is null. No refresh or load will be performed.");
            return;
        }

        if (!bidRefreshListener.canPerformRefresh()) {
            LogUtil.debug(TAG, "handleRefresh(): Loading skipped, rescheduling timer. View is not visible.");
            setupRefreshTimer();
            return;
        }

        LogUtil.debug(TAG, "refresh triggered: load() being called ");
        load();
    });

    public BidLoader(AdUnitConfiguration adConfiguration, BidRequesterListener requestListener) {
        this.adConfiguration = adConfiguration;
        this.requestListener = requestListener;
        currentlyLoading = new AtomicBoolean();
    }

    public void setBidRefreshListener(BidRefreshListener bidRefreshListener) {
        this.bidRefreshListener = bidRefreshListener;
    }

    public void load() {
        if (requestListener == null) {
            LogUtil.error(TAG, "Listener is null");
            return;
        }
        if (adConfiguration == null) {
            LogUtil.error(TAG, "No ad request configuration to load");
            return;
        }
        if (!PrebidMobile.isSdkInitialized()) {
            LogUtil.error(TAG, "SDK wasn't initialized. Context is null.");
            return;
        }

        // If currentlyLoading == false, set it to true and return true; else return false
        // If compareAndSet returns false, it means currentlyLoading was already true and therefore we should skip loading
        if (!currentlyLoading.compareAndSet(false, true)) {
            LogUtil.error(TAG, "Previous load is in progress. Load() ignored.");
            return;
        }

        sendBidRequest(adConfiguration);
    }

    public void setupRefreshTimer() {
        LogUtil.debug(TAG, "Schedule refresh timer");

        boolean isRefreshAvailable = adConfiguration != null && adConfiguration.isAdType(AdFormat.BANNER);
        if (!isRefreshAvailable) {
            LogUtil.debug(TAG, "setupRefreshTimer: Canceled. AdConfiguration is null or AdType is not Banner");
            return;
        }

        int refreshTimeMillis = adConfiguration.getAutoRefreshDelay();
        //for user or server values <= 0, no refreshtask should be created.
        //for such invalid values, refreshTimeMillis has been set to Integer.MAX_VALUE already.
        //So, check it against it to stop it from creating a refreshtask

        if (refreshTimeMillis == Integer.MAX_VALUE || refreshTimeMillis <= 0) {
            LogUtil.debug(TAG, "setupRefreshTimer(): refreshTimeMillis is: "
                    + refreshTimeMillis + ". Skipping refresh timer initialization");
            return;
        }

        int reloadTime = max(refreshTimeMillis, 1000);

        refreshTimerTask.scheduleRefreshTask(reloadTime);
    }

    public void cancelRefresh() {
        LogUtil.debug(TAG, "Cancel refresh timer");
        refreshTimerTask.cancelRefreshTimer();
    }

    public void destroy() {
        cancelRefresh();
        refreshTimerTask.destroy();

        if (bidRequester != null) {
            bidRequester.destroy();
        }
        requestListener = null;
        bidRefreshListener = null;
    }

    private void sendBidRequest(AdUnitConfiguration config) {
        currentlyLoading.set(true);
        if (bidRequester == null) {
            bidRequester = new BidRequester(config, new AdRequestInput(), responseHandler);
        }
        bidRequester.startAdRequest();
    }

    private void failedToLoadBid(String msg) {
        LogUtil.error(TAG, "Invalid bid response: " + msg);
        currentlyLoading.set(false);

        if (requestListener == null) {
            LogUtil.warning(TAG, "onFailedToLoad: Listener is null.");
            cancelRefresh();
            return;
        }

        setupRefreshTimer();
        requestListener.onError(new AdException(AdException.INTERNAL_ERROR, "Invalid bid response: " + msg));
    }

    private void checkTmax(
            BaseNetworkTask.GetUrlResult response,
            BidResponse parsedResponse
    ) {
        Map<String, Object> extMap = parsedResponse.getExt().getMap();
        if (!sTimeoutHasChanged && extMap.containsKey(TMAX_REQUEST_KEY)) {
            int tmaxRequest = (int) extMap.get(TMAX_REQUEST_KEY);
            // adding 200ms as safe time
            int timeout = (int) Math.min(response.responseTime + tmaxRequest + 200, BaseNetworkTask.TIMEOUT_DEFAULT);
            PrebidMobile.setTimeoutMillis(timeout);
            sTimeoutHasChanged = true;
        }
    }

    /**
     * Gets mobile sdk pass through object, combines it with user's ad unit
     * rendering controls settings in configuration, modifies ad unit
     * configuration, sets combined parameters to bid response.
     */
    private void updateAdUnitConfiguration(@NonNull BidResponse bidResponse) {
        MobileSdkPassThrough serverParameters = bidResponse.getMobileSdkPassThrough();
        MobileSdkPassThrough combinedParameters = MobileSdkPassThrough.combine(serverParameters, adConfiguration);
        combinedParameters.modifyAdUnitConfiguration(adConfiguration);
        bidResponse.setMobileSdkPassThrough(combinedParameters);
    }

    public interface BidRefreshListener {

        boolean canPerformRefresh();

    }

}
