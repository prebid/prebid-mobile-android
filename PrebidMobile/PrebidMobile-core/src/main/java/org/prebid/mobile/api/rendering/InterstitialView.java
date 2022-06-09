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
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.core.R;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialViewListener;
import org.prebid.mobile.rendering.interstitial.DialogEventListener;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.models.internal.InternalFriendlyObstruction;
import org.prebid.mobile.rendering.utils.constants.IntentActions;
import org.prebid.mobile.rendering.utils.helpers.InsetsUtils;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.AdViewManagerListener;
import org.prebid.mobile.rendering.views.base.BaseAdView;
import org.prebid.mobile.rendering.views.interstitial.InterstitialVideo;

import java.util.Arrays;
import java.util.List;

public class InterstitialView extends BaseAdView {

    private static final String TAG = InterstitialView.class.getSimpleName();

    private InterstitialViewListener listener;
    protected InterstitialVideo interstitialVideo;

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        List<View> views = Arrays.asList(
            findViewById(R.id.iv_close_interstitial),
            findViewById(R.id.iv_skip),
            findViewById(R.id.rl_count_down),
            findViewById(R.id.tv_learn_more)
        );

        for (View view : views) {
            InsetsUtils.resetMargins(view);
            InsetsUtils.addCutoutAndNavigationInsets(view);
        }
    }

    //region ========== Listener Area
    private final AdViewManagerListener onAdViewManagerListener = new AdViewManagerListener() {
        @Override
        public void adLoaded(final AdDetails adDetails) {
            listener.onAdLoaded(InterstitialView.this, adDetails);
        }

        @Override
        public void viewReadyForImmediateDisplay(View view) {
            if (adViewManager.isNotShowingEndCard()) {

                listener.onAdDisplayed(InterstitialView.this);
            }

            removeAllViews();

            addView(view);
        }

        @Override
        public void failedToLoad(AdException error) {
            notifyErrorListeners(error);
        }

        @Override
        public void adCompleted() {
            listener.onAdCompleted(InterstitialView.this);

            if (interstitialVideo != null && interstitialVideo.shouldShowCloseButtonOnComplete()) {
                interstitialVideo.changeCloseViewVisibility(View.VISIBLE);
            }
        }

        @Override
        public void creativeClicked(String url) {
            listener.onAdClicked(InterstitialView.this);
        }

        @Override
        public void creativeInterstitialClosed() {
            LogUtil.debug(TAG, "interstitialAdClosed");
            handleActionClose();
        }
    };
    //endregion ========== Listener Area

    public InterstitialView(Context context) throws AdException {
        super(context);
        init();
    }

    public void setPubBackGroundOpacity(float opacity) {
        interstitialManager.getInterstitialDisplayProperties().setPubBackGroundOpacity(opacity);
    }

    public void loadAd(
        AdUnitConfiguration adUnitConfiguration,
        BidResponse bidResponse
    ) {
        adViewManager.loadBidTransaction(adUnitConfiguration, bidResponse);
    }

    public void setInterstitialViewListener(InterstitialViewListener listener) {
        this.listener = listener;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (interstitialVideo != null) {
            if (!hasWindowFocus) {
                interstitialVideo.pauseVideo();
            } else {
                interstitialVideo.resumeVideo();
            }
        }
    }

    @Override
    public void destroy() {
        // Because user can click back, even before the adview is created.
        // so, activity's destroy() calling adview's destry should not crash
        super.destroy();

        if (interstitialVideo != null) {
            interstitialVideo.hide();
            interstitialVideo.cancel();
            interstitialVideo.removeViews();
        }
    }

    public void showAsInterstitialFromRoot() {
        try {
            interstitialManager.configureInterstitialProperties(adViewManager.getAdConfiguration());
            interstitialManager.displayAdViewInInterstitial(getContext(), InterstitialView.this);
        } catch (final Exception e) {
            LogUtil.error(TAG, "Interstitial failed to show:" + Log.getStackTraceString(e));
            notifyErrorListeners(new AdException(AdException.INTERNAL_ERROR, e.getMessage()));
        }
    }

    public void showVideoAsInterstitial() {
        try {
            final AdUnitConfiguration adConfiguration = adViewManager.getAdConfiguration();
            interstitialManager.configureInterstitialProperties(adConfiguration);
            interstitialVideo = new InterstitialVideo(
                getContext(),
                InterstitialView.this,
                interstitialManager,
                adConfiguration
            );
            interstitialVideo.setHasEndCard(adViewManager.hasNextCreative());
            interstitialVideo.setDialogListener(this::handleDialogEvent);
            interstitialVideo.show();
        } catch (final Exception e) {
            LogUtil.error(TAG, "Video interstitial failed to show:" + Log.getStackTraceString(e));

            notifyErrorListeners(new AdException(AdException.INTERNAL_ERROR, e.getMessage()));
        }
    }

    public void closeInterstitialVideo() {
        if (interstitialVideo != null) {
            if (interstitialVideo.isShowing()) {
                interstitialVideo.close();
            }
            interstitialVideo = null;
        }
    }

    @Override
    protected void init() throws AdException {
        try {
            super.init();
            setAdViewManagerValues();
            registerEventBroadcast();
        } catch (Exception e) {
            throw new AdException(AdException.INIT_ERROR, "AdView initialization failed: " + Log.getStackTraceString(e));
        }
    }

    @Override
    protected void notifyErrorListeners(final AdException adException) {
        if (listener != null) {
            listener.onAdFailed(InterstitialView.this, adException);
        }
    }

    @Override
    protected void handleBroadcastAction(String action) {
        if (IntentActions.ACTION_BROWSER_CLOSE.equals(action)) {
            listener.onAdClickThroughClosed(InterstitialView.this);
        }
    }

    protected void setAdViewManagerValues() throws AdException {
        adViewManager = new AdViewManager(getContext(), onAdViewManagerListener, this, interstitialManager);
        AdUnitConfiguration adConfiguration = adViewManager.getAdConfiguration();
        adConfiguration.setAutoRefreshDelay(0);
    }

    protected InternalFriendlyObstruction[] formInterstitialObstructionsArray() {
        InternalFriendlyObstruction[] obstructionArray = new InternalFriendlyObstruction[5];

        View closeInterstitial = findViewById(R.id.iv_close_interstitial);
        View skipInterstitial = findViewById(R.id.iv_skip);
        View countDownTimer = findViewById(R.id.rl_count_down);
        View actionButton = findViewById(R.id.tv_learn_more);

        obstructionArray[0] = new InternalFriendlyObstruction(closeInterstitial, InternalFriendlyObstruction.Purpose.CLOSE_AD, null);
        obstructionArray[1] = new InternalFriendlyObstruction(skipInterstitial, InternalFriendlyObstruction.Purpose.CLOSE_AD, null);
        obstructionArray[2] = new InternalFriendlyObstruction(countDownTimer, InternalFriendlyObstruction.Purpose.OTHER, "CountDownTimer");
        obstructionArray[3] = new InternalFriendlyObstruction(actionButton, InternalFriendlyObstruction.Purpose.OTHER, "Action button");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            View dialogRoot = closeInterstitial.getRootView();
            View navigationBar = dialogRoot.findViewById(android.R.id.navigationBarBackground);
            obstructionArray[4] = new InternalFriendlyObstruction(navigationBar, InternalFriendlyObstruction.Purpose.OTHER, "Bottom navigation bar");
        } else {
            obstructionArray[4] = null;
        }

        return obstructionArray;
    }

    private void handleDialogEvent(DialogEventListener.EventType eventType) {
        if (eventType == DialogEventListener.EventType.SHOWN) {
            adViewManager.addObstructions((formInterstitialObstructionsArray()));
        } else if (eventType == DialogEventListener.EventType.CLOSED) {
            handleActionClose();
        } else if (eventType == DialogEventListener.EventType.MUTE) {
            adViewManager.mute();
        } else if (eventType == DialogEventListener.EventType.UNMUTE) {
            adViewManager.unmute();
        }
    }

    private void handleActionClose() {
        if (adViewManager.isInterstitialClosed()) {
            adViewManager.trackCloseEvent();
            return;
        }

        adViewManager.resetTransactionState();

        listener.onAdClosed(InterstitialView.this);
    }

}
