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

package org.prebid.mobile.rendering.bidding.parallel;

import android.content.Context;

import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.StandaloneInterstitialEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.InterstitialAdUnitListener;
import org.prebid.mobile.rendering.bidding.listeners.InterstitialEventListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.utils.logger.LogUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_FOR_LOAD;
import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM;

public class InterstitialAdUnit extends BaseInterstitialAdUnit {
    private static final String TAG = InterstitialAdUnit.class.getSimpleName();

    private final InterstitialEventHandler mEventHandler;

    @Nullable
    private InterstitialAdUnitListener mInterstitialAdUnitListener;

    //region ==================== Listener implementation
    private final InterstitialEventListener mInterstitialEventListener = new InterstitialEventListener() {
        @Override
        public void onPrebidSdkWin() {
            if (isBidInvalid()) {
                changeInterstitialAdUnitState(READY_FOR_LOAD);
                notifyErrorListener(new AdException(AdException.INTERNAL_ERROR, "WinnerBid is null when executing onPrebidSdkWin."));
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
    //endregion ==================== Listener implementation

    /**
     * Instantiates an InterstitialAdUnit for the given configurationId and adUnitType.
     */
    public InterstitialAdUnit(Context context, String configId, AdUnitFormat adUnitFormat) {
        this(context, configId, adUnitFormat, null, new StandaloneInterstitialEventHandler());
    }

    /**
     * Instantiates an HTML InterstitialAdUnit for the given configurationId and minimum size in percentage (optional).
     */
    public InterstitialAdUnit(Context context, String configId,
                              @Nullable
                                      AdSize minSizePercentage) {
        this(context, configId, AdUnitFormat.DISPLAY, minSizePercentage, new StandaloneInterstitialEventHandler());
    }

    /**
     * Instantiates an InterstitialAdUnit for HTML GAM prebid integration with given minimum size in percentage (optional).
     */
    public InterstitialAdUnit(Context context, String configId,
                              @Nullable
                                     AdSize minSizePercentage,
                              InterstitialEventHandler eventHandler) {
        this(context, configId, AdUnitFormat.DISPLAY, minSizePercentage, eventHandler);
    }

    /**
     * Instantiates an InterstitialAdUnit for GAM prebid integration with given adUnitType.
     */
    public InterstitialAdUnit(Context context, String configId,
                              @NonNull
                                     AdUnitFormat adUnitFormat,
                              InterstitialEventHandler eventHandler) {
        this(context, configId, adUnitFormat, null, eventHandler);
    }

    private InterstitialAdUnit(Context context, String configId,
                               @NonNull
                                      AdUnitFormat adUnitFormat,
                               @Nullable
                                      AdSize minSizePercentage,
                               InterstitialEventHandler eventHandler) {
        super(context);
        mEventHandler = eventHandler;
        mEventHandler.setInterstitialEventListener(mInterstitialEventListener);

        AdConfiguration adUnitConfiguration = new AdConfiguration();
        adUnitConfiguration.setConfigId(configId);
        adUnitConfiguration.setMinSizePercentage(minSizePercentage);
        adUnitConfiguration.setAdUnitIdentifierType(mapPrebidAdUnitTypeToAdConfigAdUnitType(adUnitFormat));

        init(adUnitConfiguration);
    }

    public void destroy() {
        super.destroy();
        if (mEventHandler != null) {
            mEventHandler.destroy();
        }
    }

    //region ==================== getters and setters
    public void setInterstitialAdUnitListener(
        @Nullable
            InterstitialAdUnitListener interstitialAdUnitListener) {
        mInterstitialAdUnitListener = interstitialAdUnitListener;
    }
    //endregion ==================== getters and setters

    @Override
    void requestAdWithBid(
        @Nullable
            Bid bid) {
        mEventHandler.requestAdWithBid(bid);
    }

    @Override
    void showGamAd() {
        mEventHandler.show();
    }

    @Override
    void notifyAdEventListener(AdListenerEvent adListenerEvent) {
        if (mInterstitialAdUnitListener == null) {
            LogUtil.debug(TAG, "notifyAdEventListener: Failed. AdUnitListener is null. Passed listener event: " + adListenerEvent);
            return;
        }

        switch (adListenerEvent) {
            case AD_CLOSE:
                mInterstitialAdUnitListener.onAdClosed(InterstitialAdUnit.this);
                break;
            case AD_LOADED:
                mInterstitialAdUnitListener.onAdLoaded(InterstitialAdUnit.this);
                break;
            case AD_DISPLAYED:
                mInterstitialAdUnitListener.onAdDisplayed(InterstitialAdUnit.this);
                break;
            case AD_CLICKED:
                mInterstitialAdUnitListener.onAdClicked(InterstitialAdUnit.this);
                break;
        }
    }

    @Override
    void notifyErrorListener(AdException exception) {
        if (mInterstitialAdUnitListener != null) {
            mInterstitialAdUnitListener.onAdFailed(InterstitialAdUnit.this, exception);
        }
    }

    private AdConfiguration.AdUnitIdentifierType mapPrebidAdUnitTypeToAdConfigAdUnitType(AdUnitFormat adUnitFormat) {
        switch (adUnitFormat) {
            case DISPLAY:
                return AdConfiguration.AdUnitIdentifierType.INTERSTITIAL;
            case VIDEO:
                return AdConfiguration.AdUnitIdentifierType.VAST;
            default:
                LogUtil.debug(TAG, "setAdUnitIdentifierType: Provided AdUnitType [" + adUnitFormat + "] doesn't match any expected adUnitType.");
                return null;
        }
    }
}
