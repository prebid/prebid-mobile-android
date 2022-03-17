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

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.tasksmanager.TasksManager;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;
import org.prebid.mobile.units.configuration.BaseAdUnitConfiguration;

import java.util.*;

public abstract class AdUnit {

    private static final int MIN_AUTO_REFRESH_PERIOD_MILLIS = 30_000;

    private int periodMillis = 0; // No auto refresh
    private DemandFetcher fetcher;
    protected BaseAdUnitConfiguration configuration = createConfiguration();

    AdUnit(@NonNull String configId, @NonNull AdType adType) {
        configuration.setConfigId(configId);
        configuration.setAdType(adType);
    }

    public void setAutoRefreshPeriodMillis(@IntRange(from = MIN_AUTO_REFRESH_PERIOD_MILLIS) int periodMillis) {
        if (periodMillis < MIN_AUTO_REFRESH_PERIOD_MILLIS) {
            LogUtil.w("periodMillis less then:" + MIN_AUTO_REFRESH_PERIOD_MILLIS);
            return;
        }
        this.periodMillis = periodMillis;
        if (fetcher != null) {
            fetcher.setPeriodMillis(periodMillis);
        }
    }

    public void resumeAutoRefresh() {
        LogUtil.v("Resuming auto refresh...");
        if (fetcher != null) {
            fetcher.start();
        }
    }

    public void stopAutoRefresh() {
        LogUtil.v("Stopping auto refresh...");
        if (fetcher != null) {
            fetcher.stop();
        }
    }

    public void fetchDemand(@NonNull final OnCompleteListener2 listener) {

        final Map<String, String> keywordsMap = new HashMap<>();

        fetchDemand(keywordsMap, new OnCompleteListener() {
            @Override
            public void onComplete(final ResultCode resultCode) {
                TasksManager.getInstance().executeOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onComplete(resultCode, keywordsMap.size() != 0 ? Collections.unmodifiableMap(keywordsMap) : null);
                    }
                });
            }
        });
    }

    public void fetchDemand(@NonNull Object adObj, @NonNull OnCompleteListener listener) {
        if (TextUtils.isEmpty(PrebidMobile.getPrebidServerAccountId())) {
            LogUtil.e("Empty account id.");
            listener.onComplete(ResultCode.INVALID_ACCOUNT_ID);
            return;
        }
        if (TextUtils.isEmpty(configuration.getConfigId())) {
            LogUtil.e("Empty config id.");
            listener.onComplete(ResultCode.INVALID_CONFIG_ID);
            return;
        }
        if (PrebidMobile.getPrebidServerHost().equals(Host.CUSTOM)) {
            if (TextUtils.isEmpty(PrebidMobile.getPrebidServerHost().getHostUrl())) {
                LogUtil.e("Empty host url for custom Prebid Server host.");
                listener.onComplete(ResultCode.INVALID_HOST_URL);
                return;
            }
        }

        if (configuration.getAdType() == AdType.BANNER || configuration.getAdType() == AdType.VIDEO) {
            HashSet<AdSize> sizes = configuration.castToOriginal().getSizes();
            for (AdSize size : sizes) {
                if (size.getWidth() < 0 || size.getHeight() < 0) {
                    listener.onComplete(ResultCode.INVALID_SIZE);
                    return;
                }
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
            LogUtil.e("Invalid context");
            listener.onComplete(ResultCode.INVALID_CONTEXT);
            return;
        }

        if (Util.supportedAdObject(adObj)) {
            fetcher = new DemandFetcher(adObj);
            fetcher.setPeriodMillis(periodMillis);
            fetcher.setConfiguration(configuration);
            fetcher.setListener(listener);
            if (periodMillis >= 30000) {
                LogUtil.v("Start fetching bids with auto refresh millis: " + periodMillis);
            } else {
                LogUtil.v("Start a single fetching.");
            }
            fetcher.start();
        } else {
            listener.onComplete(ResultCode.INVALID_AD_OBJECT);
        }

    }

    // MARK: - adunit context data aka inventory data (imp[].ext.context.data)

    /**
     * This method obtains the context data keyword & value for adunit context targeting
     * if the key already exists the value will be appended to the list. No duplicates will be added
     */
    public void addContextData(String key, String value) {
        configuration.addContextData(key, value);
    }

    /**
     * This method obtains the context data keyword & values for adunit context targeting
     * the values if the key already exist will be replaced with the new set of values
     */
    public void updateContextData(String key, Set<String> value) {
        configuration.addContextData(key, value);
    }

    /**
     * This method allows to remove specific context data keyword & values set from adunit context targeting
     */
    public void removeContextData(String key) {
        configuration.removeContextData(key);
    }

    /**
     * This method allows to remove all context data set from adunit context targeting
     */
    public void clearContextData() {
        configuration.clearContextData();
    }

    Map<String, Set<String>> getContextDataDictionary() {
        return configuration.getContextDataDictionary();
    }

    // MARK: - adunit context keywords (imp[].ext.context.keywords)

    /**
     * This method obtains the context keyword for adunit context targeting
     * Inserts the given element in the set if it is not already present.
     */
    public void addContextKeyword(String keyword) {
        configuration.addContextKeyword(keyword);
    }

    /**
     * This method obtains the context keyword set for adunit context targeting
     * Adds the elements of the given set to the set.
     */
    public void addContextKeywords(Set<String> keywords) {
        configuration.addContextKeywords(keywords);
    }

    /**
     * This method allows to remove specific context keyword from adunit context targeting
     */
    public void removeContextKeyword(String keyword) {
        configuration.removeContextKeyword(keyword);
    }

    /**
     * This method allows to remove all keywords from the set of adunit context targeting
     */
    public void clearContextKeywords() {
        configuration.clearContextKeywords();
    }

    Set<String> getContextKeywordsSet() {
        return configuration.getContextKeywordsSet();
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

    protected BaseAdUnitConfiguration createConfiguration() {
        return new AdUnitConfiguration();
    }

    @VisibleForTesting
    public BaseAdUnitConfiguration getConfiguration() {
        return (BaseAdUnitConfiguration) configuration;
    }

}

