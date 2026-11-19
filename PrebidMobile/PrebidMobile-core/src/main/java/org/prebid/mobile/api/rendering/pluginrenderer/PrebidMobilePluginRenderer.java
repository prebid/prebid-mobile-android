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

package org.prebid.mobile.api.rendering.pluginrenderer;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;
import org.prebid.mobile.api.rendering.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayVideoListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

/**
 * Main plugin renderer interface.
 */
public interface PrebidMobilePluginRenderer {

    String getName();

    String getVersion();

    @Nullable
    JSONObject getData();

    /**
     * Register a listener related to a specific ad unit config fingerprint in order to dispatch specific ad events
     */
    void registerEventListener(PluginEventListener pluginEventListener, String listenerKey);

    /**
     * Unregister a listener based on an ad unit config fingerprint
     */
    void unregisterEventListener(String listenerKey);

    /**
     * Creates and returns Banner View for a given Bid Response.
     * Returns null in the case of an internal error.
     * <br>
     * Don't forget to clean resources in {@link android.view.View#onDetachedFromWindow()}.
     */
    View createBannerAdView(
            @NonNull Context context,
            @NonNull DisplayViewListener displayViewListener,
            @Nullable DisplayVideoListener displayVideoListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    );

    /**
     * Creates and returns an implementation of PrebidMobileInterstitialControllerInterface for a given bid response
     * Returns null in the case of an internal error
     */
    PrebidMobileInterstitialControllerInterface createInterstitialController(
            @NonNull Context context,
            @NonNull InterstitialControllerListener interstitialControllerListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    );

    /**
     * Returns true only if the given ad unit could be renderer by the plugin
     */
    boolean isSupportRenderingFor(AdUnitConfiguration adUnitConfiguration);

    /**
     * Returns true if the renderer, or the ad SDK it wraps, sends the win notice for its ads itself.
     * <p>
     * When a plugin renderer creates the ad, Prebid sends the bid's win notice: {@code nurl},
     * the Prebid Cache URLs and {@code ext.prebid.events.win}. Return true so that Prebid does not
     * send it a second time. The default returns false, and Prebid sends the win notice.
     */
    default boolean sendsWinNotice() {
        return false;
    }
}
