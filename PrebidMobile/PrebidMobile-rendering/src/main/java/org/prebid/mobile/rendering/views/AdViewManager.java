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

package org.prebid.mobile.rendering.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.prebid.mobile.rendering.R;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.InterstitialView;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.listeners.CreativeViewListener;
import org.prebid.mobile.rendering.loading.CreativeFactory;
import org.prebid.mobile.rendering.loading.Transaction;
import org.prebid.mobile.rendering.loading.TransactionManager;
import org.prebid.mobile.rendering.loading.TransactionManagerListener;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.InternalFriendlyObstruction;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.video.VideoAdEvent;
import org.prebid.mobile.rendering.video.VideoCreative;
import org.prebid.mobile.rendering.video.VideoCreativeView;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

import java.lang.ref.WeakReference;
import java.util.List;

public class AdViewManager implements CreativeViewListener, TransactionManagerListener {
    private static final String TAG = AdViewManager.class.getSimpleName();

    private final InterstitialManager mInterstitialManager;

    private AdConfiguration mAdConfiguration = new AdConfiguration();
    private TransactionManager mTransactionManager;
    private WeakReference<Context> mContextReference;
    private ViewGroup mAdView;
    private AdViewManagerListener mAdViewListener;
    private AbstractCreative mCurrentCreative;
    private AbstractCreative mLastCreativeShown;

    private AdViewManagerInterstitialDelegate mDelegate = this::show;

    public AdViewManager(Context context, AdViewManagerListener adViewListener, ViewGroup adView, InterstitialManager interstitialManager)
    throws AdException {

        if (context == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Context is null");
        }

        if (adViewListener == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "AdViewManagerListener is null");
        }

        mContextReference = new WeakReference<>(context);
        mAdView = adView;
        mAdViewListener = adViewListener;
        mTransactionManager = new TransactionManager(context, this, interstitialManager);
        mInterstitialManager = interstitialManager;
        mInterstitialManager.setAdViewManagerInterstitialDelegate(mDelegate);
    }

    @Override
    public void onFetchingCompleted(Transaction transaction) {
        processTransaction(transaction);
    }

    @Override
    public void onFetchingFailed(AdException exception) {
        OXLog.error(TAG, "There was an error fetching an ad " + exception.toString());
        mAdViewListener.failedToLoad(exception);
    }

    @Override
    public void creativeWasClicked(AbstractCreative creative, String url) {
        mAdViewListener.creativeClicked(url);
    }

    @Override
    public void creativeInterstitialDidClose(AbstractCreative creative) {
        OXLog.debug(TAG, "creativeInterstitialDidClose");

        Transaction currentTransaction = mTransactionManager.getCurrentTransaction();
        if (creative.isDisplay() && creative.isEndCard()) {

            // Call ad close event for video tracking event
            currentTransaction.getCreativeFactories().get(0)
                              .getCreative().trackVideoEvent(VideoAdEvent.Event.AD_CLOSE);
        }

        // Transaction is complete
        resetTransactionState();

        mAdViewListener.creativeInterstitialClosed();
    }

    @Override
    public void creativeDidExpand(AbstractCreative creative) {
        mAdViewListener.creativeExpanded();
    }

    @Override
    public void creativeDidCollapse(AbstractCreative creative) {
        mAdViewListener.creativeCollapsed();
    }

    @Override
    public void creativeInterstitialDialogShown(ViewGroup rootViewGroup) {
        addHtmlInterstitialObstructions(rootViewGroup);
    }

    @Override
    public void creativeMuted(AbstractCreative creative) {
        mAdViewListener.creativeMuted();
    }

    @Override
    public void creativeUnMuted(AbstractCreative creative) {
        mAdViewListener.creativeUnMuted();
    }

    @Override
    public void creativePaused(AbstractCreative creative) {
        mAdViewListener.creativePaused();
    }

    @Override
    public void creativeResumed(AbstractCreative creative) {
        mAdViewListener.creativeResumed();
    }

    @Override
    public void creativeDidComplete(AbstractCreative creative) {
        OXLog.debug(TAG, "creativeDidComplete");

        // NOTE: This is currently hard-wired to work for video + end card only
        //       To truly support continuous ads in a queue, there would need to be significant changes
        //       in the display layer logic
        if (creative.isVideo()) {
            handleVideoCreativeComplete(creative);
        }

        // Clean up on refresh
        if (creative.isDisplay()) {
            resetTransactionState();
        }

        mAdViewListener.adCompleted();

        // If banner refresh enabled and another ad is available, show that ad
        if (isAutoDisplayOnLoad() && mTransactionManager.hasTransaction()) {
            show();
        }
    }

    public void resetTransactionState() {
        hide();
        mTransactionManager.resetState();
    }

    public void hide() {
        if (mCurrentCreative == null) {
            OXLog.warn(TAG, "Can not hide a null creative");
            return;
        }

        if (mAdView != null && mAdView.indexOfChild(mCurrentCreative.getCreativeView()) != -1) {
            mAdView.removeView(mCurrentCreative.getCreativeView());
            mCurrentCreative = null;
        }
    }

    public void setAdVisibility(int visibility) {
        if (mCurrentCreative == null) {
            OXLog.debug(TAG, "setAdVisibility(): Skipping creative window focus notification. mCurrentCreative is null");
            return;
        }

        if (Utils.isScreenVisible(visibility)) {
            //handles resuming of show timer for auid ads & video resume for video ads
            mCurrentCreative.handleAdWindowFocus();
        }
        else {
            //handles pausing of show timer for auid ads & video pause for video ads
            mCurrentCreative.handleAdWindowNoFocus();
        }
    }

    public AdConfiguration getAdConfiguration() {
        return mAdConfiguration;
    }

    public boolean isAutoDisplayOnLoad() {
        return mAdConfiguration.getAdUnitIdentifierType() == AdConfiguration.AdUnitIdentifierType.BANNER;
    }

    public void destroy() {
        if (mTransactionManager != null) {
            mTransactionManager.destroy();
        }
        if (mInterstitialManager != null) {
            mInterstitialManager.destroy();
        }
        if (mCurrentCreative != null) {
            mCurrentCreative.destroy();
        }
    }

    public void pause() {
        if (mCurrentCreative != null) {
            mCurrentCreative.pause();
        }
    }

    public void resume() {
        if (mCurrentCreative != null) {
            mCurrentCreative.resume();
        }
    }

    public void mute() {
        if (mCurrentCreative != null) {
            mCurrentCreative.mute();
        }
    }

    public void unmute() {
        if (mCurrentCreative != null) {
            mCurrentCreative.unmute();
        }
    }

    public boolean isNotShowingEndCard() {
        return mCurrentCreative != null && (!(mCurrentCreative.isDisplay())
                                            || !mCurrentCreative.isEndCard());
    }

    public boolean hasEndCard() {
        return mCurrentCreative != null && mCurrentCreative.isDisplay();
    }

    public void updateAdView(View view) {
        mCurrentCreative.updateAdView(view);
    }

    public void trackVideoStateChange(InternalPlayerState state) {
        mCurrentCreative.trackVideoStateChange(state);
    }

    public boolean canShowFullScreen() {
        return mCurrentCreative != null && mCurrentCreative.isBuiltInVideo();
    }

    public void returnFromVideo(View callingView) {
        if (mCurrentCreative != null && mCurrentCreative.isBuiltInVideo()) {
            View creativeView = mCurrentCreative.getCreativeView();
            if (creativeView instanceof VideoCreativeView && mCurrentCreative.isVideo()) {
                VideoCreativeView videoCreativeView = (VideoCreativeView) creativeView;
                VideoCreative videoCreative = (VideoCreative) mCurrentCreative;
                videoCreativeView.hideCallToAction();
                videoCreativeView.mute();
                videoCreative.updateAdView(callingView);
                videoCreative.onPlayerStateChanged(InternalPlayerState.NORMAL);
            }
        }
    }

    public boolean isPlaying() {
        return mCurrentCreative != null && mCurrentCreative.isPlaying();
    }

    public boolean isInterstitialClosed() {
        return mCurrentCreative != null && mCurrentCreative.isInterstitialClosed();
    }

    public void trackCloseEvent() {
        if (mCurrentCreative != null) {
            mCurrentCreative.trackVideoEvent(VideoAdEvent.Event.AD_CLOSE);
        }
    }

    public long getMediaDuration() {
        return mCurrentCreative != null ? mCurrentCreative.getMediaDuration() : 0;
    }

    public long getSkipOffset() {
        int sscOffset = mAdConfiguration.getVideoSkipOffset();
        if (sscOffset >= 0) {
            return sscOffset;
        }

        return mCurrentCreative != null
               ? mCurrentCreative.getVideoSkipOffset()
               : AdConfiguration.SKIP_OFFSET_NOT_ASSIGNED;
    }

    public void addObstructions(InternalFriendlyObstruction... friendlyObstructions) {
        if (friendlyObstructions == null || friendlyObstructions.length == 0) {
            OXLog.debug(TAG, "addObstructions(): Failed. Obstructions list is empty or null");
            return;
        }

        if (mCurrentCreative == null) {
            OXLog.debug(TAG, "addObstructions(): Failed. Current creative is null.");
            return;
        }

        for (InternalFriendlyObstruction friendlyObstruction : friendlyObstructions) {
            mCurrentCreative.addOmFriendlyObstruction(friendlyObstruction);
        }
    }

    public void show() {
        if (!isCreativeResolved()) {
            OXLog.debug(TAG, "Couldn't proceed show(): Video or HTML is not resolved.");
            return;
        }
        AbstractCreative creative = mTransactionManager.getCurrentCreative();
        if (creative == null) {
            OXLog.error(TAG, "Show called with no ad");
            return;
        }
        // Display current creative
        mCurrentCreative = creative;

        mCurrentCreative.setCreativeViewListener(this);
        handleCreativeDisplay();
    }

    public void loadBidTransaction(AdConfiguration adConfiguration, BidResponse bidResponse) {
        mAdConfiguration = adConfiguration;
        resetTransactionState();
        mTransactionManager.fetchBidTransaction(adConfiguration, bidResponse);
    }

    public void loadVideoTransaction(AdConfiguration adConfiguration, String vastXml) {
        mAdConfiguration = adConfiguration;
        resetTransactionState();
        mTransactionManager.fetchVideoTransaction(adConfiguration, vastXml);
    }

    private void handleVideoCreativeComplete(AbstractCreative creative) {
        Transaction transaction = mTransactionManager.getCurrentTransaction();
        boolean isBuiltInVideo = creative.isBuiltInVideo();
        closeInterstitial();

        if (mTransactionManager.hasNextCreative() && mAdView != null) {

            mTransactionManager.incrementCreativesCounter();

            // Assuming the next creative is an HTMLCreative
            HTMLCreative endCardCreative = (HTMLCreative) transaction.getCreativeFactories().get(1).getCreative();
            if (isBuiltInVideo) {
                mInterstitialManager.displayVideoAdViewInInterstitial(mContextReference.get(), mAdView);
            }
            else {
                mInterstitialManager.setInterstitialDisplayDelegate(endCardCreative);
                mInterstitialManager.displayAdViewInInterstitial(mContextReference.get(), mAdView);
            }
        }
        mAdViewListener.videoCreativePlaybackFinished();
    }

    private void handleCreativeDisplay() {
        View creativeView = mCurrentCreative.getCreativeView();
        if (creativeView == null) {
            OXLog.error(TAG, "Creative has no view");
            return;
        }

        if (mAdConfiguration.getAdUnitIdentifierType() == AdConfiguration.AdUnitIdentifierType.BANNER) {
            if (!mCurrentCreative.equals(mLastCreativeShown)) {
                displayCreative(creativeView);
            }
            mLastCreativeShown = mCurrentCreative;
            return;
        }
        displayCreative(creativeView);
    }

    private void displayCreative(View creativeView) {
        mCurrentCreative.display();
        mAdViewListener.viewReadyForImmediateDisplay(creativeView);
    }

    private boolean isCreativeResolved() {
        if (mCurrentCreative != null && !mCurrentCreative.isResolved()) {
            mAdViewListener.failedToLoad(new AdException(AdException.INTERNAL_ERROR, "Creative has not been resolved yet"));
            return false;
        }
        return true;
    }

    // TODO: 13.08.2020 Remove casts
    private void closeInterstitial() {
        if (mAdView instanceof InterstitialView) {
            ((InterstitialView) mAdView).closeInterstitialVideo();
        }
    }

    private void handleAutoDisplay() {
        // This should cover all cases. If we don't have a delegate we're in a bad state.
        // If an ad is displaying or we are not to autodisplay, don't show. Otherwise do.
        if (mAdViewListener != null && (mCurrentCreative != null && isAutoDisplayOnLoad())) {
            show();
        }
        else {
            OXLog.info(TAG, "AdViewManager - Ad will be displayed when show is called");
        }
    }

    private void addHtmlInterstitialObstructions(ViewGroup rootViewGroup) {
        if (rootViewGroup == null) {
            OXLog.debug(TAG, "addHtmlInterstitialObstructions(): rootViewGroup is null.");
            return;
        }
        View closeButtonView = rootViewGroup.findViewById(R.id.iv_close_interstitial);
        addObstructions(new InternalFriendlyObstruction(closeButtonView, InternalFriendlyObstruction.Purpose.CLOSE_AD, null));
    }

    private void processTransaction(Transaction transaction) {
        List<CreativeFactory> creativeFactories = transaction.getCreativeFactories();
        if (!creativeFactories.isEmpty()) {
            mCurrentCreative = creativeFactories.get(0).getCreative();
            if (!mAdConfiguration.isNative()) {
                mCurrentCreative.createOmAdSession();
            }
        }
        try {
            final AdDetails adDetails = new AdDetails();
            adDetails.setTransactionId(transaction.getTransactionState());
            mAdViewListener.adLoaded(adDetails);
            trackAdLoaded();
        }
        catch (Exception e) {
            OXLog.error(TAG, "adLoaded failed: " + Log.getStackTraceString(e));
        }

        handleAutoDisplay();
    }

    private void trackAdLoaded() {
        if (mCurrentCreative != null) {
            mCurrentCreative.trackAdLoaded();
        }
    }

    public interface AdViewManagerInterstitialDelegate {
        void showInterstitial();
    }
}
