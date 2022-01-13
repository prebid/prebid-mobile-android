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
import android.util.Log;
import android.view.View;

import org.prebid.mobile.rendering.R;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialViewListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.interstitial.DialogEventListener;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.models.internal.InternalFriendlyObstruction;
import org.prebid.mobile.rendering.utils.constants.IntentActions;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.AdViewManagerListener;
import org.prebid.mobile.rendering.views.base.BaseAdView;
import org.prebid.mobile.rendering.views.interstitial.InterstitialVideo;

public class InterstitialView extends BaseAdView {
    private static final String TAG = InterstitialView.class.getSimpleName();

    private InterstitialViewListener mListener;
    protected InterstitialVideo mInterstitialVideo;

    //region ========== Listener Area
    private final AdViewManagerListener mOnAdViewManagerListener = new AdViewManagerListener() {
        @Override
        public void adLoaded(final AdDetails adDetails) {
            mListener.onAdLoaded(InterstitialView.this, adDetails);
        }

        @Override
        public void viewReadyForImmediateDisplay(View view) {
            if (mAdViewManager.isNotShowingEndCard()) {

                mListener.onAdDisplayed(InterstitialView.this);
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
            mListener.onAdCompleted(InterstitialView.this);

            if (mInterstitialVideo != null && mInterstitialVideo.shouldShowCloseButtonOnComplete()) {
                mInterstitialVideo.changeCloseViewVisibility(View.VISIBLE);
            }
        }

        @Override
        public void creativeClicked(String url) {
            mListener.onAdClicked(InterstitialView.this);
        }

        @Override
        public void creativeInterstitialClosed() {
            LogUtil.debug(TAG, "interstitialAdClosed");
            handleActionClose();
        }
    };
    //endregion ========== Listener Area

    protected InterstitialView(Context context) throws AdException {
        super(context);
        init();
    }

    public void setPubBackGroundOpacity(float opacity) {
        mInterstitialManager.getInterstitialDisplayProperties().setPubBackGroundOpacity(opacity);
    }

    public void loadAd(AdConfiguration adUnitConfiguration, BidResponse bidResponse) {
        mAdViewManager.loadBidTransaction(adUnitConfiguration, bidResponse);
    }

    public void setInterstitialViewListener(InterstitialViewListener listener) {
        mListener = listener;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (mInterstitialVideo != null) {
            if (!hasWindowFocus) {
                mInterstitialVideo.pauseVideo();
            }
            else {
                mInterstitialVideo.resumeVideo();
            }
        }
    }

    @Override
    public void destroy() {
        // Because user can click back, even before the adview is created.
        // so, activity's destroy() calling adview's destry should not crash
        super.destroy();

        if (mInterstitialVideo != null) {
            mInterstitialVideo.hide();
            mInterstitialVideo.cancel();
            mInterstitialVideo.removeViews();
        }
    }

    public void showAsInterstitialFromRoot() {
        try {
            mInterstitialManager.configureInterstitialProperties(mAdViewManager.getAdConfiguration());
            mInterstitialManager.displayAdViewInInterstitial(getContext(), InterstitialView.this);
        }
        catch (final Exception e) {
            LogUtil.error(TAG, "Interstitial failed to show:" + Log.getStackTraceString(e));
            notifyErrorListeners(new AdException(AdException.INTERNAL_ERROR, e.getMessage()));
        }
    }

    public void showVideoAsInterstitial() {
        try {
            final AdConfiguration adConfiguration = mAdViewManager.getAdConfiguration();
            mInterstitialManager.configureInterstitialProperties(adConfiguration);
            mInterstitialVideo = new InterstitialVideo(getContext(),
                                                       InterstitialView.this,
                                                       mInterstitialManager,
                                                       adConfiguration);
            mInterstitialVideo.setDialogListener(this::handleDialogEvent);
            mInterstitialVideo.show();
        }
        catch (final Exception e) {
            LogUtil.error(TAG, "Video interstitial failed to show:" + Log.getStackTraceString(e));

            notifyErrorListeners(new AdException(AdException.INTERNAL_ERROR, e.getMessage()));
        }
    }

    public void closeInterstitialVideo() {
        if (mInterstitialVideo != null) {
            if (mInterstitialVideo.isShowing()) {
                mInterstitialVideo.close();
            }
            mInterstitialVideo = null;
        }
    }

    @Override
    protected void init() throws AdException {
        try {
            super.init();
            setAdViewManagerValues();
            registerEventBroadcast();
        }
        catch (Exception e) {
            throw new AdException(AdException.INIT_ERROR, "AdView initialization failed: " + Log.getStackTraceString(e));
        }
    }

    @Override
    protected void notifyErrorListeners(final AdException adException) {
        if (mListener != null) {
            mListener.onAdFailed(InterstitialView.this, adException);
        }
    }

    @Override
    protected void handleBroadcastAction(String action) {
        if (IntentActions.ACTION_BROWSER_CLOSE.equals(action)) {
            mListener.onAdClickThroughClosed(InterstitialView.this);
        }
    }

    protected void setAdViewManagerValues() throws AdException {
        mAdViewManager = new AdViewManager(getContext(), mOnAdViewManagerListener, this, mInterstitialManager);
        AdConfiguration adConfiguration = mAdViewManager.getAdConfiguration();
        adConfiguration.setAutoRefreshDelay(0);
    }

    protected InternalFriendlyObstruction[] formInterstitialObstructionsArray() {
        InternalFriendlyObstruction[] obstructionArray = new InternalFriendlyObstruction[3];

        View closeInterstitial = findViewById(R.id.iv_close_interstitial);
        View countDownTimer = findViewById(R.id.rl_count_down);
        View actionButton = findViewById(R.id.tv_learn_more);

        obstructionArray[0] = new InternalFriendlyObstruction(closeInterstitial, InternalFriendlyObstruction.Purpose.CLOSE_AD, null);
        obstructionArray[1] = new InternalFriendlyObstruction(countDownTimer, InternalFriendlyObstruction.Purpose.OTHER, "CountDownTimer");
        obstructionArray[2] = new InternalFriendlyObstruction(actionButton, InternalFriendlyObstruction.Purpose.OTHER, "Action button");

        return obstructionArray;
    }

    private void handleDialogEvent(DialogEventListener.EventType eventType) {
        if (eventType == DialogEventListener.EventType.SHOWN) {
            mAdViewManager.addObstructions((formInterstitialObstructionsArray()));
        }
        else if (eventType == DialogEventListener.EventType.CLOSED) {
            handleActionClose();
        }
    }

    private void handleActionClose() {
        if (mAdViewManager.isInterstitialClosed()) {
            mAdViewManager.trackCloseEvent();
            return;
        }

        mAdViewManager.resetTransactionState();

        mListener.onAdClosed(InterstitialView.this);
    }
}
