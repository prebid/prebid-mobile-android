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

import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.bidding.listeners.DisplayVideoListener;
import org.prebid.mobile.api.rendering.pluginrenderer.PluginEventListener;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.core.BuildConfig;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

public class PrebidRenderer implements PrebidMobilePluginRenderer {

    @Override
    public String getName() {
        return PREBID_MOBILE_RENDERER_NAME;
    }

    @Override
    public String getVersion() {
        return BuildConfig.VERSION;
    }

    @Nullable
    @Override
    public String getToken() {
        return null;
    }

    @Override
    public void registerEventListener(PluginEventListener pluginEventListener, String listenerKey) {}

    @Override
    public void unregisterEventListener(String listenerKey) {}

    @Override
    public View createBannerAdView(
            @NonNull Context context,
            @NonNull DisplayViewListener displayViewListener,
            @Nullable DisplayVideoListener displayVideoListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    ) {
        return new PrebidDisplayView(context, displayViewListener, displayVideoListener, adUnitConfiguration, bidResponse);
    }

    @Override
    public PrebidMobileInterstitialControllerInterface createInterstitialController(
            @NonNull Context context,
            @NonNull InterstitialControllerListener interstitialControllerListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    ) {
        try {
            return new InterstitialController(context, interstitialControllerListener);
        } catch (AdException e) {
            LogUtil.error("message:" + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isSupportRenderingFor(AdUnitConfiguration adUnitConfiguration) {
        return adUnitConfiguration.isAdType(AdFormat.BANNER) ||
                adUnitConfiguration.isAdType(AdFormat.INTERSTITIAL) ||
                adUnitConfiguration.isAdType(AdFormat.NATIVE) ||
                adUnitConfiguration.isAdType(AdFormat.VAST);
    }
}
