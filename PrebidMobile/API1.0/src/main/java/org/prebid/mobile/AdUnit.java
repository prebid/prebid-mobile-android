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
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class AdUnit {
    private static final int MIN_AUTO_REFRESH_PERIOD_MILLIS = 30_000;

    private String configId;
    private AdType adType;
    private ArrayList<String> keywords;
    private DemandFetcher fetcher;
    private int periodMillis;

    AdUnit(@NonNull String configId, @NonNull AdType adType) {
        this.configId = configId;
        this.adType = adType;
        this.periodMillis = 0; // by default no auto refresh
        this.keywords = new ArrayList<>();
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
        if (Util.supportedAdObject(adObj)) {
            fetcher = new DemandFetcher(adObj);

            RequestParams requestParams = new RequestParams(configId, adType, sizes, keywords, minSizePerc);
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


    public void addUserKeyword(String key, String value) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            keywords.add(key + "=" + value);
        } else if (!TextUtils.isEmpty(key)) {
            keywords.add(key);
        }
    }

    public void addUserKeywords(String key, String[] values) {
        if (!TextUtils.isEmpty(key) && values.length > 0) {
            keywords.clear();
            for (String value : values) {
                keywords.add(key + "=" + value);
            }
        } else if (!TextUtils.isEmpty(key)) {
            keywords.clear();
            keywords.add(key);
        }
    }

    public void removeUserKeyword(String key) {
        ArrayList<String> toBeRemoved = new ArrayList<>();
        for (String keyword : keywords) {
            if (keyword.equals(key)) {
                toBeRemoved.add(keyword);
            } else {
                String[] keyValuePair = keyword.split("=");
                if (keyValuePair[0].equals(key)) {
                    toBeRemoved.add(keyword);
                }
            }
        }
        keywords.removeAll(toBeRemoved);
    }

    public void clearUserKeywords() {
        keywords.clear();
    }


}

