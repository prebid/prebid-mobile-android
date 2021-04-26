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

package org.prebid.mobile.rendering.views.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.broadcast.local.EventForwardingLocalBroadcastReceiver;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

/**
 * This class provides common functionality for Interstitial and Banner ads.
 */
public abstract class BaseAdView extends FrameLayout {
    private static final String TAG = BaseAdView.class.getSimpleName();

    protected AdViewManager mAdViewManager;
    protected InterstitialManager mInterstitialManager = new InterstitialManager();

    private EventForwardingLocalBroadcastReceiver mEventForwardingReceiver;
    private final EventForwardingLocalBroadcastReceiver.EventForwardingBroadcastListener mBroadcastListener = this::handleBroadcastAction;

    private int mScreenVisibility;

    public BaseAdView(Context context) {
        super(context);
    }

    public BaseAdView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        handleWindowFocusChange(hasWindowFocus);
    }

    public long getMediaDuration() {
        if (mAdViewManager != null) {
            return mAdViewManager.getMediaDuration();
        }
        return 0;
    }

    public long getMediaOffset() {
        if (mAdViewManager != null) {
            return mAdViewManager.getSkipOffset();
        }
        return AdConfiguration.SKIP_OFFSET_NOT_ASSIGNED;
    }

    public void setContentUrl(String contentUrl) {
        if (mAdViewManager == null) {
            OXLog.error(TAG, "setContentUrl: Failed. AdViewManager is null");
            return;
        }
        mAdViewManager.getAdConfiguration().setContentUrl(contentUrl);
    }

    /**
     * @return a creative view associated with the displayed ad
     */
    public View getCreativeView() {
        return getChildAt(0);
    }

    protected void init() throws AdException {
        int visibility = getVisibility();

        setScreenVisibility(visibility);
        PrebidRenderingSettings.initializeSDK(getContext(), null);
    }

    protected void registerEventBroadcast() {
        final int broadcastId = mAdViewManager.getAdConfiguration().getBroadcastId();
        mEventForwardingReceiver = new EventForwardingLocalBroadcastReceiver(broadcastId, mBroadcastListener);
        mEventForwardingReceiver.register(getContext(), mEventForwardingReceiver);
    }

    protected void setScreenVisibility(int screenVisibility) {
        mScreenVisibility = screenVisibility;
    }

    protected void handleBroadcastAction(String action) {
        OXLog.debug(TAG, "handleBroadcastAction: parent method executed. No default action handling. " + action);
    }

    protected void handleWindowFocusChange(boolean hasWindowFocus) {
        int visibility = (!hasWindowFocus ? View.INVISIBLE : View.VISIBLE);
        if (Utils.hasScreenVisibilityChanged(mScreenVisibility, visibility) && mAdViewManager != null) {
            mScreenVisibility = visibility;
            mAdViewManager.setAdVisibility(mScreenVisibility);
        }
    }

    protected abstract void notifyErrorListeners(final AdException adException);

    public void destroy() {
        if (mAdViewManager != null) {
            mAdViewManager.destroy();
        }

        if (mEventForwardingReceiver != null) {
            mEventForwardingReceiver.unregister(mEventForwardingReceiver);
            mEventForwardingReceiver = null;
        }
    }
}
