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
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import org.prebid.mobile.tasksmanager.TasksManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AdUnit {
    private static final int MIN_AUTO_REFRESH_PERIOD_MILLIS = 30_000;

    private String configId;
    private AdType adType;

    private DemandFetcher fetcher;
    private int periodMillis;

    private final Map<String, Set<String>> contextDataDictionary;
    private final Set<String> contextKeywordsSet;

    private String pbAdSlot;

    AdUnit(@NonNull String configId, @NonNull AdType adType) {
        this.configId = configId;
        this.adType = adType;
        this.periodMillis = 0; // by default no auto refresh

        this.contextDataDictionary = new HashMap<>();
        this.contextKeywordsSet = new HashSet<>();

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

    public void stopAutoRefresh() {
        LogUtil.v("Stopping auto refresh...");
        if (fetcher != null) {
            fetcher.destroy();
            fetcher = null;
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
        if (TextUtils.isEmpty(configId)) {
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

        HashSet<AdSize> sizes = null;
        if (adType == AdType.BANNER) {
            sizes = ((BannerAdUnit) this).getSizes();
            for (AdSize size : sizes) {
                if (size.getWidth() < 0 || size.getHeight() < 0) {
                    listener.onComplete(ResultCode.INVALID_SIZE);
                    return;
                }
            }
        } else if (adType == AdType.VIDEO) {

            VideoAdUnit videoAdUnit = (VideoAdUnit) this;

            sizes = new HashSet<>(1);
            sizes.add(videoAdUnit.getAdSize());
            for (AdSize size : sizes) {
                if (size.getWidth() < 0 || size.getHeight() < 0) {
                    listener.onComplete(ResultCode.INVALID_SIZE);
                    return;
                }
            }

        }
        AdSize minSizePerc = null;
        if (this instanceof InterstitialAdUnit) {
            InterstitialAdUnit interstitialAdUnit = (InterstitialAdUnit) this;

            minSizePerc = interstitialAdUnit.getMinSizePerc();
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
            listener.onComplete(ResultCode.INVALID_CONTEXT);
            return;
        }

        BannerBaseAdUnit.Parameters bannerParameters = null;
        if (this instanceof BannerBaseAdUnit) {
            BannerBaseAdUnit bannerBaseAdUnit = (BannerBaseAdUnit) this;
            bannerParameters = bannerBaseAdUnit.parameters;
        }

        VideoBaseAdUnit.Parameters videoParameters = null;
        if (this instanceof VideoBaseAdUnit) {
            VideoBaseAdUnit videoBaseAdUnit = (VideoBaseAdUnit) this;
            videoParameters = videoBaseAdUnit.parameters;
        }

        if (Util.supportedAdObject(adObj)) {
            fetcher = new DemandFetcher(adObj);
            RequestParams requestParams = new RequestParams(configId, adType, sizes, contextDataDictionary, contextKeywordsSet, minSizePerc, pbAdSlot, bannerParameters, videoParameters);
            if (this.adType.equals(AdType.NATIVE)) {
                requestParams.setNativeRequestParams(((NativeAdUnit) this).params);
            }
            fetcher.setPeriodMillis(periodMillis);
            fetcher.setRequestParams(requestParams);
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
        Util.addValue(contextDataDictionary, key, value);
    }

    /**
     * This method obtains the context data keyword & values for adunit context targeting
     * the values if the key already exist will be replaced with the new set of values
     */
    public void updateContextData(String key, Set<String> value) {
        contextDataDictionary.put(key, value);
    }

    /**
     * This method allows to remove specific context data keyword & values set from adunit context targeting
     */
    public void removeContextData(String key) {
        contextDataDictionary.remove(key);
    }

    /**
     * This method allows to remove all context data set from adunit context targeting
     */
    public void clearContextData() {
        contextDataDictionary.clear();
    }

    Map<String, Set<String>> getContextDataDictionary() {
        return contextDataDictionary;
    }

    // MARK: - adunit context keywords (imp[].ext.context.keywords)

    /**
     * This method obtains the context keyword for adunit context targeting
     * Inserts the given element in the set if it is not already present.
     */
    public void addContextKeyword(String keyword) {
        contextKeywordsSet.add(keyword);
    }

    /**
     * This method obtains the context keyword set for adunit context targeting
     * Adds the elements of the given set to the set.
     */
    public void addContextKeywords(Set<String> keywords) {
        contextKeywordsSet.addAll(keywords);
    }

    /**
     * This method allows to remove specific context keyword from adunit context targeting
     */
    public void removeContextKeyword(String keyword) {
        contextKeywordsSet.remove(keyword);
    }

    /**
     * This method allows to remove all keywords from the set of adunit context targeting
     */
    public void clearContextKeywords() {
        contextKeywordsSet.clear();
    }

    Set<String> getContextKeywordsSet() {
        return contextKeywordsSet;
    }

    public String getPbAdSlot() {
        return pbAdSlot;
    }

    public void setPbAdSlot(String pbAdSlot) {
        this.pbAdSlot = pbAdSlot;
    }
}

