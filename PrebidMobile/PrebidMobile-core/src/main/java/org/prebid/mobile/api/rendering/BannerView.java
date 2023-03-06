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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.AdSize;
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.DataObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.BannerAdPosition;
import org.prebid.mobile.api.data.VideoPlacementType;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.listeners.BannerViewListener;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.core.R;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.BannerEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.StandaloneBannerEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.models.PlacementType;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;
import org.prebid.mobile.rendering.utils.helpers.VisibilityChecker;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class BannerView extends FrameLayout {

    private final static String TAG = BannerView.class.getSimpleName();

    private final AdUnitConfiguration adUnitConfig = new AdUnitConfiguration();
    private BannerEventHandler eventHandler;

    private String configId;

    private DisplayView displayView;
    private BidLoader bidLoader;
    private BidResponse bidResponse;

    private final ScreenStateReceiver screenStateReceiver = new ScreenStateReceiver();

    @Nullable private BannerViewListener bannerViewListener;

    private int refreshIntervalSec = 0;

    private boolean isPrimaryAdServerRequestInProgress;
    private boolean adFailed;

    private String nativeStylesCreative = null;

    //region ==================== Listener implementation
    private final DisplayViewListener displayViewListener = new DisplayViewListener() {
        @Override
        public void onAdLoaded() {
            if (bannerViewListener != null) {
                bannerViewListener.onAdLoaded(BannerView.this);
            }
        }

        @Override
        public void onAdDisplayed() {
            if (bannerViewListener != null) {
                bannerViewListener.onAdDisplayed(BannerView.this);
                eventHandler.trackImpression();
            }
        }

        @Override
        public void onAdFailed(AdException exception) {
            if (bannerViewListener != null) {
                bannerViewListener.onAdFailed(BannerView.this, exception);
            }
        }

        @Override
        public void onAdClicked() {
            if (bannerViewListener != null) {
                bannerViewListener.onAdClicked(BannerView.this);
            }
        }

        @Override
        public void onAdClosed() {
            if (bannerViewListener != null) {
                bannerViewListener.onAdClosed(BannerView.this);
            }
        }
    };

    private final BidRequesterListener bidRequesterListener = new BidRequesterListener() {
        @Override
        public void onFetchCompleted(BidResponse response) {
            bidResponse = response;

            isPrimaryAdServerRequestInProgress = true;
            eventHandler.requestAdWithBid(getWinnerBid());
        }

        @Override
        public void onError(AdException exception) {
            bidResponse = null;
            eventHandler.requestAdWithBid(null);
        }
    };

    private final BannerEventListener bannerEventListener = new BannerEventListener() {
        @Override
        public void onPrebidSdkWin() {
            markPrimaryAdRequestFinished();

            if (isBidInvalid()) {
                notifyErrorListener(new AdException(
                        AdException.INTERNAL_ERROR,
                        "WinnerBid is null when executing onPrebidSdkWin."
                ));
                return;
            }

            displayPrebidView();
        }

        @Override
        public void onAdServerWin(View view) {
            markPrimaryAdRequestFinished();

            notifyAdLoadedListener();
            displayAdServerView(view);
        }

        @Override
        public void onAdFailed(AdException exception) {
            markPrimaryAdRequestFinished();

            if (isBidInvalid()) {
                notifyErrorListener(exception);
                return;
            }

            onPrebidSdkWin();
        }

        @Override
        public void onAdClicked() {
            if (bannerViewListener != null) {
                bannerViewListener.onAdClicked(BannerView.this);
            }
        }

        @Override
        public void onAdClosed() {
            if (bannerViewListener != null) {
                bannerViewListener.onAdClosed(BannerView.this);
            }
        }
    };
    //endregion ==================== Listener implementation

    /**
     * Instantiates an BannerView with the ad details as an attribute.
     *
     * @param attrs includes:
     *              <p>
     *              adUnitID
     *              refreshIntervalInSec
     */
    public BannerView(
        @NonNull
            Context context,
        @Nullable
            AttributeSet attrs
    ) {
        super(context, attrs);

        eventHandler = new StandaloneBannerEventHandler();
        reflectAttrs(attrs);
        init();
    }

    /**
     * Instantiates an BannerView for the given configId and adSize.
     */
    public BannerView(
        Context context,
        String configId,
        AdSize size
    ) {
        super(context);
        eventHandler = new StandaloneBannerEventHandler();
        this.configId = configId;
        adUnitConfig.addSize(size);

        init();
    }

    /**
     * Instantiates an BannerView for GAM prebid integration.
     */
    public BannerView(
        Context context,
        String configId,
        @NonNull
            BannerEventHandler eventHandler
    ) {
        super(context);
        this.eventHandler = eventHandler;
        this.configId = configId;

        init();
    }

    /**
     * Executes ad loading if no request is running.
     */
    public void loadAd() {
        if (bidLoader == null) {
            LogUtil.error(TAG, "loadAd: Failed. BidLoader is not initialized.");
            return;
        }

        if (isPrimaryAdServerRequestInProgress) {
            LogUtil.debug(TAG, "loadAd: Skipped. Loading is in progress.");
            return;
        }

        bidLoader.load();
    }

    /**
     * Cancels BidLoader refresh timer.
     */
    public void stopRefresh() {
        if (bidLoader != null) {
            bidLoader.cancelRefresh();
        }
    }

    /**
     * Cleans up resources when destroyed.
     */
    public void destroy() {
        if (eventHandler != null) {
            eventHandler.destroy();
        }
        if (bidLoader != null) {
            bidLoader.destroy();
        }
        if (displayView != null) {
            displayView.destroy();
        }

        screenStateReceiver.unregister();
    }

    //region ==================== getters and setters
    public void setAutoRefreshDelay(int seconds) {
        if (!adUnitConfig.isAdType(AdFormat.BANNER)) {
            LogUtil.info(TAG, "Autorefresh is available only for Banner ad type");
            return;
        }
        if (seconds < 0) {
            LogUtil.error(TAG, "setRefreshIntervalInSec: Failed. Refresh interval must be >= 0");
            return;
        }
        adUnitConfig.setAutoRefreshDelay(seconds);
    }

    public int getAutoRefreshDelayInMs() {
        return adUnitConfig.getAutoRefreshDelay();
    }

    public void addAdditionalSizes(AdSize... sizes) {
        adUnitConfig.addSizes(sizes);
    }

    public Set<AdSize> getAdditionalSizes() {
        return adUnitConfig.getSizes();
    }

    public void setBannerListener(BannerViewListener bannerListener) {
        bannerViewListener = bannerListener;
    }

    public void setVideoPlacementType(VideoPlacementType videoPlacement) {
        adUnitConfig.setAdFormat(AdFormat.VAST);

        final PlacementType placementType = VideoPlacementType.mapToPlacementType(videoPlacement);
        adUnitConfig.setPlacementType(placementType);
    }

    @Nullable
    public VideoPlacementType getVideoPlacementType() {
        return VideoPlacementType.mapToVideoPlacementType(adUnitConfig.getPlacementTypeValue());
    }

    /**
     * Sets BannerEventHandler for GAM prebid integration
     *
     * @param eventHandler instance of GamBannerEventHandler
     */
    public void setEventHandler(BannerEventHandler eventHandler) {
        this.eventHandler = eventHandler;
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


    public void setAdPosition(BannerAdPosition bannerAdPosition) {
        final AdPosition adPosition = BannerAdPosition.mapToAdPosition(bannerAdPosition);
        adUnitConfig.setAdPosition(adPosition);
    }

    public BannerAdPosition getAdPosition() {
        return BannerAdPosition.mapToDisplayAdPosition(adUnitConfig.getAdPositionValue());
    }

    public void setPbAdSlot(String adSlot) {
        adUnitConfig.setPbAdSlot(adSlot);
    }

    @Nullable
    public String getPbAdSlot() {
        return adUnitConfig.getPbAdSlot();
    }

    @Deprecated
    public void addContent(ContentObject content) {
        adUnitConfig.setAppContent(content);
    }

    public void setAppContent(ContentObject content) {
        adUnitConfig.setAppContent(content);
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

    //endregion ==================== getters and setters

    private void reflectAttrs(AttributeSet attrs) {
        if (attrs == null) {
            LogUtil.debug(TAG, "reflectAttrs. No attributes provided.");
            return;
        }
        TypedArray typedArray = getContext()
            .getTheme()
            .obtainStyledAttributes(attrs, R.styleable.BannerView, 0, 0);
        try {
            configId = typedArray.getString(R.styleable.BannerView_configId);
            refreshIntervalSec = typedArray.getInt(R.styleable.BannerView_refreshIntervalSec, 0);
            int width = typedArray.getInt(R.styleable.BannerView_adWidth, -1);
            int height = typedArray.getInt(R.styleable.BannerView_adHeight, -1);
            if (width >= 0 && height >= 0) {
                adUnitConfig.addSize(new AdSize(width, height));
            }
        } finally {
            typedArray.recycle();
        }
    }

    private void init() {
        initPrebidRenderingSdk();
        initAdConfiguration();
        initBidLoader();
        screenStateReceiver.register(getContext());
    }

    private void initPrebidRenderingSdk() {
        PrebidMobile.initializeSdk(getContext(), null);
    }

    private void initBidLoader() {
        bidLoader = new BidLoader(getContext(), adUnitConfig, bidRequesterListener);
        final VisibilityTrackerOption visibilityTrackerOption = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);
        final VisibilityChecker visibilityChecker = new VisibilityChecker(visibilityTrackerOption);

        bidLoader.setBidRefreshListener(() -> {
            if (adFailed) {
                adFailed = false;
                return true;
            }

            final boolean isWindowVisibleToUser = screenStateReceiver.isScreenOn();
            return visibilityChecker.isVisibleForRefresh(this) && isWindowVisibleToUser;
        });
    }

    private void initAdConfiguration() {
        adUnitConfig.setConfigId(configId);
        adUnitConfig.setAutoRefreshDelay(refreshIntervalSec);
        eventHandler.setBannerEventListener(bannerEventListener);
        adUnitConfig.setAdFormat(AdFormat.BANNER);
        adUnitConfig.addSizes(eventHandler.getAdSizeArray());
    }

    private void displayPrebidView() {
        if (indexOfChild(displayView) != -1) {
            displayView.destroy();
            displayView = null;
        }

        removeAllViews();

        final Pair<Integer, Integer> sizePair = bidResponse.getWinningBidWidthHeightPairDips(getContext());
        displayView = new DisplayView(getContext(), displayViewListener, adUnitConfig, bidResponse);
        addView(displayView, sizePair.first, sizePair.second);
    }

    private void displayAdServerView(View view) {
        removeAllViews();

        if (view == null) {
            notifyErrorListener(new AdException(
                    AdException.INTERNAL_ERROR,
                    "Failed to displayAdServerView. Provided view is null"
            ));
            return;
        }

        Views.removeFromParent(view);
        addView(view);

        if (bannerViewListener != null) {
            bannerViewListener.onAdDisplayed(BannerView.this);
        }
    }

    private void markPrimaryAdRequestFinished() {
        isPrimaryAdServerRequestInProgress = false;
    }

    private void notifyAdLoadedListener() {
        if (bannerViewListener != null) {
            bannerViewListener.onAdLoaded(BannerView.this);
        }
    }

    private void notifyErrorListener(AdException exception) {
        adFailed = true;
        if (bannerViewListener != null) {
            bannerViewListener.onAdFailed(BannerView.this, exception);
        }
    }

    private boolean isBidInvalid() {
        return bidResponse == null || bidResponse.getWinningBid() == null;
    }

    public BidResponse getBidResponse() {
        return bidResponse;
    }

    //region ==================== HelperMethods for Unit Tests. Should be used only in tests
    @VisibleForTesting
    final void setBidResponse(BidResponse response) {
        bidResponse = response;
    }

    @VisibleForTesting
    final Bid getWinnerBid() {
        return bidResponse != null ? bidResponse.getWinningBid() : null;
    }

    @VisibleForTesting
    final boolean isPrimaryAdServerRequestInProgress() {
        return isPrimaryAdServerRequestInProgress;
    }
    //endregion ==================== HelperMethods for Unit Tests
}
