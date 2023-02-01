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

package org.prebid.mobile.renderingtestapp.utils;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.rendering.customrenderer.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.api.rendering.customrenderer.PrebidMobilePluginCustomRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

// TODO temp for tests purposes, should not be merged
public class TotoCustomRenderer implements PrebidMobilePluginCustomRenderer {

    @Override
    public String getName() {
        return "Toto";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public View createBannerAdView(
            @NonNull Context context,
            @NonNull DisplayViewListener displayViewListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    ) {

        Toast.makeText(context, "Load Banner Ad", Toast.LENGTH_LONG).show();
        return new FrameLayout(context);

    }

    @Override
    public PrebidMobileInterstitialControllerInterface createInterstitialController(
            @NonNull Context context,
            @NonNull InterstitialControllerListener interstitialControllerListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse response
    ) {
        return new PrebidMobileInterstitialControllerInterface() {
            @Override
            public void loadAd(AdUnitConfiguration adUnitConfiguration, BidResponse bidResponse) {
                Toast.makeText(context, "Load Interstitial Ad", Toast.LENGTH_LONG).show();
                interstitialControllerListener.onInterstitialReadyForDisplay();
            }

            @Override
            public void show() {
                Toast.makeText(context, "Show Interstitial Ad", Toast.LENGTH_LONG).show();
                interstitialControllerListener.onInterstitialDisplayed();
            }

            @Override
            public void destroy() {

            }
        };
    }

    @Override
    public boolean isSupportRenderingFor(AdUnitConfiguration adUnitConfiguration) {
        return adUnitConfiguration.isAdType(AdFormat.INTERSTITIAL);
    }
}
