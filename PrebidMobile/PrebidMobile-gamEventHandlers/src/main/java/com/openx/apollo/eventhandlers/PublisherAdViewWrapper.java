package com.openx.apollo.eventhandlers;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.openx.apollo.bidding.data.AdSize;
import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.display.ReflectionUtils;
import com.openx.apollo.utils.logger.OXLog;

import java.util.HashMap;
import java.util.Map;

import static com.openx.apollo.eventhandlers.global.Constants.APP_EVENT;

/**
 * This class is responsible for wrapping usage of PublisherAdView from GAM SDK.
 * To achieve safe integration between various GAM SDK versions we have to wrap all PublisherAdView method execution in try / catch.
 * This class instance should be created via newInstance method, which will catch any potential exception on PublisherAdView / PublisherAdViewWrapper instance creation
 */
public class PublisherAdViewWrapper extends AdListener implements AppEventListener {

    private static final String TAG = PublisherAdViewWrapper.class.getSimpleName();

    private final PublisherAdView mPublisherAdView;
    private final GamAdEventListener mListener;

    private PublisherAdViewWrapper(Context context, String gamAdUnit,
                                   GamAdEventListener eventListener, AdSize... adSizes) {
        mListener = eventListener;

        mPublisherAdView = new PublisherAdView(context);
        mPublisherAdView.setAdSizes(mapToGamAdSizes(adSizes));
        mPublisherAdView.setAdUnitId(gamAdUnit);
        mPublisherAdView.setAdListener(this);
        mPublisherAdView.setAppEventListener(this);
    }

    @Nullable
    static PublisherAdViewWrapper newInstance(Context context, String gamAdUnitId,
                                                     GamAdEventListener eventListener, AdSize... adSizes) {
        try {
            return new PublisherAdViewWrapper(context,
                                              gamAdUnitId,
                                              eventListener,
                                              adSizes);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
        return null;
    }

    //region ==================== GAM AppEventsListener Implementation
    @Override
    public void onAppEvent(String name, String info) {
        if (APP_EVENT.equals(name)) {
            mListener.onEvent(AdEvent.APP_EVENT_RECEIVED);
        }
    }
    //endregion ==================== GAM AppEventsListener Implementation

    //region ==================== GAM AdEventListener Implementation
    @Override
    public void onAdClosed() {
        mListener.onEvent(AdEvent.CLOSED);
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        final AdEvent adEvent = AdEvent.FAILED;
        adEvent.setErrorCode(errorCode);

        mListener.onEvent(adEvent);
    }

    @Override
    public void onAdOpened() {
        mListener.onEvent(AdEvent.CLICKED);
    }

    @Override
    public void onAdLoaded() {
        mListener.onEvent(AdEvent.LOADED);
    }
    //endregion ==================== GAM AdEventListener Implementation

    public void loadAd(Bid bid) {
        try {
            PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();

            if (bid != null) {
                Map<String, String> targetingMap = new HashMap<>(bid.getPrebid().getTargeting());
                ReflectionUtils.handleGamCustomTargetingUpdate(adRequest, targetingMap);
            }

            mPublisherAdView.loadAd(adRequest);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void setManualImpressionsEnabled(boolean enabled) {
        try {
            mPublisherAdView.setManualImpressionsEnabled(enabled);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void recordManualImpression() {
        try {
            mPublisherAdView.recordManualImpression();
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void destroy() {
        try {
            mPublisherAdView.destroy();
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public View getView() {
        return mPublisherAdView;
    }

    private com.google.android.gms.ads.AdSize[] mapToGamAdSizes(AdSize[] adSizes) {
        if (adSizes == null) {
            return new com.google.android.gms.ads.AdSize[0];
        }

        final com.google.android.gms.ads.AdSize[] gamAdSizeArray = new com.google.android.gms.ads.AdSize[adSizes.length];
        for (int i = 0; i < adSizes.length; i++) {
            final AdSize apolloAdSize = adSizes[i];
            gamAdSizeArray[i] = new com.google.android.gms.ads.AdSize(apolloAdSize.width, apolloAdSize.height);
        }

        return gamAdSizeArray;
    }
}
