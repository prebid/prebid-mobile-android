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
import androidx.annotation.NonNull;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.bidding.enums.BannerAdPosition;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;
import org.prebid.mobile.units.configuration.AdFormat;

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
        mAdUnitConfig.setAdFormat(AdFormat.BANNER);
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

            boolean isViewVisible = mMediationDelegate.canPerformRefresh();
            boolean canRefresh = mScreenStateReceiver.isScreenOn() && isViewVisible;
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
