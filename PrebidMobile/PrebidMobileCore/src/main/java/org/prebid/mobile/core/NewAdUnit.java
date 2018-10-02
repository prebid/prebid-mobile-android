package org.prebid.mobile.core;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public abstract class NewAdUnit {
    private String configId;
    AdType adType;
    private static HashSet<NewDemandFetcher> fetcherReference;
    private int period;

    static {
        fetcherReference = new HashSet<>();
    }


    NewAdUnit(@NonNull String configId, @NonNull AdType adType) {
        this.configId = configId;
        this.adType = adType;
        this.period = 0; // by default no auto refresh
    }

    public void setAutoRefreshPeriod(int period) {
        this.period = period;
        for (NewDemandFetcher fetcher : fetcherReference) {
            fetcher.setPeriod(this.period);
        }
    }

    public void fetchDemand(@NonNull Object adObj, @NonNull Context context, NewOnCompleteListener listener) {
        NewDemandFetcher fetcher = null;
        for (NewDemandFetcher tmp : fetcherReference) {
            if (tmp.getAdObject().equals(adObj)) {
                fetcher = tmp;
            }
        }
        if (fetcher == null) {
            if (adType == AdType.BANNER) {
                fetcher = new NewDemandFetcher(adObj, context, listener, configId, ((NewBannerAdUnit) this).getSizes(), adType);
            } else {
                fetcher = new NewDemandFetcher(adObj, context, listener, configId, null, adType);
            }
        }
        fetcher.setPeriod(period);
        fetcherReference.add(fetcher);
        fetcher.start();
    }

    public void stopDemand(@NonNull Object adObj) {
        for (NewDemandFetcher tmp : fetcherReference) {
            if (tmp.getAdObject().equals(adObj)) {
                tmp.stop();
                fetcherReference.remove(tmp);
            }
        }
    }

    // region Helper methods
    private static HashSet<String> reservedKeys;
    private static final int MoPubQueryStringLimit = 4000;

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
        if (reservedKeys == null) {
            reservedKeys = new HashSet<>();
        }
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
    // endregion
}
