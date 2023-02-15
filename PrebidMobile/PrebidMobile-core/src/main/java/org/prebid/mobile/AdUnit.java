/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

import static org.prebid.mobile.PrebidMobile.AUTO_REFRESH_DELAY_MAX;
import static org.prebid.mobile.PrebidMobile.AUTO_REFRESH_DELAY_MIN;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.FetchDemandResult;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.tasksmanager.TasksManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AdUnit {

    protected AdUnitConfiguration configuration = new AdUnitConfiguration();

    @Nullable
    protected BidLoader bidLoader;
    @Nullable
    protected Object adObject;

    AdUnit(@NonNull String configId, @NonNull AdFormat adType) {
        configuration.setConfigId(configId);
        configuration.setAdFormat(adType);
        configuration.setIsOriginalAdUnit(true);
    }

    /**
     * @deprecated Please use setAutoRefreshInterval() in seconds!
     */
    @Deprecated
    public void setAutoRefreshPeriodMillis(
            @IntRange(from = AUTO_REFRESH_DELAY_MIN, to = AUTO_REFRESH_DELAY_MAX) int periodMillis
    ) {
        configuration.setAutoRefreshDelay(periodMillis / 1000);
    }

    public void setAutoRefreshInterval(
            @IntRange(from = AUTO_REFRESH_DELAY_MIN / 1000, to = AUTO_REFRESH_DELAY_MAX / 1000) int seconds
    ) {
        configuration.setAutoRefreshDelay(seconds);
    }

    public void resumeAutoRefresh() {
        LogUtil.verbose("Resuming auto refresh...");
        if (bidLoader != null) {
            bidLoader.setupRefreshTimer();
        }
    }

    public void stopAutoRefresh() {
        LogUtil.verbose("Stopping auto refresh...");
        if (bidLoader != null) {
            bidLoader.cancelRefresh();
        }
    }

    public void fetchDemand(@NonNull final OnCompleteListener2 listener) {
        final Map<String, String> keywordsMap = new HashMap<>();

        fetchDemand(keywordsMap, resultCode -> {
            TasksManager.getInstance().executeOnMainThread(() ->
                    listener.onComplete(resultCode, keywordsMap.size() != 0 ? Collections.unmodifiableMap(keywordsMap) : null)
            );
        });
    }

    public void fetchDemand(@NonNull Object adObj, @NonNull OnCompleteListener listener) {
        if (TextUtils.isEmpty(PrebidMobile.getPrebidServerAccountId())) {
            LogUtil.error("Empty account id.");
            listener.onComplete(ResultCode.INVALID_ACCOUNT_ID);
            return;
        }
        if (TextUtils.isEmpty(configuration.getConfigId())) {
            LogUtil.error("Empty config id.");
            listener.onComplete(ResultCode.INVALID_CONFIG_ID);
            return;
        }
        if (PrebidMobile.getPrebidServerHost().equals(Host.CUSTOM)) {
            if (TextUtils.isEmpty(PrebidMobile.getPrebidServerHost().getHostUrl())) {
                LogUtil.error("Empty host url for custom Prebid Server host.");
                listener.onComplete(ResultCode.INVALID_HOST_URL);
                return;
            }
        }

        HashSet<AdSize> sizes = configuration.getSizes();
        for (AdSize size : sizes) {
            if (size.getWidth() < 0 || size.getHeight() < 0) {
                listener.onComplete(ResultCode.INVALID_SIZE);
                return;
            }
        }

        Context context = PrebidMobile.getApplicationContext();
        if (context != null) {
            ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (conMgr != null && context.checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE") == PackageManager.PERMISSION_GRANTED) {
                NetworkInfo activeNetworkInfo = conMgr.getActiveNetworkInfo();
                if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
                    listener.onComplete(ResultCode.NETWORK_ERROR);
                    return;
                }
            }
        } else {
            LogUtil.error("Invalid context");
            listener.onComplete(ResultCode.INVALID_CONTEXT);
            return;
        }

        if (Util.supportedAdObject(adObj)) {
            adObject = adObj;
            bidLoader = new BidLoader(
                    context,
                    configuration,
                    createBidListener(listener)
            );

            if (configuration.getAutoRefreshDelay() > 0) {
                BidLoader.BidRefreshListener bidRefreshListener = () -> true;
                bidLoader.setBidRefreshListener(bidRefreshListener);
                LogUtil.verbose("Start fetching bids with auto refresh millis: " + configuration.getAutoRefreshDelay());
            } else {
                bidLoader.setBidRefreshListener(null);
                LogUtil.verbose("Start a single fetching.");
            }

            bidLoader.load();
        } else {
            adObject = null;
            listener.onComplete(ResultCode.INVALID_AD_OBJECT);
        }

    }

    // MARK: - adunit context data aka inventory data (imp[].ext.data)

    /**
     * This method obtains the context data keyword & value for adunit context targeting
     * if the key already exists the value will be appended to the list. No duplicates will be added
     */
    @Deprecated
    public void addContextData(String key, String value) {
        configuration.addExtData(key, value);
    }

    /**
     * This method obtains the context data keyword & values for adunit context targeting
     * the values if the key already exist will be replaced with the new set of values
     */
    @Deprecated
    public void updateContextData(String key, Set<String> value) {
        configuration.addExtData(key, value);
    }

    /**
     * This method allows to remove specific context data keyword & values set from adunit context targeting
     */
    @Deprecated
    public void removeContextData(String key) {
        configuration.removeExtData(key);
    }

    /**
     * This method allows to remove all context data set from adunit context targeting
     */
    @Deprecated
    public void clearContextData() {
        configuration.clearExtData();
    }

    @Deprecated
    Map<String, Set<String>> getContextDataDictionary() {
        return configuration.getExtDataDictionary();
    }

    /**
     * This method obtains the context data keyword & value for adunit context targeting
     * if the key already exists the value will be appended to the list. No duplicates will be added
     */
    public void addExtData(String key, String value) {
        configuration.addExtData(key, value);
    }

    /**
     * This method obtains the context data keyword & values for adunit context targeting
     * the values if the key already exist will be replaced with the new set of values
     */
    public void updateExtData(String key, Set<String> value) {
        configuration.addExtData(key, value);
    }

    /**
     * This method allows to remove specific context data keyword & values set from adunit context targeting
     */
    public void removeExtData(String key) {
        configuration.removeExtData(key);
    }

    /**
     * This method allows to remove all context data set from adunit context targeting
     */
    public void clearExtData() {
        configuration.clearExtData();
    }

    Map<String, Set<String>> getExtDataDictionary() {
        return configuration.getExtDataDictionary();
    }

    // MARK: - adunit context keywords (imp[].ext.keywords)

    /**
     * This method obtains the context keyword for adunit context targeting
     * Inserts the given element in the set if it is not already present.
     *
     * @deprecated Use addExtKeyword
     */
    @Deprecated
    public void addContextKeyword(String keyword) {
        configuration.addExtKeyword(keyword);
    }

    /**
     * This method obtains the context keyword set for adunit context targeting
     * Adds the elements of the given set to the set.
     *
     * @deprecated Use addExtKeywords
     */
    @Deprecated
    public void addContextKeywords(Set<String> keywords) {
        configuration.addExtKeywords(keywords);
    }

    /**
     * This method allows to remove specific context keyword from adunit context targeting
     * @deprecated Use removeExtKeyword
     */
    @Deprecated
    public void removeContextKeyword(String keyword) {
        configuration.removeExtKeyword(keyword);
    }

    /**
     * This method allows to remove all keywords from the set of adunit context targeting
     *
     * @deprecated Use clearExtKeywords
     */
    @Deprecated
    public void clearContextKeywords() {
        configuration.clearExtKeywords();
    }

    @Deprecated
    Set<String> getContextKeywordsSet() {
        return configuration.getExtKeywordsSet();
    }

    /**
     * This method obtains the context keyword for adunit context targeting
     * Inserts the given element in the set if it is not already present.
     */
    public void addExtKeyword(String keyword) {
        configuration.addExtKeyword(keyword);
    }

    /**
     * This method obtains the context keyword set for adunit context targeting
     * Adds the elements of the given set to the set.
     */
    public void addExtKeywords(Set<String> keywords) {
        configuration.addExtKeywords(keywords);
    }

    /**
     * This method allows to remove specific context keyword from adunit context targeting
     */
    public void removeExtKeyword(String keyword) {
        configuration.removeExtKeyword(keyword);
    }

    /**
     * This method allows to remove all keywords from the set of adunit context targeting
     */
    public void clearExtKeywords() {
        configuration.clearExtKeywords();
    }

    Set<String> getExtKeywordsSet() {
        return configuration.getExtKeywordsSet();
    }

    /**
     * This method obtains the content for adunit, content, in which impression will appear
     */
    public void setAppContent(ContentObject content) {
        configuration.setAppContent(content);
    }

    public ContentObject getAppContent() {
        return configuration.getAppContent();
    }

    public void addUserData(DataObject dataObject) {
        configuration.addUserData(dataObject);
    }

    public ArrayList<DataObject> getUserData() {
        return configuration.getUserData();
    }

    public void clearUserData() {
        configuration.clearUserData();
    }

    public String getPbAdSlot() {
        return configuration.getPbAdSlot();
    }

    public void setPbAdSlot(String pbAdSlot) {
        configuration.setPbAdSlot(pbAdSlot);
    }


    protected BidRequesterListener createBidListener(OnCompleteListener originalListener) {
        return new BidRequesterListener() {
            @Override
            public void onFetchCompleted(BidResponse response) {
                HashMap<String, String> keywords = response.getTargeting();
                Util.apply(keywords, adObject);
                originalListener.onComplete(ResultCode.SUCCESS);
            }

            @Override
            public void onError(AdException exception) {
                Util.apply(null, adObject);
                originalListener.onComplete(convertToResultCode(exception));
            }
        };
    }

    protected ResultCode convertToResultCode(AdException renderingException) {
        FetchDemandResult fetchDemandResult = FetchDemandResult.parseErrorMessage(renderingException.getMessage());
        LogUtil.error("Prebid", "Can't download bids: " + fetchDemandResult);
        switch (fetchDemandResult) {
            case INVALID_ACCOUNT_ID:
                return ResultCode.INVALID_ACCOUNT_ID;
            case INVALID_CONFIG_ID:
                return ResultCode.INVALID_CONFIG_ID;
            case INVALID_SIZE:
                return ResultCode.INVALID_SIZE;
            case INVALID_CONTEXT:
                return ResultCode.INVALID_CONTEXT;
            case INVALID_AD_OBJECT:
                return ResultCode.INVALID_AD_OBJECT;
            case INVALID_HOST_URL:
                return ResultCode.INVALID_HOST_URL;
            case NETWORK_ERROR:
                return ResultCode.NETWORK_ERROR;
            case TIMEOUT:
                return ResultCode.TIMEOUT;
            case NO_BIDS:
                return ResultCode.NO_BIDS;
            default:
                return ResultCode.PREBID_SERVER_ERROR;
        }
    }


    @VisibleForTesting
    public AdUnitConfiguration getConfiguration() {
        return configuration;
    }

}

