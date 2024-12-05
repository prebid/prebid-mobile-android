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
import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.InterstitialView;
import org.prebid.mobile.api.rendering.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialViewListener;
import org.prebid.mobile.rendering.interstitial.rewarded.Reward;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.MobileSdkPassThrough;
import org.prebid.mobile.rendering.networking.WinNotifier;

public class InterstitialController implements PrebidMobileInterstitialControllerInterface {

    private static final String TAG = InterstitialController.class.getSimpleName();

    private String impressionEventUrl;

    private final InterstitialView bidInterstitialView;
    private InterstitialControllerListener listener;
    private AdFormat adUnitIdentifierType;
    private AdUnitConfiguration config;
    private Runnable rewardListener;

    private InterstitialViewListener interstitialViewListener = new InterstitialViewListener() {
        @Override
        public void onAdLoaded(
                InterstitialView interstitialView,
                AdDetails adDetails
        ) {
            LogUtil.debug(TAG, "onAdLoaded");
            if (listener != null) {
                listener.onInterstitialReadyForDisplay();
            }
        }

        @Override
        public void onAdFailed(
                InterstitialView interstitialView,
                AdException error
        ) {
            LogUtil.debug(TAG, "onAdFailed");
            if (listener != null) {
                listener.onInterstitialFailedToLoad(error);
            }
        }

        @Override
        public void onAdDisplayed(InterstitialView interstitialView) {
            LogUtil.debug(TAG, "onAdDisplayed");
            if (listener != null) {
                listener.onInterstitialDisplayed();
            }
        }

        @Override
        public void onAdCompleted(InterstitialView interstitialView) {
        }

        @Override
        public void onAdClicked(InterstitialView interstitialView) {
            LogUtil.debug(TAG, "onAdClicked");
            if (listener != null) {
                listener.onInterstitialClicked();
            }
        }

        @Override
        public void onAdClickThroughClosed(InterstitialView interstitialView) {

        }

        @Override
        public void onAdClosed(InterstitialView interstitialView) {
            LogUtil.debug(TAG, "onAdClosed");
            if (listener != null) {
                listener.onInterstitialClosed();

                if (config == null) return;
                boolean userIsNotRewarded = !config.getRewardManager().getUserRewardedAlready();
                if (userIsNotRewarded) {
                    listener.onUserEarnedReward();
                }
            }
        }
    };

    public InterstitialController(Context context, InterstitialControllerListener listener)
    throws AdException {
        this.listener = listener;
        bidInterstitialView = new InterstitialView(context);
        if (interstitialViewListener != null) {
            bidInterstitialView.setInterstitialViewListener(interstitialViewListener);
        }
        bidInterstitialView.setPubBackGroundOpacity(1.0f);
    }

    public void loadAd(AdUnitConfiguration adUnitConfiguration, BidResponse bidResponse) {
        config = adUnitConfiguration;
        adUnitConfiguration.modifyUsingBidResponse(bidResponse);
        setRenderingControlSettings(adUnitConfiguration, bidResponse);
        WinNotifier winNotifier = new WinNotifier();
        winNotifier.notifyWin(bidResponse, () -> {
            impressionEventUrl = bidResponse.getImpressionEventUrl();
            adUnitIdentifierType = bidResponse.isVideo() ? AdFormat.VAST : AdFormat.INTERSTITIAL;
            adUnitConfiguration.setAdFormat(adUnitIdentifierType);
            bidInterstitialView.loadAd(adUnitConfiguration, bidResponse);
        });
    }

    public void loadAd(String responseId, boolean isRewarded) {
        BidResponse bidResponse = BidResponseCache.getInstance().popBidResponse(responseId);
        if (bidResponse == null) {
            if (listener != null) {
                listener.onInterstitialFailedToLoad(new AdException(
                        AdException.INTERNAL_ERROR,
                        "No bid response found in the cache"
                ));
            }
            return;
        }
        config = new AdUnitConfiguration();
        config.setRewarded(isRewarded);
        if (isRewarded) {
            config.getRewardManager().setRewardListener(rewardListener);
        }
        loadAd(config, bidResponse);
    }

    public void show() {
        if (adUnitIdentifierType == null) {
            LogUtil.error(TAG, "show: Failed. AdUnitIdentifierType is not defined!");
            return;
        }

        switch (adUnitIdentifierType) {
            case INTERSTITIAL:
                bidInterstitialView.showAsInterstitialFromRoot();
                break;
            case VAST:
                bidInterstitialView.showVideoAsInterstitial();
                break;
            default:
                LogUtil.error(TAG, "show: Failed. Did you specify correct AdUnitConfigurationType? "
                    + "Supported types: VAST, INTERSTITIAL. "
                    + "Provided type: " + adUnitIdentifierType
                );
        }
    }

    public void destroy() {
        bidInterstitialView.destroy();
        listener = null;
        interstitialViewListener = null;
    }

    public void setRewardListener(Runnable rewardListener) {
        this.rewardListener = rewardListener;
    }

    @Nullable
    public Reward getReward() {
        if (config == null) return null;

        return config.getRewardManager().getRewardedExt().getReward();
    }

    private void setRenderingControlSettings(
        AdUnitConfiguration adUnitConfiguration,
        BidResponse bidResponse
    ) {
        MobileSdkPassThrough renderingControlSettings = bidResponse.getMobileSdkPassThrough();
        if (renderingControlSettings != null) {
            renderingControlSettings.modifyAdUnitConfiguration(adUnitConfiguration);
        }
    }

}
