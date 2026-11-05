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

package org.prebid.mobile.api.mediation;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.AdSize;
import org.prebid.mobile.Host;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.api.data.FetchDemandResult;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.mediation.listeners.OnFetchCompleteListener;
import org.prebid.mobile.api.original.OnFetchDemandResult;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

/**
 * Internal base mediation ad unit.
 */
public abstract class MediationBaseAdUnit {

    private static final String TAG = MediationBaseAdUnit.class.getSimpleName();

    protected OnFetchCompleteListener onFetchCompleteListener;
    protected WeakReference<Context> contextWeakReference;
    protected AdUnitConfiguration adUnitConfig = new AdUnitConfiguration();
    protected PrebidMediationDelegate mediationDelegate;
    protected BidLoader bidLoader;

    private final BidRequesterListener bidRequesterListener = new BidRequesterListener() {
        @Override
        public void onFetchCompleted(BidResponse response) {
            onResponseReceived(response);
        }

        @Override
        public void onError(AdException exception) {
            onErrorReceived(exception);
        }
    };

    protected MediationBaseAdUnit(
        Context context,
        String configId,
        AdSize adSize,
        PrebidMediationDelegate mediationDelegate
    ) {
        contextWeakReference = new WeakReference<>(context);
        this.mediationDelegate = mediationDelegate;
        adUnitConfig.setAutoRefreshDelay(PrebidMobile.AUTO_REFRESH_DELAY_MIN / 1000);
        initAdConfig(configId, adSize);
        initBidLoader();
    }

    protected void fetchDemand(@NonNull OnFetchCompleteListener listener) {
        if (mediationDelegate == null) {
            LogUtil.error(TAG, "Demand fetch failed. Mediation delegate object must be not null");
            listener.onComplete(FetchDemandResult.INVALID_AD_OBJECT);
            return;
        }
        if (TextUtils.isEmpty(PrebidMobile.getPrebidServerAccountId())) {
            LogUtil.error(TAG, "Empty account id");
            listener.onComplete(FetchDemandResult.INVALID_ACCOUNT_ID);
            return;
        }
        if (TextUtils.isEmpty(adUnitConfig.getConfigId())) {
            LogUtil.error(TAG, "Empty config id");
            listener.onComplete(FetchDemandResult.INVALID_CONFIG_ID);
            return;
        }

        final Host bidServerHost = PrebidMobile.getPrebidServerHost();
        if (bidServerHost.equals(Host.CUSTOM) && bidServerHost.getHostUrl().isEmpty()) {
            LogUtil.error(TAG, "Empty host url for custom Prebid Server host.");
            listener.onComplete(FetchDemandResult.INVALID_HOST_URL);
            return;
        }

        onFetchCompleteListener = listener;
        bidLoader.load();
    }

    /**
     * Like {@link #fetchDemand(OnFetchCompleteListener)}, but returns the full {@link BidInfo}.
     * Its result code is the {@link ResultCode} matching the {@link FetchDemandResult} that
     * {@code fetchDemand} reports.
     */
    protected void fetchDemandBidInfo(@NonNull OnFetchDemandResult listener) {
        if (listener == null) {
            LogUtil.error(TAG, "Parameter OnFetchDemandResult in fetchDemandBidInfo() must be not null.");
            return;
        }

        fetchDemand(new BidInfoListener(listener, adUnitConfig));
    }

    public void setPbAdSlot(String adSlot) {
        adUnitConfig.setPbAdSlot(adSlot);
    }

    @Nullable
    public String getPbAdSlot() {
        return adUnitConfig.getPbAdSlot();
    }


    public void destroy() {
        onFetchCompleteListener = null;
        bidLoader.destroy();
        bidLoader = null;
    }

    protected abstract void initAdConfig(
        String configId,
        AdSize adSize
    );

    protected void onResponseReceived(BidResponse response) {
        LogUtil.debug(TAG, "On response received");
        if (onFetchCompleteListener == null) {
            cancelRefresh();
            return;
        }
        BidResponseCache.getInstance().putBidResponse(response);
        mediationDelegate.handleKeywordsUpdate(response.getTargeting());
        mediationDelegate.setResponseToLocalExtras(response);
        notifyFetchCompleted(FetchDemandResult.SUCCESS, response);
    }

    protected void onErrorReceived(AdException exception) {
        LogUtil.warning(TAG, "On error received");
        if (onFetchCompleteListener == null) {
            cancelRefresh();
            return;
        }
        onFetchCompleteListener.onComplete(FetchDemandResult.parseErrorMessage(exception.getMessage()));
    }

    /**
     * Reports a fetch result to {@link #onFetchCompleteListener}. When the listener came from
     * {@link #fetchDemandBidInfo(OnFetchDemandResult)}, the {@link BidInfo} is built from
     * {@code response}, so it always describes the response that produced the result.
     */
    protected final void notifyFetchCompleted(
            @NonNull FetchDemandResult result,
            @Nullable BidResponse response
    ) {
        if (onFetchCompleteListener instanceof BidInfoListener) {
            ((BidInfoListener) onFetchCompleteListener).onComplete(result, response);
        } else if (onFetchCompleteListener != null) {
            onFetchCompleteListener.onComplete(result);
        }
    }

    protected void initBidLoader() {
        bidLoader = new BidLoader(adUnitConfig, bidRequesterListener);
    }

    private void cancelRefresh() {
        bidLoader.cancelRefresh();
        LogUtil.error(TAG, "Failed to pass callback");
        if (onFetchCompleteListener == null) {
            LogUtil.error(TAG, "OnFetchCompleteListener is null");
        }
    }

    @Nullable
    public String getImpOrtbConfig() {
        return adUnitConfig.getImpOrtbConfig();
    }

    /**
     * Sets imp level OpenRTB config JSON string that will be merged with the original imp object in the bid request.
     * Expected format: {@code "{"new_field": "value"}"}.
     * @param ortbConfig JSON config string.
     */
    public void setImpOrtbConfig(@Nullable String ortbConfig) {
        adUnitConfig.setImpOrtbConfig(ortbConfig);
    }

    @Nullable
    public String getGlobalOrtbConfig() {
        return adUnitConfig.getGlobalOrtbConfig();
    }

    /**
     * Sets the global OpenRTB configuration string for the ad unit. It takes precedence over `Targeting.setGlobalOrtbConfig`.
     * Expected format: {@code "{"new_field": "value"}"}.
     * @param ortbConfig The global OpenRTB JSON configuration string to set. Can be `null` to clear the configuration.
     */
    public void setGlobalOrtbConfig(@Nullable String ortbConfig) {
        adUnitConfig.setGlobalOrtbConfig(ortbConfig);
    }

    /**
     * Adapts an {@link OnFetchDemandResult} to {@link OnFetchCompleteListener}, so
     * {@link #onFetchCompleteListener} keeps its type. A result reported without a response
     * (a validation failure, an error, or a subclass calling {@code onComplete} directly) gets a
     * {@link BidInfo} with only the result code.
     */
    private static final class BidInfoListener implements OnFetchCompleteListener {

        @NonNull
        private final OnFetchDemandResult listener;
        @NonNull
        private final AdUnitConfiguration adUnitConfig;

        BidInfoListener(@NonNull OnFetchDemandResult listener, @NonNull AdUnitConfiguration adUnitConfig) {
            this.listener = listener;
            this.adUnitConfig = adUnitConfig;
        }

        @Override
        public void onComplete(FetchDemandResult result) {
            onComplete(result, null);
        }

        void onComplete(@NonNull FetchDemandResult result, @Nullable BidResponse response) {
            listener.onComplete(BidInfo.create(result.toResultCode(), response, adUnitConfig));
        }

    }

}
