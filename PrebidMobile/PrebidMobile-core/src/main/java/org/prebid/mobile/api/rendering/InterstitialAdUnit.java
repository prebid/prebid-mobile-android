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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener;
import org.prebid.mobile.api.rendering.pluginrenderer.PluginEventListener;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.StandaloneInterstitialEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.InterstitialEventListener;

import java.util.EnumSet;

import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_FOR_LOAD;
import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM;

/**
 * Interstitial ad unit for rendering API.
 */
public class InterstitialAdUnit extends BaseInterstitialAdUnit {

    private static final String TAG = InterstitialAdUnit.class.getSimpleName();

    /**
     * Handler that is responsible for requesting, displaying and destroying of
     * primary ad (e.g. GAM). Also it tracks impression and sets listener.
     */
    private final InterstitialEventHandler eventHandler;

    /**
     * Listener that must be applied to InterstitialEventHandler.
     * It is responsible for onAdServerWin or onPrebidSdkWin.
     * Also, onAdDisplayed, onAdFailed, onAdClosed.
     */
    private final InterstitialEventListener interstitialEventListener = createEventListener();

    /**
     * Interstitial ad units events listener (like onAdLoaded, onAdFailed...)
     */
    @Nullable private InterstitialAdUnitListener adUnitEventsListener;

    /**
     * Instantiates an HTML InterstitialAdUnit for the given configurationId.
     */
    public InterstitialAdUnit(
        Context context,
        String configId
    ) {
        this(context, configId, EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO), null);
    }

    /**
     * Instantiates an InterstitialAdUnit for the given configurationId and adUnitType.
     */
    public InterstitialAdUnit(
        Context context,
        String configId,
        EnumSet<AdUnitFormat> adUnitFormats
    ) {
        this(context, configId, adUnitFormats, null);
    }

    /**
     * Instantiates an InterstitialAdUnit for HTML GAM prebid integration.
     */
    public InterstitialAdUnit(
        Context context,
        String configId,
        InterstitialEventHandler eventHandler
    ) {
        this(context, configId, EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO), eventHandler);
    }

    /**
     * Instantiates an InterstitialAdUnit for GAM prebid integration with given adUnitType.
     */
    public InterstitialAdUnit(
        Context context,
        String configId,
        @NonNull EnumSet<AdUnitFormat> adUnitFormats,
        InterstitialEventHandler eventHandler
    ) {
        super(context);

        if (eventHandler == null) {
            this.eventHandler = createStandaloneEventHandler();
        } else {
            this.eventHandler = eventHandler;
        }
        this.eventHandler.setInterstitialEventListener(interstitialEventListener);

        config.setConfigId(configId);
        config.setAdUnitFormats(adUnitFormats);
        config.addAdFormat(AdFormat.INTERSTITIAL);
        init(config);
    }


    @Override
    void showGamAd() {
        eventHandler.show();
    }

    @Override
    void requestAdWithBid(@Nullable Bid bid) {
        eventHandler.requestAdWithBid(bid);
    }

    @Override
    void notifyAdEventListener(AdListenerEvent adListenerEvent) {
        if (adUnitEventsListener == null) {
            LogUtil.debug(
                TAG,
                "notifyAdEventListener: Failed. AdUnitListener is null. Passed listener event: " + adListenerEvent
            );
            return;
        }

        switch (adListenerEvent) {
            case AD_CLOSE:
                adUnitEventsListener.onAdClosed(InterstitialAdUnit.this);
                break;
            case AD_LOADED:
                adUnitEventsListener.onAdLoaded(InterstitialAdUnit.this);
                break;
            case AD_DISPLAYED:
                adUnitEventsListener.onAdDisplayed(InterstitialAdUnit.this);
                break;
            case AD_CLICKED:
                adUnitEventsListener.onAdClicked(InterstitialAdUnit.this);
                break;
        }
    }

    @Override
    void notifyErrorListener(AdException exception) {
        if (adUnitEventsListener != null) {
            adUnitEventsListener.onAdFailed(InterstitialAdUnit.this, exception);
        }
    }


    public void setInterstitialAdUnitListener(@Nullable InterstitialAdUnitListener adUnitEventsListener) {
        this.adUnitEventsListener = adUnitEventsListener;
    }

    public void setPluginEventListener(PluginEventListener pluginEventListener) {
        PrebidMobilePluginRegister.getInstance().registerEventListener(pluginEventListener, config.getFingerprint());
    }

    public void setMinSizePercentage(AdSize minSizePercentage) {
        config.setMinSizePercentage(minSizePercentage);
    }


    @Override
    public void destroy() {
        super.destroy();
        if (eventHandler != null) {
            eventHandler.destroy();
        }
        adUnitEventsListener = null;

        PrebidMobilePluginRegister.getInstance().unregisterEventListener(config.getFingerprint());
    }


    private StandaloneInterstitialEventHandler createStandaloneEventHandler() {
        return new StandaloneInterstitialEventHandler();
    }

    private InterstitialEventListener createEventListener() {
        return new InterstitialEventListener() {
            @Override
            public void onPrebidSdkWin() {
                if (isBidInvalid()) {
                    changeInterstitialAdUnitState(READY_FOR_LOAD);
                    notifyErrorListener(new AdException(
                        AdException.INTERNAL_ERROR,
                        "WinnerBid is null when executing onPrebidSdkWin."
                    ));
                    return;
                }

                loadPrebidAd();
            }

            @Override
            public void onAdServerWin() {
                changeInterstitialAdUnitState(READY_TO_DISPLAY_GAM);
                notifyAdEventListener(AdListenerEvent.AD_LOADED);
            }

            @Override
            public void onAdFailed(AdException exception) {
                if (isBidInvalid()) {
                    changeInterstitialAdUnitState(READY_FOR_LOAD);
                    notifyErrorListener(exception);
                    return;
                }

                onPrebidSdkWin();
            }

            @Override
            public void onAdClosed() {
                notifyAdEventListener(AdListenerEvent.AD_CLOSE);
            }

            @Override
            public void onAdDisplayed() {
                changeInterstitialAdUnitState(READY_FOR_LOAD);
                notifyAdEventListener(AdListenerEvent.AD_DISPLAYED);
            }
        };
    }

}
