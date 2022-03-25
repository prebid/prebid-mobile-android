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
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mopub.common.LifecycleListener;
import com.mopub.common.logging.MoPubLog;
import org.prebid.mobile.rendering.bidding.display.DisplayView;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;

/**
 * To integrate, place a copy of this class into 'com.mopub.mobileads'
 */
public class PrebidBannerAdapter extends BaseAd {

    private static final String TAG = PrebidBannerAdapter.class.getSimpleName();
    private static final String KEY_BID_RESPONSE = "PREBID_BID_RESPONSE_ID";

    private DisplayView mDisplayView;
    private DisplayViewListener mDisplayViewListener = new DisplayViewListener() {

        @Override
        public void onAdLoaded() {
            MoPubLog.log(MoPubLog.AdapterLogEvent.LOAD_SUCCESS, TAG);
            mLoadListener.onAdLoaded();
        }

        @Override
        public void onAdDisplayed() {
            MoPubLog.log(MoPubLog.AdapterLogEvent.SHOW_SUCCESS, TAG);
            mInteractionListener.onAdShown();
        }

        @Override
        public void onAdFailed(AdException exception) {
            MoPubLog.log(MoPubLog.AdapterLogEvent.LOAD_FAILED, TAG);
            mLoadListener.onAdLoadFailed(MoPubErrorCode.NO_FILL);
        }

        @Override
        public void onAdClicked() {
            MoPubLog.log(MoPubLog.AdapterLogEvent.CLICKED, TAG);
            mInteractionListener.onAdClicked();
        }

        @Override
        public void onAdClosed() {
            MoPubLog.log(MoPubLog.AdapterLogEvent.CUSTOM, TAG, "onAdClosed");
            mInteractionListener.onAdCollapsed();
        }
    };

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
        @NonNull
            Activity launcherActivity,
        @NonNull
            AdData adData) throws Exception {
        return false;
    }

    @Override
    protected void load(
        @NonNull
            Context context,
        @NonNull
            AdData adData) {
        if (!adData.getExtras().containsKey(KEY_BID_RESPONSE)) {
            mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        initDisplayView(context, adData.getExtras().get(KEY_BID_RESPONSE));
    }

    @Nullable
    @Override
    protected View getAdView() {
        return mDisplayView;
    }

    @Override
    protected void onInvalidate() {
        if (mDisplayView != null) {
            mDisplayView.destroy();
        }
    }

    private void initDisplayView(Context context, String responseId) {
        try {
            AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
            adUnitConfiguration.setAdUnitIdentifierType(AdUnitConfiguration.AdUnitIdentifierType.BANNER);
            mDisplayView = new DisplayView(context, mDisplayViewListener, adUnitConfiguration, responseId);
        }
        catch (AdException e) {
            MoPubLog.log(MoPubLog.AdapterLogEvent.LOAD_FAILED, TAG);
            mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        }
    }
}
