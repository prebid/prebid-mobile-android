package org.prebid.mobile;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public abstract class AdUnit {
    private String configId;
    private AdType adType;
    private int periodMillis;
    private final static HashSet<DemandFetcher> fetcherReference;
    private static final HashSet<String> reservedKeys;
    private static final int MoPubQueryStringLimit = 4000;


    static {
        fetcherReference = new HashSet<>();
        reservedKeys = new HashSet<>();
    }


    AdUnit(@NonNull String configId, @NonNull AdType adType) {
        this.configId = configId;
        this.adType = adType;
        this.periodMillis = 0; // by default no auto refresh
    }

    public void setAutoRefreshPeriodMillis(int periodMillis) {
        if (this.periodMillis > 0 && this.periodMillis < 10000) { // todo change it to 30000 for production
            return;
        }
        this.periodMillis = periodMillis;
        for (DemandFetcher fetcher : fetcherReference) {
            fetcher.setPeriodMillis(this.periodMillis);
        }
    }

    public void fetchDemand(@NonNull Object adObj, @NonNull Context context, OnCompleteListener listener) {
        if (TextUtils.isEmpty(configId)) {
            if (listener != null) {
                listener.onComplete(ResultCode.INVALID_REQUEST);
            }
            return;
        }
        ArrayList<AdSize> sizes = null;
        if (adType == AdType.BANNER) {
            sizes = ((BannerAdUnit) this).getSizes();
            if (sizes == null || sizes.isEmpty()) {
                if (listener != null) {
                    listener.onComplete(ResultCode.INVALID_REQUEST);
                }
                return;
            }
        }
        DemandFetcher fetcher = null;
        for (DemandFetcher tmp : fetcherReference) {
            if (tmp.getAdObject().equals(adObj)) {
                fetcher = tmp;
            }
        }
        if (fetcher == null) {
            if (adType == AdType.BANNER) {
                fetcher = new DemandFetcher(adObj, context);
            } else {
                fetcher = new DemandFetcher(adObj, context);
            }
        }
        RequestParams requestParams = new RequestParams(configId, adType, sizes, false);
        if (adObj.getClass() == Util.getClassFromString("com.google.android.gms.ads.doubleclick.PublisherAdRequest")) {
            requestParams = new RequestParams(configId, adType, sizes, true);
        }
        fetcher.setRequestParams(requestParams);
        fetcher.setListener(listener);
        fetcher.setPeriodMillis(periodMillis);
        synchronized (fetcherReference) {
            fetcherReference.add(fetcher);
        }
        fetcher.start();
    }

    public static void stopAutoRefersh(@NonNull Object object) {
        synchronized (fetcherReference) {
            ArrayList<DemandFetcher> fetchersToRemove = new ArrayList<>();
            for (DemandFetcher fetcher : fetcherReference) {
                if (fetcher.getAdObject().equals(object)) {
                    fetcher.destroy();
                    fetchersToRemove.add(fetcher);
                }
            }
            fetcherReference.removeAll(fetchersToRemove);
        }
    }

    static void removeFetcher(DemandFetcher demandFetcher) {
        synchronized (fetcherReference) {
            fetcherReference.remove(demandFetcher);
        }
    }

    static void apply(HashMap<String, String> bids, Object adObj) {
        if (adObj == null) return;
        if (adObj.getClass() == Util.getClassFromString("com.mopub.mobileads.MoPubView")
                || adObj.getClass() == Util.getClassFromString("com.mopub.mobileads.MoPubInterstitial")) {
            handleMoPubKeywordsUpdate(bids, adObj);
        } else if (adObj.getClass() == Util.getClassFromString("com.google.android.gms.ads.doubleclick.PublisherAdRequest")) {
            handleDFPCustomTargetingUpdate(bids, adObj);
        }
    }

    private static void handleMoPubKeywordsUpdate(HashMap<String, String> bids, Object adObj) {
        removeUsedKeywordsForMoPub(adObj);
        if (bids != null && !bids.isEmpty()) {
            StringBuilder keywordsBuilder = new StringBuilder();
            for (String key : bids.keySet()) {
                addReservedKeys(key);
                keywordsBuilder.append(key).append(":").append(bids.get(key)).append(",");
            }
            String pbmKeywords = keywordsBuilder.toString();
            String adViewKeywords = (String) Util.callMethodOnObject(adObj, "getKeywords");
            if (!TextUtils.isEmpty(adViewKeywords)) {
                adViewKeywords = pbmKeywords + adViewKeywords;
            } else {
                adViewKeywords = pbmKeywords;
            }
            // only set keywords if less than mopub query string limit
            if (adViewKeywords.length() <= MoPubQueryStringLimit) {
                Util.callMethodOnObject(adObj, "setKeywords", adViewKeywords);
            }
        }
    }

    private static void handleDFPCustomTargetingUpdate(HashMap<String, String> bids, Object adObj) {
        removeUsedCustomTargetingForDFP(adObj);
        if (bids != null && !bids.isEmpty()) {
            Bundle bundle = (Bundle) Util.callMethodOnObject(adObj, "getCustomTargeting");
            if (bundle != null) {
                // retrieve keywords from mopub adview
                for (String key : bids.keySet()) {
                    bundle.putString(key, bids.get(key));
                    addReservedKeys(key);
                }
            }
        }
    }

    private static void addReservedKeys(String key) {
        synchronized (reservedKeys) {
            reservedKeys.add(key);
        }
    }

    private static void removeUsedKeywordsForMoPub(Object adViewObj) {
        String adViewKeywords = (String) Util.callMethodOnObject(adViewObj, "getKeywords");
        if (!TextUtils.isEmpty(adViewKeywords) && reservedKeys != null && !reservedKeys.isEmpty()) {
            // Copy used keywords to a temporary list to avoid concurrent modification
            // while iterating through the list
            String[] adViewKeywordsArray = adViewKeywords.split(",");
            ArrayList<String> adViewKeywordsArrayList = new ArrayList<>(Arrays.asList(adViewKeywordsArray));
            LinkedList<String> toRemove = new LinkedList<>();
            for (String keyword : adViewKeywordsArray) {
                if (!TextUtils.isEmpty(keyword) && keyword.contains(":")) {
                    String[] keywordArray = keyword.split(":");
                    if (keywordArray.length > 0) {
                        if (reservedKeys.contains(keywordArray[0])) {
                            toRemove.add(keyword);
                        }
                    }
                }
            }
            adViewKeywordsArrayList.removeAll(toRemove);
            adViewKeywords = TextUtils.join(",", adViewKeywordsArrayList);
            Util.callMethodOnObject(adViewObj, "setKeywords", adViewKeywords);
        }
    }

    private static void removeUsedCustomTargetingForDFP(Object adRequestObj) {
        Bundle bundle = (Bundle) Util.callMethodOnObject(adRequestObj, "getCustomTargeting");
        if (bundle != null && reservedKeys != null) {
            for (String key : reservedKeys) {
                bundle.remove(key);
            }
        }
    }
}

