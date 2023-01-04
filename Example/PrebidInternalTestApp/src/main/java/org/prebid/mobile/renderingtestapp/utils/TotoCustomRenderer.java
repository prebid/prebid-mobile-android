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

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.prebid.mobile.api.exceptions.AdException.THIRD_PARTY;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.customrenderer.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.api.rendering.customrenderer.PrebidMobilePluginCustomRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

import tv.teads.sdk.AdOpportunityTrackerView;
import tv.teads.sdk.AdPlacementSettings;
import tv.teads.sdk.AdRatio;
import tv.teads.sdk.AdRequestSettings;
import tv.teads.sdk.InReadAd;
import tv.teads.sdk.InReadAdModelListener;
import tv.teads.sdk.InReadAdPlacement;
import tv.teads.sdk.TeadsSDK;
import tv.teads.sdk.renderer.InReadAdView;

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
            @NonNull DisplayViewListener listener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse response
    ) {

        InReadAdView inReadAdView = new InReadAdView(context);
        inReadAdView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        getAdd(inReadAdView, listener);

        return inReadAdView;
    }

    @Override
    public PrebidMobileInterstitialControllerInterface createInterstitialController(
            @NonNull Context context,
            @NonNull InterstitialControllerListener listener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse response
    ) {
        return new PrebidMobileInterstitialControllerInterface() {
            @Override
            public void loadAd(AdUnitConfiguration adUnitConfiguration, BidResponse bidResponse) {
                Toast.makeText(context, "Load Interstitial Ad", Toast.LENGTH_LONG).show();
                listener.onInterstitialReadyForDisplay();
            }

            @Override
            public void show() {
                Toast.makeText(context, "Show Interstitial Ad", Toast.LENGTH_LONG).show();
                listener.onInterstitialDisplayed();
            }

            @Override
            public void destroy() {

            }
        };
    }

    private void getAdd(InReadAdView inReadAdView, DisplayViewListener displayViewListener) {
        InReadAdPlacement inReadAdPlacement;
        AdPlacementSettings adPlacementSettings = new AdPlacementSettings.Builder().enableDebug().build();
        AdRequestSettings adRequestSettings = new AdRequestSettings.Builder().pageSlotUrl("http://teads.com").build();

        inReadAdPlacement = TeadsSDK.INSTANCE.createInReadPlacement(inReadAdView.getContext(), 84242, adPlacementSettings);

        inReadAdPlacement.requestAd(adRequestSettings, new InReadAdModelListener() {
            @Override
            public void onAdReceived(@NonNull InReadAd inReadAd, @NonNull AdRatio adRatio) {
                inReadAdView.bind(inReadAd);
                displayViewListener.onAdLoaded();
            }

            @Override
            public void adOpportunityTrackerView(@NonNull AdOpportunityTrackerView adOpportunityTrackerView) {

            }

            @Override
            public void onFailToReceiveAd(@NonNull String s) {
                displayViewListener.onAdFailed(new AdException(THIRD_PARTY, s));
            }

            @Override
            public void onAdRatioUpdate(@NonNull AdRatio adRatio) {}

            @Override
            public void onAdImpression() {
                displayViewListener.onAdDisplayed();
            }

            @Override
            public void onAdClicked() {
                displayViewListener.onAdClicked();
            }

            @Override
            public void onAdError(int i, @NonNull String s) {
                displayViewListener.onAdFailed(new AdException(THIRD_PARTY, s));
            }

            @Override
            public void onAdClosed() {
                displayViewListener.onAdClosed();
            }

            @Override
            public void onAdExpandedToFullscreen() {}

            @Override
            public void onAdCollapsedFromFullscreen() {}
        });
    }
}
