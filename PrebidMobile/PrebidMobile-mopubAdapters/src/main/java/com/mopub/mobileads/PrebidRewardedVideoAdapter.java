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

package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mopub.common.LifecycleListener;
import com.mopub.common.MoPubReward;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mediation.MoPubRewardedVideoMediationUtils;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.errors.AdException;

public class PrebidRewardedVideoAdapter extends BaseAd {
    private static final String TAG = PrebidRewardedVideoAdapter.class.getSimpleName();

    private InterstitialController mInterstitialController;
    private InterstitialControllerListener mInterstitialListener = new InterstitialControllerListener() {
        @Override
        public void onInterstitialReadyForDisplay() {
            MoPubLog.log(MoPubLog.AdapterLogEvent.LOAD_SUCCESS, TAG);
            mLoadListener.onAdLoaded();
        }

        @Override
        public void onInterstitialClicked() {
            MoPubLog.log(MoPubLog.AdapterLogEvent.CLICKED, TAG);
            mInteractionListener.onAdClicked();
        }

        @Override
        public void onInterstitialFailedToLoad(AdException exception) {
            MoPubLog.log(MoPubLog.AdapterLogEvent.LOAD_FAILED, TAG);
            mLoadListener.onAdLoadFailed(MoPubErrorCode.NO_FILL);
        }

        @Override
        public void onInterstitialDisplayed() {
            MoPubLog.log(MoPubLog.AdapterLogEvent.SHOW_SUCCESS, TAG);
            mInteractionListener.onAdShown();
        }

        @Override
        public void onInterstitialClosed() {
            MoPubLog.log(MoPubLog.AdapterLogEvent.DID_DISAPPEAR, TAG);
            MoPubLog.log(MoPubLog.AdapterLogEvent.CUSTOM, TAG, "Display - adDidComplete");
            mInteractionListener.onAdComplete(MoPubReward.success(MoPubReward.NO_REWARD_LABEL, MoPubReward.DEFAULT_REWARD_AMOUNT));
            mInteractionListener.onAdDismissed();
        }
    };

    @Override
    protected void onInvalidate() {
        if (mInterstitialController != null) {
            mInterstitialController.destroy();
        }
    }

    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        return null;
    }

    @NonNull
    @Override
    protected String getAdNetworkId() {
        return "";
    }

    @Override
    protected boolean checkAndInitializeSdk(
            @NonNull Activity launcherActivity,
            @NonNull AdData adData
    ) {
        return false;
    }

    @Override
    protected void load(
            @NonNull Context context,
            @NonNull AdData adData
    ) {
        String cacheId = MoPubRewardedVideoMediationUtils.cacheId;
        if (TextUtils.isEmpty(cacheId)) {
            mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        initInterstitialController(context, cacheId);
    }

    @Override
    protected void show() {
        if (mInterstitialController != null) {
            mInterstitialController.show();
        }
    }

    private void initInterstitialController(Context context, String responseId) {
        try {
            mInterstitialController = new InterstitialController(context, mInterstitialListener);
            mInterstitialController.loadAd(responseId, true);
        }
        catch (AdException e) {
            MoPubLog.log(MoPubLog.AdapterLogEvent.LOAD_FAILED, TAG);
            mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        }
    }
}
