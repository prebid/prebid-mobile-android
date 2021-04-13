package org.prebid.mobile.rendering.bidding.parallel;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.LOADING;
import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_FOR_LOAD;
import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM;
import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_OXB;

public abstract class BaseInterstitialAdUnit {
    private static final String TAG = BaseInterstitialAdUnit.class.getSimpleName();

    private final WeakReference<Context> mWeakContext;
    protected AdConfiguration mAdUnitConfig;

    private BidLoader mBidLoader;
    private BidResponse mBidResponse;

    private InterstitialController mInterstitialController;

    private InterstitialAdUnitState mInterstitialAdUnitState = READY_FOR_LOAD;

    private final BidRequesterListener mBidRequesterListener = new BidRequesterListener() {
        @Override
        public void onFetchCompleted(BidResponse response) {
            mBidResponse = response;

            changeOxbInterstitialAdUnitState(LOADING);
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
            changeOxbInterstitialAdUnitState(READY_TO_DISPLAY_OXB);
            notifyAdEventListener(AdListenerEvent.AD_LOADED);
        }

        @Override
        public void onInterstitialClicked() {
            notifyAdEventListener(AdListenerEvent.AD_CLICKED);
        }

        @Override
        public void onInterstitialFailedToLoad(AdException exception) {
            changeOxbInterstitialAdUnitState(READY_FOR_LOAD);
            notifyErrorListener(exception);
        }

        @Override
        public void onInterstitialDisplayed() {
            changeOxbInterstitialAdUnitState(READY_FOR_LOAD);
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
            OXLog.error(TAG, "loadAd: Failed. BidLoader is not initialized.");
            return;
        }

        if (!isAdLoadAllowed()) {
            OXLog.debug(TAG, "loadAd: Skipped. OXBInterstitialAdUnitState is: " + mInterstitialAdUnitState);
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
            OXLog.debug(TAG, "show(): Ad is not yet ready for display!");
            return;
        }

        switch (mInterstitialAdUnitState) {
            case READY_TO_DISPLAY_GAM:
                showGamAd();
                break;
            case READY_TO_DISPLAY_OXB:
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

    protected void init(AdConfiguration adUnitConfiguration) {
        mAdUnitConfig = adUnitConfiguration;
        mAdUnitConfig.setAdPosition(AdPosition.FULLSCREEN);

        initPrebidRenderingSdk();
        initBidLoader();
        initInterstitialController();
    }

    protected void loadOxbAd() {
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

    protected void changeOxbInterstitialAdUnitState(InterstitialAdUnitState state) {
        mInterstitialAdUnitState = state;
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
    }

    private void initInterstitialController() {
        try {
            mInterstitialController = new InterstitialController(getContext(), mControllerListener);
        }
        catch (AdException e) {
            notifyErrorListener(e);
        }
    }

    private Bid getWinnerBid() {
        return mBidResponse != null ? mBidResponse.getWinningBid() : null;
    }

    private boolean isAuctionWinnerReadyToDisplay() {
        return mInterstitialAdUnitState == READY_TO_DISPLAY_OXB
               || mInterstitialAdUnitState == READY_TO_DISPLAY_GAM;
    }

    private boolean isAdLoadAllowed() {
        return mInterstitialAdUnitState == READY_FOR_LOAD;
    }

    @VisibleForTesting
    final InterstitialAdUnitState getAdUnitState() {
        return mInterstitialAdUnitState;
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
        OXB_LOADING,
        READY_TO_DISPLAY_GAM,
        READY_TO_DISPLAY_OXB
    }
}
