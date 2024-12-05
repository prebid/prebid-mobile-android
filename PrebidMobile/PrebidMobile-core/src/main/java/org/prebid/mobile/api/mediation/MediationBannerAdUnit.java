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

import androidx.annotation.NonNull;

import org.prebid.mobile.AdSize;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.mediation.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;

/**
 * Mediation banner ad unit for Rendering API with AdMob or AppLovin MAX.
 */
public class MediationBannerAdUnit extends MediationBaseAdUnit {

    private static final String TAG = MediationBannerAdUnit.class.getSimpleName();

    private final ScreenStateReceiver screenStateReceiver = new ScreenStateReceiver();

    private boolean adFailed;

    /**
     * Default constructor.
     *
     * @param context           {@link Context}
     * @param configId          config id
     * @param size              ad size
     * @param mediationDelegate mediation delegate ({@code AdMobBannerMediationUtils} or {@code MaxMediationBannerUtils}.
     */
    public MediationBannerAdUnit(
        Context context,
        String configId,
        AdSize size,
        PrebidMediationDelegate mediationDelegate
    ) {
        super(context, configId, size, mediationDelegate);
        screenStateReceiver.register(context);
    }

    @Override
    protected final void initAdConfig(
        String configId,
        AdSize adSize
    ) {
        adUnitConfig.addSize(adSize);
        adUnitConfig.setConfigId(configId);
        adUnitConfig.setAdFormat(AdFormat.BANNER);
    }

    /**
     * Destroy ad unit.
     */
    @Override
    public void destroy() {
        super.destroy();
        screenStateReceiver.unregister();
    }

    @Override
    protected void initBidLoader() {
        super.initBidLoader();

        bidLoader.setBidRefreshListener(() -> {
            if (adFailed) {
                adFailed = false;
                LogUtil.debug(TAG, "Ad failed, can perform refresh.");
                return true;
            }

            boolean isViewVisible = mediationDelegate.canPerformRefresh();
            boolean canRefresh = screenStateReceiver.isScreenOn() && isViewVisible;
            LogUtil.debug(TAG, "Can perform refresh: " + canRefresh);
            return canRefresh;
        });
    }

    /**
     * Loads ad and applies mediation delegate.
     *
     * @param listener callback when operation is completed (success or fail)
     */
    @Override
    public final void fetchDemand(
            @NonNull OnFetchCompleteListener listener
    ) {
        super.fetchDemand(listener);
    }

    public final void addAdditionalSizes(AdSize... sizes) {
        adUnitConfig.addSizes(sizes);
    }

    public final void setRefreshInterval(int seconds) {
        adUnitConfig.setAutoRefreshDelay(seconds);
    }

    public void setAdPosition(AdPosition adPosition) {
        adUnitConfig.setAdPosition(adPosition);
    }

    public AdPosition getAdPosition() {
        return adUnitConfig.getAdPosition();
    }

    public void stopRefresh() {
        if (bidLoader != null) {
            bidLoader.cancelRefresh();
        }
    }

    public void onAdFailed() {
        adFailed = true;
    }

}
