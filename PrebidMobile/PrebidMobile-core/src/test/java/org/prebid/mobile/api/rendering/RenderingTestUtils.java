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

import androidx.annotation.Nullable;

import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayVideoListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.bidding.listeners.InterstitialEventListener;
import org.prebid.mobile.rendering.bidding.listeners.RewardedVideoEventListener;
import org.prebid.mobile.test.utils.WhiteBox;

final class RenderingTestUtils {

    private RenderingTestUtils() {
    }

    static BidRequesterListener getBidRequesterListener(BannerView bannerView) {
        try {
            return (BidRequesterListener) WhiteBox.field(BannerView.class, "bidRequesterListener").get(bannerView);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    static BidRequesterListener getBidRequesterListener(BaseInterstitialAdUnit adUnit) {
        return (BidRequesterListener) WhiteBox.getInternalState(adUnit, "bidRequesterListener");
    }

    static DisplayViewListener getDisplayViewListener(BannerView bannerView) {
        try {
            return (DisplayViewListener) WhiteBox.field(BannerView.class, "displayViewListener").get(bannerView);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    static DisplayVideoListener getDisplayVideoListener(BannerView bannerView) {
        try {
            return (DisplayVideoListener) WhiteBox.field(BannerView.class, "displayVideoListener").get(bannerView);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    static BannerEventListener getBannerEventListener(BannerView bannerView) {
        try {
            return (BannerEventListener) WhiteBox.field(BannerView.class, "bannerEventListener").get(bannerView);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    static InterstitialEventListener getInterstitialEventListener(InterstitialAdUnit adUnit) {
        return (InterstitialEventListener) WhiteBox.getInternalState(adUnit, "interstitialEventListener");
    }

    static RewardedVideoEventListener getRewardedVideoEventListener(RewardedAdUnit adUnit) {
        return (RewardedVideoEventListener) WhiteBox.getInternalState(adUnit, "eventListener");
    }

    static void changeInterstitialState(
        BaseInterstitialAdUnit adUnit,
        BaseInterstitialAdUnit.InterstitialAdUnitState adUnitState
    ) {
        adUnit.changeInterstitialAdUnitState(adUnitState);
    }

    static class TestBaseInterstitialAdUnit extends BaseInterstitialAdUnit {

        TestBaseInterstitialAdUnit(Context context) {
            super(context);
        }

        @Override
        void requestAdWithBid(@Nullable Bid bid) {
        }

        @Override
        void showGamAd() {
        }

        @Override
        void notifyAdEventListener(AdListenerEvent adListenerEvent) {
        }

        @Override
        void notifyErrorListener(AdException exception) {
        }

        @Override
        void notifyAdExpiredListener() {
        }
    }

}
