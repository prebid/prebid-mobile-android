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

package org.prebid.mobile.rendering.bidding.parallel;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.*;

public abstract class BaseInterstitialAdUnit {
    private static final String TAG = BaseInterstitialAdUnit.class.getSimpleName();

    private final WeakReference<Context> mWeakContext;
    protected AdUnitConfiguration mAdUnitConfig;

    private BidLoader mBidLoader;
    private BidResponse mBidResponse;

    private InterstitialController mInterstitialController;

    private InterstitialAdUnitState mInterstitialAdUnitState = READY_FOR_LOAD;

    private final BidRequesterListener mBidRequesterListener = new BidRequesterListener() {
        @Override
        public void onFetchCompleted(BidResponse response) {
            mBidResponse = response;

            changeInterstitialAdUnitState(LOADING);
            requestAdWithBid(getWinnerBid());
        }

        @Override
        public void onError(AdException exception) {
            mBidResponse = null;
            requestAdWithBid(null);
        }
    };

    private final InterstitialControllerListener mControllerListener = new InterstitialControllerListener() {
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

    protected BaseInterstitialAdUnit(Context context) {
        mWeakContext = new WeakReference<>(context);
    }

    abstract void requestAdWithBid(
            @Nullable
                    Bid bid);

    abstract void showGamAd();

    abstract void notifyAdEventListener(AdListenerEvent adListenerEvent);

    abstract void notifyErrorListener(AdException exception);

    /**
     * Executes ad loading if no request is running.
     */
    public void loadAd() {
        if (mBidLoader == null) {
            LogUtil.error(TAG, "loadAd: Failed. BidLoader is not initialized.");
            return;
        }

        if (!isAdLoadAllowed()) {
            LogUtil.debug(TAG, "loadAd: Skipped. InterstitialAdUnitState is: " + mInterstitialAdUnitState);
            return;
        }

        mBidLoader.load();
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

        switch (mInterstitialAdUnitState) {
            case READY_TO_DISPLAY_GAM:
                showGamAd();
                break;
            case READY_TO_DISPLAY_PREBID:
                mInterstitialController.show();
                break;
            default:
                notifyErrorListener(new AdException(AdException.INTERNAL_ERROR,
                        "show(): Encountered an invalid mInterstitialAdUnitState - " + mInterstitialAdUnitState));
        }
    }

    /// setters and getters
    public void addContextData(String key, String value) {
        mAdUnitConfig.addContextData(key, value);
    }

    public void updateContextData(String key, Set<String> value) {
        mAdUnitConfig.addContextData(key, value);
    }

    public void removeContextData(String key) {
        mAdUnitConfig.removeContextData(key);
    }

    public void clearContextData() {
        mAdUnitConfig.clearContextData();
    }

    public Map<String, Set<String>> getContextDataDictionary() {
        return mAdUnitConfig.getContextDataDictionary();
    }

    public void addContextKeyword(String keyword) {
        mAdUnitConfig.addContextKeyword(keyword);
    }

    public void addContextKeywords(Set<String> keywords) {
        mAdUnitConfig.addContextKeywords(keywords);
    }

    public void removeContextKeyword(String keyword) {
        mAdUnitConfig.removeContextKeyword(keyword);
    }

    public Set<String> getContextKeywordsSet() {
        return mAdUnitConfig.getContextKeywordsSet();
    }

    public void clearContextKeywords() {
        mAdUnitConfig.clearContextKeywords();
    }

    public void setPbAdSlot(String adSlot) {
        mAdUnitConfig.setPbAdSlot(adSlot);
    }

    @Nullable
    public String getPbAdSlot() {
        return mAdUnitConfig.getPbAdSlot();
    }
    /// setters and getters end region

    /**
     * Cleans up resources when destroyed.
     */
    public void destroy() {
        if (mBidLoader != null) {
            mBidLoader.destroy();
        }
        if (mInterstitialController != null) {
            mInterstitialController.destroy();
        }
    }

    protected void init(AdUnitConfiguration adUnitConfiguration) {
        mAdUnitConfig = adUnitConfiguration;
        mAdUnitConfig.setAdPosition(AdPosition.FULLSCREEN);

        initPrebidRenderingSdk();
        initBidLoader();
        initInterstitialController();
    }

    protected void loadPrebidAd() {
        if (mInterstitialController == null) {
            notifyErrorListener(new AdException(AdException.INTERNAL_ERROR, "InterstitialController is not defined. Unable to process bid."));
            return;
        }

        mInterstitialController.loadAd(mAdUnitConfig, mBidResponse);
    }

    @Nullable
    protected Context getContext() {
        return mWeakContext.get();
    }

    protected boolean isBidInvalid() {
        return mBidResponse == null || mBidResponse.getWinningBid() == null;
    }

    protected void changeInterstitialAdUnitState(InterstitialAdUnitState state) {
        mInterstitialAdUnitState = state;
    }

    private void initPrebidRenderingSdk() {
        PrebidMobile.setApplicationContext(getContext(), () -> {
        });
    }

    private void initBidLoader() {
        mBidLoader = new BidLoader(getContext(), mAdUnitConfig, mBidRequesterListener);
    }

    private void initInterstitialController() {
        try {
            mInterstitialController = new InterstitialController(getContext(), mControllerListener);
        } catch (AdException e) {
            notifyErrorListener(e);
        }
    }

    private Bid getWinnerBid() {
        return mBidResponse != null ? mBidResponse.getWinningBid() : null;
    }

    public BidResponse getBidResponse() {
        return mBidResponse;
    }

    private boolean isAuctionWinnerReadyToDisplay() {
        return mInterstitialAdUnitState == READY_TO_DISPLAY_PREBID
                || mInterstitialAdUnitState == READY_TO_DISPLAY_GAM;
    }

    private boolean isAdLoadAllowed() {
        return mInterstitialAdUnitState == READY_FOR_LOAD;
    }

    @VisibleForTesting
    final InterstitialAdUnitState getAdUnitState() {
        return mInterstitialAdUnitState;
    }

    public void addContent(ContentObject content) {
        mAdUnitConfig.setAppContent(content);
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
