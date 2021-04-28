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

import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialViewListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.networking.WinNotifier;
import org.prebid.mobile.rendering.utils.logger.LogUtil;

public class InterstitialController {
    private static final String TAG = InterstitialController.class.getSimpleName();

    private final InterstitialView mBidInterstitialView;
    private final InterstitialControllerListener mListener;
    private AdConfiguration.AdUnitIdentifierType mAdUnitIdentifierType;

    private final InterstitialViewListener mInterstitialViewListener = new InterstitialViewListener() {
        @Override
        public void onAdLoaded(InterstitialView interstitialView, AdDetails adDetails) {
            LogUtil.debug(TAG, "onAdLoaded");
            if (mListener != null) {
                mListener.onInterstitialReadyForDisplay();
            }
        }

        @Override
        public void onAdFailed(InterstitialView interstitialView, AdException error) {
            LogUtil.debug(TAG, "onAdFailed");
            if (mListener != null) {
                mListener.onInterstitialFailedToLoad(error);
            }
        }

        @Override
        public void onAdDisplayed(InterstitialView interstitialView) {
            LogUtil.debug(TAG, "onAdDisplayed");
            if (mListener != null) {
                mListener.onInterstitialDisplayed();
            }
        }

        @Override
        public void onAdCompleted(InterstitialView interstitialView) {
        }

        @Override
        public void onAdClicked(InterstitialView interstitialView) {
            LogUtil.debug(TAG, "onAdClicked");
            if (mListener != null) {
                mListener.onInterstitialClicked();
            }
        }

        @Override
        public void onAdClickThroughClosed(InterstitialView interstitialView) {

        }

        @Override
        public void onAdClosed(InterstitialView interstitialView) {
            LogUtil.debug(TAG, "onAdClosed");
            if (mListener != null) {
                mListener.onInterstitialClosed();
            }
        }
    };

    public InterstitialController(Context context, InterstitialControllerListener listener)
    throws AdException {
        mListener = listener;
        mBidInterstitialView = new InterstitialView(context);
        mBidInterstitialView.setInterstitialViewListener(mInterstitialViewListener);
        mBidInterstitialView.setPubBackGroundOpacity(1.0f);
    }

    public void loadAd(AdConfiguration adUnitConfiguration, BidResponse bidResponse) {
        WinNotifier winNotifier = new WinNotifier();
        winNotifier.notifyWin(bidResponse, () -> {
            mAdUnitIdentifierType = bidResponse.isVideo()
                                    ? AdConfiguration.AdUnitIdentifierType.VAST
                                    : AdConfiguration.AdUnitIdentifierType.INTERSTITIAL;
            adUnitConfiguration.setAdUnitIdentifierType(mAdUnitIdentifierType);
            mBidInterstitialView.loadAd(adUnitConfiguration, bidResponse);
        });
    }

    public void loadAd(String responseId, boolean isRewarded) {
        BidResponse bidResponse = BidResponseCache.getInstance().popBidResponse(responseId);
        if (bidResponse == null) {
            if (mListener != null) {
                mListener.onInterstitialFailedToLoad(new AdException(AdException.INTERNAL_ERROR, "No bid response found in the cache"));
            }
            return;
        }
        AdConfiguration adUnitConfiguration = new AdConfiguration();
        adUnitConfiguration.setRewarded(isRewarded);
        loadAd(adUnitConfiguration, bidResponse);
    }

    public void show() {
        if (mAdUnitIdentifierType == null) {
            LogUtil.error(TAG, "show: Failed. AdUnitIdentifierType is not defined!");
            return;
        }

        switch (mAdUnitIdentifierType) {
            case INTERSTITIAL:
                mBidInterstitialView.showAsInterstitialFromRoot();
                break;
            case VAST:
                mBidInterstitialView.showVideoAsInterstitial();
                break;
            default:
                LogUtil.error(TAG, "show: Failed. Did you specify correct AdUnitConfigurationType? "
                                   + "Supported types: VAST, INTERSTITIAL. "
                                   + "Provided type: " + mAdUnitIdentifierType);
        }
    }

    public void destroy() {
        mBidInterstitialView.destroy();
    }
}
