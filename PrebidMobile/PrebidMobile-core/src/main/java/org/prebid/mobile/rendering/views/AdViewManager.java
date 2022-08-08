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
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.InterstitialView;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.core.R;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.listeners.CreativeViewListener;
import org.prebid.mobile.rendering.loading.CreativeFactory;
import org.prebid.mobile.rendering.loading.Transaction;
import org.prebid.mobile.rendering.loading.TransactionManager;
import org.prebid.mobile.rendering.loading.TransactionManagerListener;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.InternalFriendlyObstruction;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.video.VideoAdEvent;
import org.prebid.mobile.rendering.video.VideoCreative;
import org.prebid.mobile.rendering.video.VideoCreativeView;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

import java.lang.ref.WeakReference;
import java.util.List;

public class AdViewManager implements CreativeViewListener, TransactionManagerListener {

    private static final String TAG = AdViewManager.class.getSimpleName();

    private boolean builtInVideoFirstStart = true;

    private final InterstitialManager interstitialManager;

    private AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
    private TransactionManager transactionManager;
    private WeakReference<Context> contextReference;
    private ViewGroup adView;
    private AdViewManagerListener adViewListener;
    private AbstractCreative currentCreative;
    private AbstractCreative lastCreativeShown;

    private AdViewManagerInterstitialDelegate delegate = this::show;

    public AdViewManager(
            Context context,
            AdViewManagerListener adViewListener,
            ViewGroup adView,
            InterstitialManager interstitialManager
    ) throws AdException {

        if (context == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Context is null");
        }

        if (adViewListener == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "AdViewManagerListener is null");
        }

        contextReference = new WeakReference<>(context);
        this.adView = adView;
        this.adViewListener = adViewListener;
        transactionManager = new TransactionManager(context, this, interstitialManager);
        this.interstitialManager = interstitialManager;
        this.interstitialManager.setAdViewManagerInterstitialDelegate(delegate);
    }

    @Override
    public void onFetchingCompleted(Transaction transaction) {
        processTransaction(transaction);
    }

    @Override
    public void onFetchingFailed(AdException exception) {
        LogUtil.error(TAG, "There was an error fetching an ad " + exception.toString());
        adViewListener.failedToLoad(exception);
    }

    @Override
    public void creativeWasClicked(AbstractCreative creative, String url) {
        adViewListener.creativeClicked(url);
    }

    @Override
    public void creativeInterstitialDidClose(AbstractCreative creative) {
        LogUtil.debug(TAG, "creativeInterstitialDidClose");

        Transaction currentTransaction = transactionManager.getCurrentTransaction();
        if (creative.isDisplay() && creative.isEndCard()) {

            // Call ad close event for video tracking event
            currentTransaction.getCreativeFactories().get(0)
                              .getCreative().trackVideoEvent(VideoAdEvent.Event.AD_CLOSE);
        }

        // Transaction is complete
        resetTransactionState();

        adViewListener.creativeInterstitialClosed();
    }

    @Override
    public void creativeDidExpand(AbstractCreative creative) {
        adViewListener.creativeExpanded();
    }

    @Override
    public void creativeDidCollapse(AbstractCreative creative) {
        adViewListener.creativeCollapsed();
    }

    @Override
    public void creativeInterstitialDialogShown(ViewGroup rootViewGroup) {
        addHtmlInterstitialObstructions(rootViewGroup);
    }

    @Override
    public void creativeMuted(AbstractCreative creative) {
        adViewListener.creativeMuted();
    }

    @Override
    public void creativeUnMuted(AbstractCreative creative) {
        adViewListener.creativeUnMuted();
    }

    @Override
    public void creativePaused(AbstractCreative creative) {
        adViewListener.creativePaused();
    }

    @Override
    public void creativeResumed(AbstractCreative creative) {
        adViewListener.creativeResumed();
    }

    @Override
    public void creativeDidComplete(AbstractCreative creative) {
        LogUtil.debug(TAG, "creativeDidComplete");

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

        adViewListener.adCompleted();

        // If banner refresh enabled and another ad is available, show that ad
        if (isAutoDisplayOnLoad() && transactionManager.hasTransaction()) {
            show();
        }
    }

    public void resetTransactionState() {
        hide();
        transactionManager.resetState();
    }

    public void hide() {
        if (currentCreative == null) {
            LogUtil.warning(TAG, "Can not hide a null creative");
            return;
        }

        if (adView != null && adView.indexOfChild(currentCreative.getCreativeView()) != -1) {
            adView.removeView(currentCreative.getCreativeView());
            currentCreative = null;
        }
    }

    public void setAdVisibility(int visibility) {
        if (currentCreative == null) {
            LogUtil.debug(
                    TAG,
                    "setAdVisibility(): Skipping creative window focus notification. currentCreative is null"
            );
            return;
        }

        if (Utils.isScreenVisible(visibility)) {
            //handles resuming of show timer for auid ads & video resume for video ads
            currentCreative.handleAdWindowFocus();
        } else {
            //handles pausing of show timer for auid ads & video pause for video ads
            currentCreative.handleAdWindowNoFocus();
        }
    }

    public AdUnitConfiguration getAdConfiguration() {
        return adConfiguration;
    }

    public boolean isAutoDisplayOnLoad() {
        boolean result = adConfiguration.isAdType(AdFormat.BANNER);
        if (builtInVideoFirstStart) {
            builtInVideoFirstStart = false;
            result = result || adConfiguration.isBuiltInVideo();
        }
        return result;
    }

    public void destroy() {
        if (transactionManager != null) {
            transactionManager.destroy();
        }
        if (interstitialManager != null) {
            interstitialManager.destroy();
        }
        if (currentCreative != null) {
            currentCreative.destroy();
        }
    }

    public void pause() {
        if (currentCreative != null) {
            currentCreative.pause();
        }
    }

    public void resume() {
        if (currentCreative != null) {
            currentCreative.resume();
        }
    }

    public void mute() {
        if (currentCreative != null) {
            currentCreative.mute();
        }
    }

    public void unmute() {
        if (currentCreative != null) {
            currentCreative.unmute();
        }
    }

    public boolean isNotShowingEndCard() {
        return currentCreative != null && (!(currentCreative.isDisplay()) || !currentCreative.isEndCard());
    }

    public boolean hasEndCard() {
        return currentCreative != null && currentCreative.isDisplay();
    }

    public boolean hasNextCreative() {
        return transactionManager.hasNextCreative();
    }

    public void updateAdView(View view) {
        currentCreative.updateAdView(view);
    }

    public void trackVideoStateChange(InternalPlayerState state) {
        currentCreative.trackVideoStateChange(state);
    }

    public boolean canShowFullScreen() {
        return currentCreative != null && currentCreative.isBuiltInVideo();
    }

    public void returnFromVideo(View callingView) {
        if (currentCreative != null && currentCreative.isBuiltInVideo()) {
            View creativeView = currentCreative.getCreativeView();
            if (creativeView instanceof VideoCreativeView && currentCreative.isVideo()) {
                VideoCreativeView videoCreativeView = (VideoCreativeView) creativeView;
                VideoCreative videoCreative = (VideoCreative) currentCreative;
                videoCreativeView.hideCallToAction();
                videoCreativeView.mute();
                videoCreative.updateAdView(callingView);
                videoCreative.onPlayerStateChanged(InternalPlayerState.NORMAL);
            }
        }
    }

    public boolean isPlaying() {
        return currentCreative != null && currentCreative.isPlaying();
    }

    public boolean isInterstitialClosed() {
        return currentCreative != null && currentCreative.isInterstitialClosed();
    }

    public void trackCloseEvent() {
        if (currentCreative != null) {
            currentCreative.trackVideoEvent(VideoAdEvent.Event.AD_CLOSE);
        }
    }

    public long getMediaDuration() {
        return currentCreative != null ? currentCreative.getMediaDuration() : 0;
    }

    public long getSkipOffset() {
        int sscOffset = adConfiguration.getVideoSkipOffset();
        if (sscOffset >= 0) {
            return sscOffset;
        }

        return currentCreative != null ? currentCreative.getVideoSkipOffset() : AdUnitConfiguration.SKIP_OFFSET_NOT_ASSIGNED;
    }

    public void addObstructions(InternalFriendlyObstruction... friendlyObstructions) {
        if (friendlyObstructions == null || friendlyObstructions.length == 0) {
            LogUtil.debug(TAG, "addObstructions(): Failed. Obstructions list is empty or null");
            return;
        }

        if (currentCreative == null) {
            LogUtil.debug(TAG, "addObstructions(): Failed. Current creative is null.");
            return;
        }

        for (InternalFriendlyObstruction friendlyObstruction : friendlyObstructions) {
            currentCreative.addOmFriendlyObstruction(friendlyObstruction);
        }
    }

    public void show() {
        if (!isCreativeResolved()) {
            LogUtil.debug(TAG, "Couldn't proceed show(): Video or HTML is not resolved.");
            return;
        }
        AbstractCreative creative = transactionManager.getCurrentCreative();
        if (creative == null) {
            LogUtil.error(TAG, "Show called with no ad");
            return;
        }
        // Display current creative
        currentCreative = creative;

        currentCreative.setCreativeViewListener(this);
        handleCreativeDisplay();
    }

    public void loadBidTransaction(AdUnitConfiguration adConfiguration, BidResponse bidResponse) {
        this.adConfiguration = adConfiguration;
        resetTransactionState();
        transactionManager.fetchBidTransaction(adConfiguration, bidResponse);
    }

    public void loadVideoTransaction(AdUnitConfiguration adConfiguration, String vastXml) {
        this.adConfiguration = adConfiguration;
        resetTransactionState();
        transactionManager.fetchVideoTransaction(adConfiguration, vastXml);
    }

    private void handleVideoCreativeComplete(AbstractCreative creative) {
        Transaction transaction = transactionManager.getCurrentTransaction();
        boolean isBuiltInVideo = creative.isBuiltInVideo();
        if(hasEndCard())
            closeInterstitial();

        if (transactionManager.hasNextCreative() && adView != null) {

            transactionManager.incrementCreativesCounter();

            // Assuming the next creative is an HTMLCreative
            HTMLCreative endCardCreative = (HTMLCreative) transaction.getCreativeFactories().get(1).getCreative();
            if (isBuiltInVideo) {
                interstitialManager.displayVideoAdViewInInterstitial(contextReference.get(), adView);
            } else {
                interstitialManager.setInterstitialDisplayDelegate(endCardCreative);
                interstitialManager.displayAdViewInInterstitial(contextReference.get(), adView);
            }
        }
        adViewListener.videoCreativePlaybackFinished();
    }

    private void handleCreativeDisplay() {
        View creativeView = currentCreative.getCreativeView();
        if (creativeView == null) {
            LogUtil.error(TAG, "Creative has no view");
            return;
        }

        if (adConfiguration.isAdType(AdFormat.BANNER)) {
            if (!currentCreative.equals(lastCreativeShown)) {
                displayCreative(creativeView);
            }
            lastCreativeShown = currentCreative;
            return;
        }
        displayCreative(creativeView);
    }

    private void displayCreative(View creativeView) {
        currentCreative.display();
        adViewListener.viewReadyForImmediateDisplay(creativeView);
    }

    private boolean isCreativeResolved() {
        if (currentCreative != null && !currentCreative.isResolved()) {
            adViewListener.failedToLoad(new AdException(
                    AdException.INTERNAL_ERROR,
                    "Creative has not been resolved yet"
            ));
            return false;
        }
        return true;
    }

    // TODO: 13.08.2020 Remove casts
    private void closeInterstitial() {
        if (adView instanceof InterstitialView) {
            ((InterstitialView) adView).closeInterstitialVideo();
        }
    }

    private void handleAutoDisplay() {
        // This should cover all cases. If we don't have a delegate we're in a bad state.
        // If an ad is displaying or we are not to autodisplay, don't show. Otherwise do.
        if (adViewListener != null && (currentCreative != null && isAutoDisplayOnLoad())) {
            show();
        } else {
            LogUtil.info(TAG, "AdViewManager - Ad will be displayed when show is called");
        }
    }

    private void addHtmlInterstitialObstructions(ViewGroup rootViewGroup) {
        if (rootViewGroup == null) {
            LogUtil.debug(TAG, "addHtmlInterstitialObstructions(): rootViewGroup is null.");
            return;
        }
        View closeButtonView = rootViewGroup.findViewById(R.id.iv_close_interstitial);

        InternalFriendlyObstruction[] obstructionArray = new InternalFriendlyObstruction[2];
        obstructionArray[0] = new InternalFriendlyObstruction(closeButtonView, InternalFriendlyObstruction.Purpose.CLOSE_AD, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View dialogRoot = closeButtonView.getRootView();
            View navigationBarView = dialogRoot.findViewById(android.R.id.navigationBarBackground);
            InternalFriendlyObstruction obstruction = new InternalFriendlyObstruction(navigationBarView, InternalFriendlyObstruction.Purpose.OTHER, "Bottom navigation bar");
            obstructionArray[1] = obstruction;
        } else {
            obstructionArray[1] = null;
        }

        addObstructions(obstructionArray);
    }

    private void processTransaction(Transaction transaction) {
        List<CreativeFactory> creativeFactories = transaction.getCreativeFactories();
        if (!creativeFactories.isEmpty()) {
            currentCreative = creativeFactories.get(0).getCreative();
            currentCreative.createOmAdSession();
        }
        try {
            final AdDetails adDetails = new AdDetails();
            adDetails.setTransactionId(transaction.getTransactionState());
            adViewListener.adLoaded(adDetails);
            trackAdLoaded();
        }
        catch (Exception e) {
            LogUtil.error(TAG, "adLoaded failed: " + Log.getStackTraceString(e));
        }

        handleAutoDisplay();
    }

    private void trackAdLoaded() {
        if (currentCreative != null) {
            currentCreative.trackAdLoaded();
        }
    }

    public interface AdViewManagerInterstitialDelegate {
        void showInterstitial();
    }
}
