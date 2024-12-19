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

package org.prebid.mobile.api.rendering;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.DisplayVideoListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.networking.WinNotifier;

/**
 * Internal view for {@link BannerView}.
 */
public class DisplayView extends FrameLayout {
    private View adView;
    private AdUnitConfiguration adUnitConfiguration;
    private DisplayViewListener displayViewListener;
    private DisplayVideoListener displayVideoListener;

    public DisplayView(
            @NonNull Context context,
            DisplayViewListener displayViewListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    ) {
        super(context);

        this.adUnitConfiguration = adUnitConfiguration;
        this.displayViewListener = displayViewListener;

        createBannerAdView(context, bidResponse);
    }

    public DisplayView(
            @NonNull Context context,
            DisplayViewListener displayViewListener,
            DisplayVideoListener displayVideoListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    ) {
        super(context);

        this.adUnitConfiguration = adUnitConfiguration;
        this.displayViewListener = displayViewListener;
        this.displayVideoListener = displayVideoListener;

        createBannerAdView(context, bidResponse);
    }

    private void createBannerAdView(
            @NonNull Context context,
            @NonNull BidResponse bidResponse
    ) {
        WinNotifier winNotifier = new WinNotifier();
        winNotifier.notifyWin(bidResponse, () -> {
            PrebidMobilePluginRenderer plugin = PrebidMobilePluginRegister.getInstance().getPluginForPreferredRenderer(bidResponse);
            if (plugin != null) {
                adUnitConfiguration.modifyUsingBidResponse(bidResponse);
                adView = plugin.createBannerAdView(context, displayViewListener, displayVideoListener, adUnitConfiguration, bidResponse);
                addView(adView);
            }
        });
    }

    public void destroy() {
        adUnitConfiguration = null;
        displayViewListener = null;
        displayVideoListener = null;
    }

    @Nullable
    public String getImpOrtbConfig() {
        return adUnitConfiguration.getImpOrtbConfig();
    }

    public void setImpOrtbConfig(@Nullable String ortbConfig) {
        adUnitConfiguration.setImpOrtbConfig(ortbConfig);
    }
}
