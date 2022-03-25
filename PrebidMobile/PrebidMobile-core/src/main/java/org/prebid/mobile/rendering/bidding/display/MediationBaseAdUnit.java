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

package org.prebid.mobile.rendering.bidding.display;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.*;
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public abstract class MediationBaseAdUnit {
    private static final String TAG = MediationBaseAdUnit.class.getSimpleName();

    protected OnFetchCompleteListener mOnFetchCompleteListener;

    protected WeakReference<Context> mContextWeakReference;
    protected AdUnitConfiguration mAdUnitConfig = new AdUnitConfiguration();
    protected PrebidMediationDelegate mMediationDelegate;

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

    protected MediationBaseAdUnit(Context context, String configId, AdSize adSize, PrebidMediationDelegate mediationDelegate) {
        mContextWeakReference = new WeakReference<>(context);
        mMediationDelegate = mediationDelegate;
        mAdUnitConfig.setAutoRefreshDelay(PrebidMobile.AUTO_REFRESH_DELAY_MIN / 1000);
        initSdk(context);
        initAdConfig(configId, adSize);
        initBidLoader();
    }

    protected void fetchDemand(@NonNull OnFetchCompleteListener listener) {
        if (mMediationDelegate == null) {
            LogUtil.error(TAG, "Demand fetch failed. Mediation delegate object must be not null");
            listener.onComplete(FetchDemandResult.INVALID_AD_OBJECT);
            return;
        }
        if (TextUtils.isEmpty(PrebidMobile.getPrebidServerAccountId())) {
            LogUtil.error(TAG, "Empty account id");
            listener.onComplete(FetchDemandResult.INVALID_ACCOUNT_ID);
            return;
        }
        if (TextUtils.isEmpty(mAdUnitConfig.getConfigId())) {
            LogUtil.error(TAG, "Empty config id");
            listener.onComplete(FetchDemandResult.INVALID_CONFIG_ID);
            return;
        }

        final Host bidServerHost = PrebidMobile.getPrebidServerHost();
        if (bidServerHost.equals(Host.CUSTOM) && bidServerHost.getHostUrl().isEmpty()) {
            LogUtil.error(TAG, "Empty host url for custom Prebid Server host.");
            listener.onComplete(FetchDemandResult.INVALID_HOST_URL);
            return;
        }

        mOnFetchCompleteListener = listener;
        mBidLoader.load();
    }

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

    public void setAppContent(ContentObject content) {
        mAdUnitConfig.setAppContent(content);
    }

    public ContentObject getAppContent() {
        return mAdUnitConfig.getAppContent();
    }

    public void addUserData(DataObject dataObject) {
        mAdUnitConfig.addUserData(dataObject);
    }

    public void clearUserData() {
        mAdUnitConfig.clearUserData();
    }

    public ArrayList<DataObject> getUserData() {
        return mAdUnitConfig.getUserData();
    }

    public void destroy() {
        mOnFetchCompleteListener = null;
        mBidLoader.destroy();
        mBidLoader = null;
    }

    protected abstract void initAdConfig(String configId, AdSize adSize);

    protected void onResponseReceived(BidResponse response) {
        LogUtil.debug(TAG, "On response received");
        if (mOnFetchCompleteListener == null) {
            cancelRefresh();
            return;
        }
        BidResponseCache.getInstance().putBidResponse(response);
        mMediationDelegate.handleKeywordsUpdate(response.getTargeting());
        mMediationDelegate.setResponseToLocalExtras(response);
        mOnFetchCompleteListener.onComplete(FetchDemandResult.SUCCESS);
    }

    protected void onErrorReceived(AdException exception) {
        LogUtil.warning(TAG, "On error received");
        if (mOnFetchCompleteListener == null) {
            cancelRefresh();
            return;
        }
        mOnFetchCompleteListener.onComplete(FetchDemandResult.parseErrorMessage(exception.getMessage()));
    }

    protected void initBidLoader() {
        mBidLoader = new BidLoader(mContextWeakReference.get(), mAdUnitConfig, mBidRequesterListener);
    }

    private void initSdk(Context context) {
        PrebidMobile.setApplicationContext(context, () -> {
        });
    }

    private void cancelRefresh() {
        mBidLoader.cancelRefresh();
        LogUtil.error(TAG, "Failed to pass callback");
        if (mOnFetchCompleteListener == null) {
            LogUtil.error(TAG, "OnFetchCompleteListener is null");
        }
    }

}
