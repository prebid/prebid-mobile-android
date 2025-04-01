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

import androidx.annotation.Nullable;

import org.prebid.mobile.ContentObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.utils.broadcast.local.EventForwardingLocalBroadcastReceiver;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

/**
 * This class provides common functionality for Interstitial and Banner ads.
 */
public abstract class BaseAdView extends FrameLayout {

    private static final String TAG = BaseAdView.class.getSimpleName();

    protected AdViewManager adViewManager;
    protected InterstitialManager interstitialManager = new InterstitialManager();

    private EventForwardingLocalBroadcastReceiver eventForwardingReceiver;
    private final EventForwardingLocalBroadcastReceiver.EventForwardingBroadcastListener broadcastListener = this::handleBroadcastAction;

    private int screenVisibility;

    public BaseAdView(Context context) {
        super(context);
    }

    public BaseAdView(
            Context context,
            AttributeSet attrs
    ) {
        super(context, attrs);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        handleWindowFocusChange(hasWindowFocus);
    }

    public long getMediaDuration() {
        if (adViewManager != null) {
            return adViewManager.getMediaDuration();
        }
        return 0;
    }

    public long getMediaOffset() {
        if (adViewManager != null) {
            return adViewManager.getSkipOffset();
        }
        return AdUnitConfiguration.SKIP_OFFSET_NOT_ASSIGNED;
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
        String hostUrl = PrebidMobile.getPrebidServerHost().getHostUrl();
        if (!hostUrl.isEmpty()) {
            PrebidMobile.initializeSdk(getContext(), hostUrl, null);
        }
    }

    protected void registerEventBroadcast() {
        final int broadcastId = adViewManager.getAdConfiguration().getBroadcastId();
        eventForwardingReceiver = new EventForwardingLocalBroadcastReceiver(broadcastId, broadcastListener);
        eventForwardingReceiver.register(getContext(), eventForwardingReceiver);
    }

    protected void setScreenVisibility(int screenVisibility) {
        this.screenVisibility = screenVisibility;
    }

    protected void handleBroadcastAction(String action) {
        LogUtil.debug(TAG, "handleBroadcastAction: parent method executed. No default action handling. " + action);
    }

    protected void handleWindowFocusChange(boolean hasWindowFocus) {
        int visibility = (!hasWindowFocus ? View.INVISIBLE : View.VISIBLE);
        if (Utils.hasScreenVisibilityChanged(screenVisibility, visibility) && adViewManager != null) {
            screenVisibility = visibility;
            adViewManager.setAdVisibility(screenVisibility);
        }
    }

    protected abstract void notifyErrorListeners(final AdException adException);

    public void destroy() {
        if (adViewManager != null) {
            adViewManager.destroy();
        }

        if (eventForwardingReceiver != null) {
            eventForwardingReceiver.unregister(eventForwardingReceiver);
            eventForwardingReceiver = null;
        }
    }


    @Nullable
    public String getImpOrtbConfig() {
        return adViewManager.getAdConfiguration().getImpOrtbConfig();
    }

    public void setImpOrtbConfig(@Nullable String ortbConfig) {
        adViewManager.getAdConfiguration().setImpOrtbConfig(ortbConfig);
    }

}
