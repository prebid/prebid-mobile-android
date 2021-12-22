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

package org.prebid.mobile.rendering.bidding.display;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.enums.BannerAdPosition;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;
import org.prebid.mobile.rendering.utils.helpers.VisibilityChecker;
import org.prebid.mobile.rendering.utils.logger.LogUtil;

public class MediationBannerAdUnit extends MediationBaseAdUnit {
    private static final String TAG = MediationBannerAdUnit.class.getSimpleName();

    private final ScreenStateReceiver mScreenStateReceiver = new ScreenStateReceiver();

    private boolean mAdFailed;

    public MediationBannerAdUnit(Context context, String configId, AdSize size, PrebidMediationDelegate mediationDelegate) {
        super(context, configId, size, mediationDelegate);
        mScreenStateReceiver.register(context);
    }

    @Override
    protected final void initAdConfig(String configId, AdSize adSize) {
        mAdUnitConfig.addSize(adSize);
        mAdUnitConfig.setConfigId(configId);
        mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
    }

    @Override
    public void destroy() {
        super.destroy();
        mScreenStateReceiver.unregister();
    }

    @Override
    protected void initBidLoader() {
        super.initBidLoader();

        mBidLoader.setBidRefreshListener(() -> {
            if (mAdFailed) {
                mAdFailed = false;
                LogUtil.debug(TAG, "Ad failed, can perform refresh.");
                return true;
            }

            Object adObject = mMediationDelegate.getAdObject();
            boolean isVisible = true;
            if (adObject instanceof View) {
                final VisibilityTrackerOption visibilityTrackerOption = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);
                final VisibilityChecker checker = new VisibilityChecker(visibilityTrackerOption);
                isVisible = checker.isVisibleForRefresh((View) adObject);
                if (isVisible) {
                    LogUtil.debug(TAG, "Visibility checker result: " + isVisible);
                } else {
                    LogUtil.error(TAG, "Can't perform refresh. Ad view is not visible.");
                }
            }

            boolean canRefresh = mScreenStateReceiver.isScreenOn() && isVisible;
            LogUtil.debug(TAG, "Can perform refresh: " + canRefresh);
            return canRefresh;
        });
    }

    @Override
    public final void fetchDemand(
        @NonNull
            OnFetchCompleteListener listener) {
        super.fetchDemand(listener);
    }

    /**
     * Sets NativeAdConfiguration and enables Native Ad requests
     *
     * @param configuration - configured NativeAdConfiguration class
     */
    public void setNativeAdConfiguration(NativeAdConfiguration configuration) {
        mAdUnitConfig.setNativeAdConfiguration(configuration);
    }

    public final void addAdditionalSizes(AdSize... sizes) {
        mAdUnitConfig.addSizes(sizes);
    }

    public final void setRefreshInterval(int seconds) {
        mAdUnitConfig.setAutoRefreshDelay(seconds);
    }

    public void setAdPosition(BannerAdPosition bannerAdPosition) {
        final AdPosition adPosition = BannerAdPosition.mapToAdPosition(bannerAdPosition);
        mAdUnitConfig.setAdPosition(adPosition);
    }

    public BannerAdPosition getAdPosition() {
        return BannerAdPosition.mapToDisplayAdPosition(mAdUnitConfig.getAdPositionValue());
    }

    public void stopRefresh() {
        if (mBidLoader != null) {
            mBidLoader.cancelRefresh();
        }
    }

    public void onAdFailed() {
        mAdFailed = true;
    }
}
