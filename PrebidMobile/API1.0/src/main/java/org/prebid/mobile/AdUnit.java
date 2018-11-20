package org.prebid.mobile;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class AdUnit {
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

    public void setAutoRefreshPeriodMillis(int periodMillis) {
        if (periodMillis < 30000) {
            return;
        }
        this.periodMillis = periodMillis;
        if (fetcher != null) {
            fetcher.setPeriodMillis(periodMillis);
        }
    }

    public void stopAutoRefersh() {
        LogUtil.v("Stopping auto refresh...");
        if (fetcher != null) {
            fetcher.destroy();
            fetcher = null;
        }
    }


    public void fetchDemand(@NonNull Object adObj, @NonNull OnCompleteListener listener) {
        if (TextUtils.isEmpty(PrebidMobile.getAccountId())) {
            LogUtil.e("Invalid account id.");
            listener.onComplete(ResultCode.INVALID_ACCOUNT_ID);
            return;
        }
        if (TextUtils.isEmpty(configId)) {
            LogUtil.e("Invalid config id.");
            listener.onComplete(ResultCode.INVALID_CONFIG_ID);
            return;
        }
        if (PrebidMobile.getHost().equals(Host.CUSTOM)) {
            if (TextUtils.isEmpty(PrebidMobile.getHost().getHostUrl())) {
                LogUtil.e("Empty host url for custom Prebid Server host.");
                listener.onComplete(ResultCode.INVALID_HOST_URL);
                return;
            }
        }
        HashSet<AdSize> sizes = null;
        if (adType == AdType.BANNER) {
            sizes = ((BannerAdUnit) this).getSizes();
            if (sizes == null || sizes.isEmpty()) {
                LogUtil.e("No size set for banner ad unit.");
                listener.onComplete(ResultCode.NO_SIZE_FOR_BANNER);
                return;
            }
            if (adObj.getClass() == Util.getClassFromString(Util.MOPUB_BANNER_VIEW_CLASS) && sizes.size() > 1) {
                LogUtil.e("More than one size passed for MoPub ad view.");
                listener.onComplete(ResultCode.INVALID_SIZE);
            }
        }
        fetcher = new DemandFetcher(adObj);
        RequestParams requestParams = new RequestParams(configId, adType, sizes, keywords);
        fetcher.setPeriodMillis(periodMillis);
        fetcher.setRequestParams(requestParams);
        fetcher.setListener(listener);
        if (periodMillis >= 30000) {
            LogUtil.v("Start fetching bids with auto refresh millis: " + periodMillis);
        } else {
            LogUtil.v("Start a single fetching.");
        }
        fetcher.start();
    }


    public void setUserKeyword(String key, String value) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            keywords.add(key + "=" + value);
        } else if (!TextUtils.isEmpty(key)) {
            keywords.add(key);
        }
    }

    public void setUserKeywords(String key, String[] values) {
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

    public void removeUserKeywords() {
        keywords.clear();
    }


}

