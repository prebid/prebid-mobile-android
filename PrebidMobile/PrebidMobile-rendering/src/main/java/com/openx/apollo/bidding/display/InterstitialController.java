package com.openx.apollo.bidding.display;

import android.content.Context;

import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.bidding.interfaces.InterstitialControllerListener;
import com.openx.apollo.bidding.interfaces.InterstitialViewListener;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.AdDetails;
import com.openx.apollo.networking.WinNotifier;
import com.openx.apollo.utils.logger.OXLog;

public class InterstitialController {
    private static final String TAG = InterstitialController.class.getSimpleName();

    private final InterstitialView mBidInterstitialView;
    private final InterstitialControllerListener mListener;
    private AdConfiguration.AdUnitIdentifierType mAdUnitIdentifierType;

    private final InterstitialViewListener mInterstitialViewListener = new InterstitialViewListener() {
        @Override
        public void onAdLoaded(InterstitialView interstitialView, AdDetails adDetails) {
            OXLog.debug(TAG, "onAdLoaded");
            if (mListener != null) {
                mListener.onInterstitialReadyForDisplay();
            }
        }

        @Override
        public void onAdFailed(InterstitialView interstitialView, AdException error) {
            OXLog.debug(TAG, "onAdFailed");
            if (mListener != null) {
                mListener.onInterstitialFailedToLoad(error);
            }
        }

        @Override
        public void onAdDisplayed(InterstitialView interstitialView) {
            OXLog.debug(TAG, "onAdDisplayed");
            if (mListener != null) {
                mListener.onInterstitialDisplayed();
            }
        }

        @Override
        public void onAdCompleted(InterstitialView interstitialView) {
        }

        @Override
        public void onAdClicked(InterstitialView interstitialView) {
            OXLog.debug(TAG, "onAdClicked");
            if (mListener != null) {
                mListener.onInterstitialClicked();
            }
        }

        @Override
        public void onAdClickThroughClosed(InterstitialView interstitialView) {

        }

        @Override
        public void onAdClosed(InterstitialView interstitialView) {
            OXLog.debug(TAG, "onAdClosed");
            if (mListener != null) {
                mListener.onInterstitialClosed();
            }
        }
    };

    public InterstitialController(Context context, InterstitialControllerListener listener)
    throws AdException {
        mListener = listener;
        mBidInterstitialView = new InterstitialView(context);
        mBidInterstitialView.setInterstitialViewListener(mInterstitialViewListener);
        mBidInterstitialView.setPubBackGroundOpacity(1.0f);
    }

    public void loadAd(AdConfiguration adUnitConfiguration, BidResponse bidResponse) {
        WinNotifier winNotifier = new WinNotifier();
        winNotifier.notifyWin(bidResponse, () -> {
            mAdUnitIdentifierType = bidResponse.isVideo()
                                    ? AdConfiguration.AdUnitIdentifierType.VAST
                                    : AdConfiguration.AdUnitIdentifierType.INTERSTITIAL;
            adUnitConfiguration.setAdUnitIdentifierType(mAdUnitIdentifierType);
            mBidInterstitialView.loadAd(adUnitConfiguration, bidResponse);
        });
    }

    public void loadAd(String responseId, boolean isRewarded) {
        BidResponse bidResponse = BidResponseCache.getInstance().popBidResponse(responseId);
        if (bidResponse == null) {
            if (mListener != null) {
                mListener.onInterstitialFailedToLoad(new AdException(AdException.INTERNAL_ERROR, "No bid response found in the cache"));
            }
            return;
        }
        AdConfiguration adUnitConfiguration = new AdConfiguration();
        adUnitConfiguration.setRewarded(isRewarded);
        loadAd(adUnitConfiguration, bidResponse);
    }

    public void show() {
        if (mAdUnitIdentifierType == null) {
            OXLog.error(TAG, "show: Failed. AdUnitIdentifierType is not defined!");
            return;
        }

        switch (mAdUnitIdentifierType) {
            case INTERSTITIAL:
                mBidInterstitialView.showAsInterstitialFromRoot();
                break;
            case VAST:
                mBidInterstitialView.showVideoAsInterstitial();
                break;
            default:
                OXLog.error(TAG, "show: Failed. Did you specify correct AdUnitConfigurationType? "
                                 + "Supported types: VAST, INTERSTITIAL. "
                                 + "Provided type: " + mAdUnitIdentifierType);
        }
    }

    public void destroy() {
        mBidInterstitialView.destroy();
    }
}
