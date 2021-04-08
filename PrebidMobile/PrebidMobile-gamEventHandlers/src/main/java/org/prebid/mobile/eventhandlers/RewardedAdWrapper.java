package org.prebid.mobile.eventhandlers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.rewarded.OnAdMetadataChangedListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.display.ReflectionUtils;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.util.HashMap;
import java.util.Map;

import static org.prebid.mobile.eventhandlers.global.Constants.APP_EVENT;

/**
 * This class is responsible for wrapping usage of RewardedAd from GAM SDK.
 * To achieve safe integration between various GAM SDK versions we have to wrap all PublisherAdView method execution in try / catch.
 * This class instance should be created via newInstance method, which will catch any potential exception on RewardedAd / RewardedAdWrapper instance creation
 */
public class RewardedAdWrapper extends RewardedAdCallback implements OnAdMetadataChangedListener {

    private static final String TAG = RewardedAdWrapper.class.getSimpleName();
    public static final String KEY_METADATA = "AdTitle";

    private final RewardedAd mRewardedAd;
    private final GamAdEventListener mListener;

    private RewardedAdLoadCallback mRewardedAdLoadCallback = new RewardedAdLoadCallback() {
        @Override
        public void onRewardedAdLoaded() {
            mListener.onEvent(AdEvent.LOADED);
        }

        @Override
        public void onRewardedAdFailedToLoad(int errorCode) {
            notifyErrorListener(errorCode);
        }
    };

    private RewardedAdWrapper(Context context, String gamAdUnitId, GamAdEventListener eventListener) {
        mListener = eventListener;

        mRewardedAd = new RewardedAd(context, gamAdUnitId);
        mRewardedAd.setOnAdMetadataChangedListener(this);
    }

    @Nullable
    static RewardedAdWrapper newInstance(Context context, String gamAdUnitId, GamAdEventListener eventListener) {
        try {
            return new RewardedAdWrapper(context, gamAdUnitId, eventListener);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
        return null;
    }

    //region ==================== GAM AppEventsListener Implementation
    @Override
    public void onAdMetadataChanged() {
        if (metadataContainsAdEvent()) {
            mListener.onEvent(AdEvent.APP_EVENT_RECEIVED);
        }
    }
    //endregion ==================== GAM AppEventsListener Implementation

    //region ==================== GAM AdEventListener Implementation
    @Override
    public void onRewardedAdOpened() {
        mListener.onEvent(AdEvent.DISPLAYED);
    }

    @Override
    public void onRewardedAdClosed() {
        mListener.onEvent(AdEvent.CLOSED);
    }

    @Override
    public void onRewardedAdFailedToShow(int errorCode) {
        notifyErrorListener(errorCode);
    }

    @Override
    public void onUserEarnedReward(
        @NonNull
            RewardItem rewardItem) {
        mListener.onEvent(AdEvent.REWARD_EARNED);
        mListener.onEvent(AdEvent.CLOSED);
    }
    //endregion ==================== GAM AdEventListener Implementation

    public void loadAd(Bid bid) {
        try {
            PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();

            if (bid != null) {
                Map<String, String> targetingMap = new HashMap<>(bid.getPrebid().getTargeting());
                ReflectionUtils.handleGamCustomTargetingUpdate(adRequest, targetingMap);
            }

            mRewardedAd.loadAd(adRequest, mRewardedAdLoadCallback);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public boolean isLoaded() {
        try {
            return mRewardedAd.isLoaded();
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }

        return false;
    }

    public void show(Activity activity) {
        try {
            mRewardedAd.show(activity, this);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public Object getRewardItem() {
        return mRewardedAd != null ? mRewardedAd.getRewardItem() : null;
    }

    private void notifyErrorListener(int errorCode) {
        final AdEvent adEvent = AdEvent.FAILED;
        adEvent.setErrorCode(errorCode);

        mListener.onEvent(adEvent);
    }

    @VisibleForTesting
    boolean metadataContainsAdEvent() {
        try {
            if (mRewardedAd == null) {
                OXLog.debug(TAG, "metadataContainsAdEvent: Failed to process. RewardedAd is null.");
                return false;
            }

            final Bundle adMetadata = mRewardedAd.getAdMetadata();
            return adMetadata != null && APP_EVENT.equals(adMetadata.getString(KEY_METADATA));
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
        return false;
    }
}
