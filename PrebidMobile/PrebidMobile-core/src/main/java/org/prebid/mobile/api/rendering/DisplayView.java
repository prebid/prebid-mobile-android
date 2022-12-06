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

import org.prebid.mobile.api.rendering.customrenderer.CustomBannerRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.networking.WinNotifier;
import org.prebid.mobile.rendering.utils.helpers.CustomRendererUtils;

import java.util.List;

public class DisplayView extends FrameLayout {
    private View adView;
    private AdUnitConfiguration adUnitConfiguration;
    private DisplayViewListener displayViewListener;

    public DisplayView(
            @NonNull Context context,
            DisplayViewListener displayViewListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    ) {
        super(context);

        this.adUnitConfiguration = adUnitConfiguration;
        this.displayViewListener = displayViewListener;

        WinNotifier winNotifier = new WinNotifier();
        winNotifier.notifyWin(bidResponse, () -> {
            CustomBannerRenderer customBannerRenderer = getCustomRenderer(bidResponse);
            if (customBannerRenderer != null) {
                adUnitConfiguration.modifyUsingBidResponse(bidResponse);
                adView = customBannerRenderer.getBannerAdView(context, displayViewListener, adUnitConfiguration, bidResponse);
            } else {
                adView = new DefaultDisplayView(context, displayViewListener, adUnitConfiguration, bidResponse);
            }
            addView(adView);
        });
    }

    private CustomBannerRenderer getCustomRenderer(@NonNull BidResponse response) {
        List<String> renderers = response.getCustomRenderers();
        if (renderers != null && renderers.size() > 0) {
            return CustomRendererUtils.getBannerRendererBySingleton(renderers);
        } else {
            return null;
        }
    }

    public void destroy() {
        adUnitConfiguration = null;
        displayViewListener = null;
    }
}
