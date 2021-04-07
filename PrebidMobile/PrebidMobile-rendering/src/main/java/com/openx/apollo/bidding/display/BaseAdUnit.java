package com.openx.apollo.bidding.display;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.AdSize;
import com.openx.apollo.bidding.data.FetchDemandResult;
import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.bidding.listeners.BidRequesterListener;
import com.openx.apollo.bidding.listeners.OnFetchCompleteListener;
import com.openx.apollo.bidding.loader.BidLoader;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.sdk.ApolloSettings;
import com.openx.apollo.utils.logger.OXLog;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

abstract class BaseAdUnit {
    private static final String TAG = BaseAdUnit.class.getSimpleName();

    protected OnFetchCompleteListener mOnFetchCompleteListener;

    protected WeakReference<Context> mContextWeakReference;
    protected WeakReference<Object> mAdViewReference;
    protected AdConfiguration mAdUnitConfig = new AdConfiguration();

    protected BidLoader mBidLoader;
    private final BidRequesterListener mBidRequesterListener = new BidRequesterListener() {
        @Override
        public void onFetchCompleted(BidResponse response) {
            onResponseReceived(response);
        }

        @Override
        public void onError(AdException exception) {
            onErrorReceived(exception);
        }
    };

    protected BaseAdUnit(Context context, String configId, AdSize adSize) {
        mContextWeakReference = new WeakReference<>(context);
        initSdk(context);
        initAdConfig(configId, adSize);
        initBidLoader();
    }

    protected void fetchDemand(
        @Nullable
            Object adObject,
        @NonNull
            OnFetchCompleteListener listener) {
        if (!isAdObjectSupported(adObject)) {
            OXLog.error(TAG, "Demand fetch failed. MoPub view have to be passed in arguments.");
            listener.onComplete(FetchDemandResult.INVALID_AD_OBJECT);
            return;
        }
        if (TextUtils.isEmpty(ApolloSettings.getAccountId())) {
            OXLog.error(TAG, "Empty account id");
            listener.onComplete(FetchDemandResult.INVALID_ACCOUNT_ID);
            return;
        }
        if (TextUtils.isEmpty(mAdUnitConfig.getConfigId())) {
            OXLog.error(TAG, "Empty config id");
            listener.onComplete(FetchDemandResult.INVALID_CONFIG_ID);
            return;
        }
        mAdViewReference = new WeakReference<>(adObject);
        mOnFetchCompleteListener = listener;
        mBidLoader.load();
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

    public void destroy() {
        mOnFetchCompleteListener = null;
        mBidLoader.destroy();
        mBidLoader = null;
    }

    protected abstract void initAdConfig(String configId, AdSize adSize);

    protected abstract boolean isAdObjectSupported(
        @Nullable
            Object adObject);

    protected void onResponseReceived(BidResponse response) {
        if (mAdViewReference.get() == null || mOnFetchCompleteListener == null) {
            mBidLoader.cancelRefresh();
            OXLog.error(TAG, "Failed to pass callback. Ad object or OnFetchCompleteListener is null");
            return;
        }
        BidResponseCache.getInstance().putBidResponse(response);
        ReflectionUtils.handleMoPubKeywordsUpdate(mAdViewReference.get(), response.getTargeting());
        ReflectionUtils.setResponseIdToMoPubLocalExtras(mAdViewReference.get(), response);
        mOnFetchCompleteListener.onComplete(FetchDemandResult.SUCCESS);
    }

    protected void onErrorReceived(AdException exception) {
        if (mAdViewReference.get() == null || mOnFetchCompleteListener == null) {
            mBidLoader.cancelRefresh();
            OXLog.error(TAG, "Failed to pass callback. Ad object or OnFetchCompleteListener is null");
            return;
        }
        mOnFetchCompleteListener.onComplete(FetchDemandResult.parseErrorMessage(exception.getMessage()));
    }

    protected void initBidLoader() {
        mBidLoader = new BidLoader(mContextWeakReference.get(), mAdUnitConfig, mBidRequesterListener);
    }

    private void initSdk(Context context) {
        try {
            ApolloSettings.initializeSDK(context, () -> { });
        }
        catch (AdException e) {
            e.printStackTrace();
        }
    }
}
