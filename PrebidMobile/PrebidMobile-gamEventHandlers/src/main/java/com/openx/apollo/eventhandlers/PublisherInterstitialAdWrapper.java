package com.openx.apollo.eventhandlers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.display.ReflectionUtils;
import com.openx.apollo.utils.logger.OXLog;

import java.util.HashMap;
import java.util.Map;

import static com.openx.apollo.eventhandlers.global.Constants.APP_EVENT;

/**
 * This class is responsible for wrapping usage of PublisherInterstitialAd from GAM SDK.
 * To achieve safe integration between various GAM SDK versions we have to wrap all PublisherAdView method execution in try / catch.
 * This class instance should be created via newInstance method, which will catch any potential exception on PublisherInterstitialAd / PublisherInterstitialAdWrapper instance creation
 */
public class PublisherInterstitialAdWrapper extends AdListener implements AppEventListener {

    private static final String TAG = PublisherInterstitialAdWrapper.class.getSimpleName();

    private final PublisherInterstitialAd mRequestInterstitial;
    private final GamAdEventListener mListener;

    private PublisherInterstitialAdWrapper(Context context, String gamAdUnitId, GamAdEventListener eventListener) {
        mListener = eventListener;

        mRequestInterstitial = new PublisherInterstitialAd(context.getApplicationContext());
        mRequestInterstitial.setAdUnitId(gamAdUnitId);
        mRequestInterstitial.setAdListener(this);
        mRequestInterstitial.setAppEventListener(this);
    }

    @Nullable
    static PublisherInterstitialAdWrapper newInstance(Context context, String gamAdUnitId, GamAdEventListener eventListener) {
        try {
            return new PublisherInterstitialAdWrapper(context, gamAdUnitId, eventListener);
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
        mListener.onEvent(AdEvent.DISPLAYED);
    }

    @Override
    public void onAdClicked() {
        mListener.onEvent(AdEvent.CLICKED);
    }

    @Override
    public void onAdLoaded() {
        mListener.onEvent(AdEvent.LOADED);
    }
    //endregion ==================== GAM AdEventListener Implementation

    public boolean isLoaded() {
        try {
            return mRequestInterstitial.isLoaded();
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
        return false;
    }

    public void show() {
        try {
            mRequestInterstitial.show();
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void loadAd(Bid bid) {
        try {
            PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();
            if (bid != null) {
                Map<String, String> targetingMap = new HashMap<>(bid.getPrebid().getTargeting());
                ReflectionUtils.handleGamCustomTargetingUpdate(adRequest, targetingMap);
            }

            mRequestInterstitial.loadAd(adRequest);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }
}
