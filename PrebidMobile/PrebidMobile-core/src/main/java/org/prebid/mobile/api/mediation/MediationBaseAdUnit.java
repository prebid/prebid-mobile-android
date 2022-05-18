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

package org.prebid.mobile.api.mediation;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.*;
import org.prebid.mobile.api.data.FetchDemandResult;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.mediation.listeners.OnFetchCompleteListener;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public abstract class MediationBaseAdUnit {

    private static final String TAG = MediationBaseAdUnit.class.getSimpleName();

    protected OnFetchCompleteListener onFetchCompleteListener;
    protected WeakReference<Context> contextWeakReference;
    protected AdUnitConfiguration adUnitConfig = new AdUnitConfiguration();
    protected PrebidMediationDelegate mediationDelegate;
    protected BidLoader bidLoader;

    private final BidRequesterListener bidRequesterListener = new BidRequesterListener() {
        @Override
        public void onFetchCompleted(BidResponse response) {
            onResponseReceived(response);
        }

        @Override
        public void onError(AdException exception) {
            onErrorReceived(exception);
        }
    };

    protected MediationBaseAdUnit(
        Context context,
        String configId,
        AdSize adSize,
        PrebidMediationDelegate mediationDelegate
    ) {
        contextWeakReference = new WeakReference<>(context);
        this.mediationDelegate = mediationDelegate;
        adUnitConfig.setAutoRefreshDelay(PrebidMobile.AUTO_REFRESH_DELAY_MIN / 1000);
        initSdk(context);
        initAdConfig(configId, adSize);
        initBidLoader();
    }

    protected void fetchDemand(@NonNull OnFetchCompleteListener listener) {
        if (mediationDelegate == null) {
            LogUtil.error(TAG, "Demand fetch failed. Mediation delegate object must be not null");
            listener.onComplete(FetchDemandResult.INVALID_AD_OBJECT);
            return;
        }
        if (TextUtils.isEmpty(PrebidMobile.getPrebidServerAccountId())) {
            LogUtil.error(TAG, "Empty account id");
            listener.onComplete(FetchDemandResult.INVALID_ACCOUNT_ID);
            return;
        }
        if (TextUtils.isEmpty(adUnitConfig.getConfigId())) {
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

        onFetchCompleteListener = listener;
        bidLoader.load();
    }

    public void addContextData(
        String key,
        String value
    ) {
        adUnitConfig.addContextData(key, value);
    }

    public void updateContextData(
        String key,
        Set<String> value
    ) {
        adUnitConfig.addContextData(key, value);
    }

    public void removeContextData(String key) {
        adUnitConfig.removeContextData(key);
    }

    public void clearContextData() {
        adUnitConfig.clearContextData();
    }

    public Map<String, Set<String>> getContextDataDictionary() {
        return adUnitConfig.getContextDataDictionary();
    }

    public void addContextKeyword(String keyword) {
        adUnitConfig.addContextKeyword(keyword);
    }

    public void addContextKeywords(Set<String> keywords) {
        adUnitConfig.addContextKeywords(keywords);
    }

    public void removeContextKeyword(String keyword) {
        adUnitConfig.removeContextKeyword(keyword);
    }

    public Set<String> getContextKeywordsSet() {
        return adUnitConfig.getContextKeywordsSet();
    }

    public void clearContextKeywords() {
        adUnitConfig.clearContextKeywords();
    }

    public void setPbAdSlot(String adSlot) {
        adUnitConfig.setPbAdSlot(adSlot);
    }

    @Nullable
    public String getPbAdSlot() {
        return adUnitConfig.getPbAdSlot();
    }

    public void setAppContent(ContentObject content) {
        adUnitConfig.setAppContent(content);
    }

    public ContentObject getAppContent() {
        return adUnitConfig.getAppContent();
    }

    public void addUserData(DataObject dataObject) {
        adUnitConfig.addUserData(dataObject);
    }

    public void clearUserData() {
        adUnitConfig.clearUserData();
    }

    public ArrayList<DataObject> getUserData() {
        return adUnitConfig.getUserData();
    }

    public void destroy() {
        onFetchCompleteListener = null;
        bidLoader.destroy();
        bidLoader = null;
    }

    protected abstract void initAdConfig(
        String configId,
        AdSize adSize
    );

    protected void onResponseReceived(BidResponse response) {
        LogUtil.debug(TAG, "On response received");
        if (onFetchCompleteListener == null) {
            cancelRefresh();
            return;
        }
        BidResponseCache.getInstance().putBidResponse(response);
        mediationDelegate.handleKeywordsUpdate(response.getTargeting());
        mediationDelegate.setResponseToLocalExtras(response);
        onFetchCompleteListener.onComplete(FetchDemandResult.SUCCESS);
    }

    protected void onErrorReceived(AdException exception) {
        LogUtil.warning(TAG, "On error received");
        if (onFetchCompleteListener == null) {
            cancelRefresh();
            return;
        }
        onFetchCompleteListener.onComplete(FetchDemandResult.parseErrorMessage(exception.getMessage()));
    }

    protected void initBidLoader() {
        bidLoader = new BidLoader(contextWeakReference.get(), adUnitConfig, bidRequesterListener);
    }

    private void initSdk(Context context) {
        PrebidMobile.initializeSdk(context, null);
    }

    private void cancelRefresh() {
        bidLoader.cancelRefresh();
        LogUtil.error(TAG, "Failed to pass callback");
        if (onFetchCompleteListener == null) {
            LogUtil.error(TAG, "OnFetchCompleteListener is null");
        }
    }

}
