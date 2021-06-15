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
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;

import org.prebid.mobile.rendering.R;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.DisplayView;
import org.prebid.mobile.rendering.bidding.enums.BannerAdPosition;
import org.prebid.mobile.rendering.bidding.enums.VideoPlacementType;
import org.prebid.mobile.rendering.bidding.interfaces.BannerEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.StandaloneBannerEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener;
import org.prebid.mobile.rendering.bidding.listeners.BannerViewListener;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.models.PlacementType;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;
import org.prebid.mobile.rendering.utils.helpers.VisibilityChecker;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

public class BannerView extends FrameLayout {
    private final static String TAG = BannerView.class.getSimpleName();

    private final AdConfiguration mAdUnitConfig = new AdConfiguration();
    private BannerEventHandler mEventHandler;

    private String mConfigId;

    private DisplayView mDisplayView;
    private BidLoader mBidLoader;
    private BidResponse mBidResponse;

    private final ScreenStateReceiver mScreenStateReceiver = new ScreenStateReceiver();

    @Nullable
    private BannerViewListener mBannerViewListener;

    private int mRefreshIntervalSec = -1;

    private boolean mIsPrimaryAdServerRequestInProgress;
    private boolean mAdFailed;

    //region ==================== Listener implementation
    private final DisplayViewListener mDisplayViewListener = new DisplayViewListener() {
        @Override
        public void onAdLoaded() {
            if (mBannerViewListener != null) {
                mBannerViewListener.onAdLoaded(BannerView.this);
            }
        }

        @Override
        public void onAdDisplayed() {
            if (mBannerViewListener != null) {
                mBannerViewListener.onAdDisplayed(BannerView.this);
                mEventHandler.trackImpression();
            }
        }

        @Override
        public void onAdFailed(AdException exception) {
            if (mBannerViewListener != null) {
                mBannerViewListener.onAdFailed(BannerView.this, exception);
            }
        }

        @Override
        public void onAdClicked() {
            if (mBannerViewListener != null) {
                mBannerViewListener.onAdClicked(BannerView.this);
            }
        }

        @Override
        public void onAdClosed() {
            if (mBannerViewListener != null) {
                mBannerViewListener.onAdClosed(BannerView.this);
            }
        }
    };

    private final BidRequesterListener mBidRequesterListener = new BidRequesterListener() {
        @Override
        public void onFetchCompleted(BidResponse response) {
            mBidResponse = response;

            mIsPrimaryAdServerRequestInProgress = true;
            mEventHandler.requestAdWithBid(getWinnerBid());
        }

        @Override
        public void onError(AdException exception) {
            mBidResponse = null;
            mEventHandler.requestAdWithBid(null);
        }
    };

    private final BannerEventListener mBannerEventListener = new BannerEventListener() {
        @Override
        public void onPrebidSdkWin() {
            markPrimaryAdRequestFinished();

            if (isBidInvalid()) {
                notifyErrorListener(new AdException(AdException.INTERNAL_ERROR, "WinnerBid is null when executing onPrebidSdkWin."));
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
            if (mBannerViewListener != null) {
                mBannerViewListener.onAdClicked(BannerView.this);
            }
        }

        @Override
        public void onAdClosed() {
            if (mBannerViewListener != null) {
                mBannerViewListener.onAdClosed(BannerView.this);
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
            AttributeSet attrs) {
        super(context, attrs);

        mEventHandler = new StandaloneBannerEventHandler();
        reflectAttrs(attrs);
        init();
    }

    /**
     * Instantiates an BannerView for the given configId and adSize.
     */
    public BannerView(Context context, String configId, AdSize size) {
        super(context);
        mEventHandler = new StandaloneBannerEventHandler();
        mConfigId = configId;
        mAdUnitConfig.addSize(size);

        init();
    }

    /**
     * Instantiates an BannerView for GAM prebid integration.
     */
    public BannerView(Context context, String configId,
                      @NonNull
                          BannerEventHandler eventHandler) {
        super(context);
        mEventHandler = eventHandler;
        mConfigId = configId;

        init();
    }

    /**
     * Executes ad loading if no request is running.
     */
    public void loadAd() {
        if (mBidLoader == null) {
            LogUtil.error(TAG, "loadAd: Failed. BidLoader is not initialized.");
            return;
        }

        if (mIsPrimaryAdServerRequestInProgress) {
            LogUtil.debug(TAG, "loadAd: Skipped. Loading is in progress.");
            return;
        }

        boolean isNativePpm = mAdUnitConfig.isNative() && mEventHandler instanceof StandaloneBannerEventHandler;
        NativeAdConfiguration nativeAdConfiguration = mAdUnitConfig.getNativeAdConfiguration();

        if (isNativePpm && TextUtils.isEmpty(nativeAdConfiguration.getNativeStylesCreative())) {
            notifyErrorListener(new AdException(AdException.INVALID_REQUEST, "Failed. PPM native bid request won't be performed without a valid nativeStylesCreative."));
            return;
        }

        mBidLoader.load();
    }

    /**
     * Cancels BidLoader refresh timer.
     */
    public void stopRefresh() {
        if (mBidLoader != null) {
            mBidLoader.cancelRefresh();
        }
    }

    /**
     * Cleans up resources when destroyed.
     */
    public void destroy() {
        if (mEventHandler != null) {
            mEventHandler.destroy();
        }
        if (mBidLoader != null) {
            mBidLoader.destroy();
        }
        if (mDisplayView != null) {
            mDisplayView.destroy();
        }

        mScreenStateReceiver.unregister();
    }

    //region ==================== getters and setters
    public void setAutoRefreshDelay(int seconds) {
        if (!mAdUnitConfig.isAdType(AdConfiguration.AdUnitIdentifierType.BANNER)) {
            LogUtil.info(TAG, "Autorefresh is available only for Banner ad type");
            return;
        }
        if (seconds < 0) {
            LogUtil.error(TAG, "setRefreshIntervalInSec: Failed. Refresh interval must be >= 0");
            return;
        }
        mAdUnitConfig.setAutoRefreshDelay(seconds);
    }

    public int getAutoRefreshDelayInMs() {
        return mAdUnitConfig.getAutoRefreshDelay();
    }

    public void addAdditionalSizes(AdSize... sizes) {
        mAdUnitConfig.addSizes(sizes);
    }

    public Set<AdSize> getAdditionalSizes() {
        return mAdUnitConfig.getAdSizes();
    }

    public void setBannerListener(BannerViewListener bannerListener) {
        mBannerViewListener = bannerListener;
    }

    public void setVideoPlacementType(VideoPlacementType videoPlacement) {
        mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.VAST);

        final PlacementType placementType = VideoPlacementType.mapToPlacementType(videoPlacement);
        mAdUnitConfig.setPlacementType(placementType);
    }

    @Nullable
    public VideoPlacementType getVideoPlacementType() {
        return VideoPlacementType.mapToVideoPlacementType(mAdUnitConfig.getPlacementTypeValue());
    }

    /**
     * Sets NativeAdConfiguration and enables Native Ad requests
     *
     * @param configuration - configured NativeAdConfiguration class
     */
    public void setNativeAdConfiguration(NativeAdConfiguration configuration) {
        mAdUnitConfig.setNativeAdConfiguration(configuration);
    }

    /**
     * Sets BannerEventHandler for GAM prebid integration
     *
     * @param eventHandler instance of GamBannerEventHandler
     */
    public void setEventHandler(BannerEventHandler eventHandler) {
        mEventHandler = eventHandler;
    }

    public void addContextData(String key, String value) {
        mAdUnitConfig.addContextData(key, value);
    }

    public void updateContextData(String key, Set<String> value) {
        mAdUnitConfig.updateContextData(key, value);
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

    public void setAdPosition(BannerAdPosition bannerAdPosition) {
        final AdPosition adPosition = BannerAdPosition.mapToAdPosition(bannerAdPosition);
        mAdUnitConfig.setAdPosition(adPosition);
    }

    public BannerAdPosition getAdPosition() {
        return BannerAdPosition.mapToDisplayAdPosition(mAdUnitConfig.getAdPositionValue());
    }

    public void setPbAdSlot(String adSlot) {
        mAdUnitConfig.setPbAdSlot(adSlot);
    }

    @Nullable
    public String getPbAdSlot() {
        return mAdUnitConfig.getPbAdSlot();
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
            mConfigId = typedArray.getString(R.styleable.BannerView_configId);
            mRefreshIntervalSec = typedArray.getInt(R.styleable.BannerView_refreshIntervalSec, 0);
            int width = typedArray.getInt(R.styleable.BannerView_adWidth, -1);
            int height = typedArray.getInt(R.styleable.BannerView_adHeight, -1);
            if (width >= 0 && height >= 0) {
                mAdUnitConfig.addSize(new AdSize(width, height));
            }
        }
        finally {
            typedArray.recycle();
        }
    }

    private void init() {
        initPrebidRenderingSdk();
        initAdConfiguration();
        initBidLoader();
        mScreenStateReceiver.register(getContext());
    }

    private void initPrebidRenderingSdk() {
        try {
            PrebidRenderingSettings.initializeSDK(getContext(), () -> { });
        }
        catch (AdException e) {
            e.printStackTrace();
        }
    }

    private void initBidLoader() {
        mBidLoader = new BidLoader(getContext(), mAdUnitConfig, mBidRequesterListener);
        final VisibilityTrackerOption visibilityTrackerOption = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);
        final VisibilityChecker visibilityChecker = new VisibilityChecker(visibilityTrackerOption);

        mBidLoader.setBidRefreshListener(() -> {
            if (mAdFailed) {
                mAdFailed = false;
                return true;
            }

            final boolean isWindowVisibleToUser = mScreenStateReceiver.isScreenOn();
            return visibilityChecker.isVisibleForRefresh(this) && isWindowVisibleToUser;
        });
    }

    private void initAdConfiguration() {
        mAdUnitConfig.setConfigId(mConfigId);
        mAdUnitConfig.setAutoRefreshDelay(mRefreshIntervalSec);
        mEventHandler.setBannerEventListener(mBannerEventListener);
        mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
        mAdUnitConfig.addSizes(mEventHandler.getAdSizeArray());
    }

    private void displayPrebidView() {
        if (indexOfChild(mDisplayView) != -1) {
            mDisplayView.destroy();
            mDisplayView = null;
        }

        removeAllViews();

        final Pair<Integer, Integer> sizePair = mBidResponse.getWinningBidWidthHeightPairDips(getContext());
        mDisplayView = new DisplayView(getContext(), mDisplayViewListener, mAdUnitConfig, mBidResponse);
        addView(mDisplayView, sizePair.first, sizePair.second);
    }

    private void displayAdServerView(View view) {
        removeAllViews();

        if (view == null) {
            notifyErrorListener(new AdException(AdException.INTERNAL_ERROR, "Failed to displayAdServerView. Provided view is null"));
            return;
        }

        Views.removeFromParent(view);
        addView(view);

        if (mBannerViewListener != null) {
            mBannerViewListener.onAdDisplayed(BannerView.this);
        }
    }

    private void markPrimaryAdRequestFinished() {
        mIsPrimaryAdServerRequestInProgress = false;
    }

    private void notifyAdLoadedListener() {
        if (mBannerViewListener != null) {
            mBannerViewListener.onAdLoaded(BannerView.this);
        }
    }

    private void notifyErrorListener(AdException exception) {
        mAdFailed = true;
        if (mBannerViewListener != null) {
            mBannerViewListener.onAdFailed(BannerView.this, exception);
        }
    }

    private boolean isBidInvalid() {
        return mBidResponse == null || mBidResponse.getWinningBid() == null;
    }

    //region ==================== HelperMethods for Unit Tests. Should be used only in tests
    @VisibleForTesting
    final void setBidResponse(BidResponse response) {
        mBidResponse = response;
    }

    @VisibleForTesting
    final Bid getWinnerBid() {
        return mBidResponse != null ? mBidResponse.getWinningBid() : null;
    }

    @VisibleForTesting
    final boolean isPrimaryAdServerRequestInProgress() {
        return mIsPrimaryAdServerRequestInProgress;
    }
    //endregion ==================== HelperMethods for Unit Tests
}
