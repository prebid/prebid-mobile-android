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

import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.LOADING;
import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_FOR_LOAD;
import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM;
import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_PREBID;

import android.content.Context;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.ContentObject;
import org.prebid.mobile.DataObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.Position;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.models.AdPosition;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public abstract class BaseInterstitialAdUnit {

    private static final String TAG = BaseInterstitialAdUnit.class.getSimpleName();

    protected AdUnitConfiguration adUnitConfig;

    private BidLoader bidLoader;
    private BidResponse bidResponse;
    private InterstitialController interstitialController;
    private InterstitialAdUnitState interstitialAdUnitState = READY_FOR_LOAD;

    private final WeakReference<Context> weakContext;
    private final BidRequesterListener bidRequesterListener = createBidRequesterListener();
    private final InterstitialControllerListener controllerListener = createInterstitialControllerListener();

    protected BaseInterstitialAdUnit(Context context) {
        weakContext = new WeakReference<>(context);
    }


    abstract void requestAdWithBid(@Nullable Bid bid);

    abstract void showGamAd();

    abstract void notifyAdEventListener(AdListenerEvent adListenerEvent);

    abstract void notifyErrorListener(AdException exception);


    /**
     * Executes ad loading if no request is running.
     */
    public void loadAd() {
        if (bidLoader == null) {
            LogUtil.error(TAG, "loadAd: Failed. BidLoader is not initialized.");
            return;
        }

        if (!isAdLoadAllowed()) {
            LogUtil.debug(TAG, "loadAd: Skipped. InterstitialAdUnitState is: " + interstitialAdUnitState);
            return;
        }

        bidLoader.load();
    }

    /**
     * @return true if auction winner was defined, false otherwise
     */
    public boolean isLoaded() {
        return isAuctionWinnerReadyToDisplay();
    }

    /**
     * Executes interstitial display if auction winner is defined.
     */
    public void show() {
        if (!isAuctionWinnerReadyToDisplay()) {
            LogUtil.debug(TAG, "show(): Ad is not yet ready for display!");
            return;
        }

        switch (interstitialAdUnitState) {
            case READY_TO_DISPLAY_GAM:
                showGamAd();
                break;
            case READY_TO_DISPLAY_PREBID:
                interstitialController.show();
                break;
            default:
                notifyErrorListener(new AdException(
                    AdException.INTERNAL_ERROR,
                    "show(): Encountered an invalid interstitialAdUnitState - " + interstitialAdUnitState
                ));
        }
    }

    /**
     * @deprecated use addExtData
     */
    @Deprecated
    public void addContextData(
        String key,
        String value
    ) {
        adUnitConfig.addExtData(key, value);
    }

    /**
     * @deprecated use updateExtData
     */
    @Deprecated
    public void updateContextData(
        String key,
        Set<String> value
    ) {
        adUnitConfig.addExtData(key, value);
    }

    /**
     * @deprecated use removeExtData
     */
    @Deprecated
    public void removeContextData(String key) {
        adUnitConfig.removeExtData(key);
    }

    /**
     * @deprecated use clearExtData
     */
    @Deprecated
    public void clearContextData() {
        adUnitConfig.clearExtData();
    }

    /**
     * @deprecated use getExtDataDictionary
     */
    @Deprecated
    public Map<String, Set<String>> getContextDataDictionary() {
        return adUnitConfig.getExtDataDictionary();
    }

    /**
     * @deprecated use addExtKeyword
     */
    @Deprecated
    public void addContextKeyword(String keyword) {
        adUnitConfig.addExtKeyword(keyword);
    }

    /**
     * @deprecated use addExtKeywords
     */
    @Deprecated
    public void addContextKeywords(Set<String> keywords) {
        adUnitConfig.addExtKeywords(keywords);
    }

    /**
     * @deprecated use removeExtKeyword
     */
    @Deprecated
    public void removeContextKeyword(String keyword) {
        adUnitConfig.removeExtKeyword(keyword);
    }

    /**
     * @deprecated use getExtKeywordsSet
     */
    @Deprecated
    public Set<String> getContextKeywordsSet() {
        return adUnitConfig.getExtKeywordsSet();
    }

    /**
     * @deprecated use clearExtKeywords
     */
    @Deprecated
    public void clearContextKeywords() {
        adUnitConfig.clearExtKeywords();
    }


    public void addExtData(
        String key,
        String value
    ) {
        adUnitConfig.addExtData(key, value);
    }

    public void updateExtData(
        String key,
        Set<String> value
    ) {
        adUnitConfig.addExtData(key, value);
    }

    public void removeExtData(String key) {
        adUnitConfig.removeExtData(key);
    }

    public void clearExtData() {
        adUnitConfig.clearExtData();
    }

    public Map<String, Set<String>> getExtDataDictionary() {
        return adUnitConfig.getExtDataDictionary();
    }

    public void addExtKeyword(String keyword) {
        adUnitConfig.addExtKeyword(keyword);
    }

    public void addExtKeywords(Set<String> keywords) {
        adUnitConfig.addExtKeywords(keywords);
    }

    public void removeExtKeyword(String keyword) {
        adUnitConfig.removeExtKeyword(keyword);
    }

    public Set<String> getExtKeywordsSet() {
        return adUnitConfig.getExtKeywordsSet();
    }

    public void clearExtKeywords() {
        adUnitConfig.clearExtKeywords();
    }

    public void setAppContent(ContentObject content) {
        adUnitConfig.setAppContent(content);
    }

    public ContentObject getAppContent() {
        return adUnitConfig.getAppContent();
    }

    public void addUserData(DataObject dataObject) {
        adUnitConfig.addUserData(dataObject);
    }

    public ArrayList<DataObject> getUserData() {
        return adUnitConfig.getUserData();
    }

    public void clearUserData() {
        adUnitConfig.clearUserData();
    }


    @Nullable
    public String getPbAdSlot() {
        return adUnitConfig.getPbAdSlot();
    }

    public void setPbAdSlot(String adSlot) {
        adUnitConfig.setPbAdSlot(adSlot);
    }

    /**
     * Sets delay in seconds to show skip or close button.
     */
    public void setSkipDelay(int secondsDelay) {
        adUnitConfig.setSkipDelay(secondsDelay);
    }

    /**
     * Sets skip button percentage size in range from 0.05 to 1.
     * If value less than 0.05, size will be default.
     */
    public void setSkipButtonArea(@FloatRange(from = 0, to = 1.0) double buttonArea) {
        adUnitConfig.setSkipButtonArea(buttonArea);
    }

    /**
     * Sets skip button position on the screen. Suitable values TOP_LEFT and TOP_RIGHT.
     * Default value TOP_RIGHT.
     */
    public void setSkipButtonPosition(Position skipButtonPosition) {
        adUnitConfig.setSkipButtonPosition(skipButtonPosition);
    }

    public void setIsMuted(boolean isMuted) {
        adUnitConfig.setIsMuted(isMuted);
    }

    public void setIsSoundButtonVisible(boolean isSoundButtonVisible) {
        adUnitConfig.setIsSoundButtonVisible(isSoundButtonVisible);
    }

    public void setMaxVideoDuration(int seconds) {
        adUnitConfig.setMaxVideoDuration(seconds);
    }

    /**
     * Sets close button percentage size in range from 0.05 to 1.
     * If value less than 0.05, size will be default.
     */
    public void setCloseButtonArea(@FloatRange(from = 0, to = 1.0) double closeButtonArea) {
        adUnitConfig.setCloseButtonArea(closeButtonArea);
    }

    /**
     * Sets close button position on the screen. Suitable values TOP_LEFT and TOP_RIGHT.
     * Default value TOP_RIGHT.
     */
    public void setCloseButtonPosition(@Nullable Position closeButtonPosition) {
        adUnitConfig.setCloseButtonPosition(closeButtonPosition);
    }

    /**
     * Cleans up resources when destroyed.
     */
    public void destroy() {
        if (bidLoader != null) {
            bidLoader.destroy();
        }
        if (interstitialController != null) {
            interstitialController.destroy();
        }
    }

    protected void init(AdUnitConfiguration adUnitConfiguration) {
        adUnitConfig = adUnitConfiguration;
        adUnitConfig.setAdPosition(AdPosition.FULLSCREEN);

        initPrebidRenderingSdk();
        initBidLoader();
        initInterstitialController();
    }

    protected void loadPrebidAd() {
        if (interstitialController == null) {
            notifyErrorListener(new AdException(
                AdException.INTERNAL_ERROR,
                "InterstitialController is not defined. Unable to process bid."
            ));
            return;
        }

        interstitialController.loadAd(adUnitConfig, bidResponse);
    }

    @Nullable
    protected Context getContext() {
        return weakContext.get();
    }

    protected boolean isBidInvalid() {
        return bidResponse == null || bidResponse.getWinningBid() == null;
    }

    protected void changeInterstitialAdUnitState(InterstitialAdUnitState state) {
        interstitialAdUnitState = state;
    }

    private void initPrebidRenderingSdk() {
        PrebidMobile.initializeSdk(getContext(), null);
    }

    private void initBidLoader() {
        bidLoader = new BidLoader(getContext(), adUnitConfig, bidRequesterListener);
    }

    private void initInterstitialController() {
        try {
            interstitialController = new InterstitialController(getContext(), controllerListener);
        } catch (AdException e) {
            notifyErrorListener(e);
        }
    }

    private Bid getWinnerBid() {
        return bidResponse != null ? bidResponse.getWinningBid() : null;
    }

    public BidResponse getBidResponse() {
        return bidResponse;
    }

    private boolean isAuctionWinnerReadyToDisplay() {
        return interstitialAdUnitState == READY_TO_DISPLAY_PREBID || interstitialAdUnitState == READY_TO_DISPLAY_GAM;
    }

    private boolean isAdLoadAllowed() {
        return interstitialAdUnitState == READY_FOR_LOAD;
    }

    @VisibleForTesting
    final InterstitialAdUnitState getAdUnitState() {
        return interstitialAdUnitState;
    }

    public void addContent(ContentObject content) {
        adUnitConfig.setAppContent(content);
    }


    private BidRequesterListener createBidRequesterListener() {
        return new BidRequesterListener() {
            @Override
            public void onFetchCompleted(BidResponse response) {
                bidResponse = response;

                changeInterstitialAdUnitState(LOADING);
                requestAdWithBid(getWinnerBid());
            }

            @Override
            public void onError(AdException exception) {
                bidResponse = null;
                requestAdWithBid(null);
            }
        };
    }

    private InterstitialControllerListener createInterstitialControllerListener() {
        return new InterstitialControllerListener() {
            @Override
            public void onInterstitialReadyForDisplay() {
                changeInterstitialAdUnitState(READY_TO_DISPLAY_PREBID);
                notifyAdEventListener(AdListenerEvent.AD_LOADED);
            }

            @Override
            public void onInterstitialClicked() {
                notifyAdEventListener(AdListenerEvent.AD_CLICKED);
            }

            @Override
            public void onInterstitialFailedToLoad(AdException exception) {
                changeInterstitialAdUnitState(READY_FOR_LOAD);
                notifyErrorListener(exception);
            }

            @Override
            public void onInterstitialDisplayed() {
                changeInterstitialAdUnitState(READY_FOR_LOAD);
                notifyAdEventListener(AdListenerEvent.AD_DISPLAYED);
            }

            @Override
            public void onInterstitialClosed() {
                notifyAdEventListener(AdListenerEvent.AD_CLOSE);
                notifyAdEventListener(AdListenerEvent.USER_RECEIVED_PREBID_REWARD);
            }
        };
    }


    enum AdListenerEvent {
        AD_CLOSE,
        AD_CLICKED,
        AD_DISPLAYED,
        AD_LOADED,
        USER_RECEIVED_PREBID_REWARD // only for RewardedAdUnit
    }

    enum InterstitialAdUnitState {
        READY_FOR_LOAD,
        LOADING,
        PREBID_LOADING,
        READY_TO_DISPLAY_GAM,
        READY_TO_DISPLAY_PREBID
    }

}
