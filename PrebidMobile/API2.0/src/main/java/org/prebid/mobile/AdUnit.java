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
    private HashMap<String, String> keywords;
    private static final HashSet<String> reservedKeys;
    private static final int MoPubQueryStringLimit = 4000;
    DemandFetcher fetcher;
    int periodMillis;

    static {
        reservedKeys = new HashSet<>();
    }


    AdUnit(@NonNull String configId, @NonNull AdType adType) {
        this.configId = configId;
        this.adType = adType;
        this.periodMillis = 0; // by default no auto refresh
        this.keywords = new HashMap<>();
    }


    public void fetchDemand(@NonNull Object adObj, @NonNull Context context, OnCompleteListener listener) {
        if (TextUtils.isEmpty(PrebidMobile.getAccountId())) {
            if (listener != null) {
                listener.onComplete(ResultCode.INVALID_ACCOUNT_ID);
            }
            return;
        }
        if (TextUtils.isEmpty(configId)) {
            if (listener != null) {
                listener.onComplete(ResultCode.INVALID_CONFIG_ID);
            }
            return;
        }
        ArrayList<AdSize> sizes = null;
        if (adType == AdType.BANNER) {
            sizes = ((BannerAdUnit) this).getSizes();
            if (sizes == null || sizes.isEmpty()) {
                if (listener != null) {
                    listener.onComplete(ResultCode.NO_SIZE_FOR_BANNER);
                }
                return;
            }
            if (adObj.getClass() == Util.getClassFromString(Util.MOPUB_BANNER_VIEW_CLASS) && sizes.size() > 1) {
                if (listener != null) {
                    listener.onComplete(ResultCode.INVALID_SIZE);
                }
            }
        }
        fetcher = new DemandFetcher(adObj, context);
        RequestParams requestParams = new RequestParams(configId, adType, sizes, false);
        if (adObj.getClass() == Util.getClassFromString(Util.DFP_AD_REQUEST_CLASS)) {
            requestParams = new RequestParams(configId, adType, sizes, true);
        }
        if (!keywords.isEmpty()) {
            for (String key : keywords.keySet()) {
                requestParams.addKeyword(key, keywords.get(key));
            }
        }
        fetcher.setPeriodMillis(periodMillis);
        fetcher.setRequestParams(requestParams);
        fetcher.setListener(listener);
        fetcher.start();
    }


    public void setUserKeyword(String key, String value) {
        keywords.put(key, value);
    }

    public void removeUserKeyword(String key) {
        keywords.remove(key);
    }

    public void removeUserKeywords() {
        keywords.clear();
    }


    static void apply(HashMap<String, String> bids, Object adObj) {
        if (adObj == null) return;
        if (adObj.getClass() == Util.getClassFromString(Util.MOPUB_BANNER_VIEW_CLASS)
                || adObj.getClass() == Util.getClassFromString(Util.MOPUB_INTERSTITIAL_CLASS)) {
            handleMoPubKeywordsUpdate(bids, adObj);
        } else if (adObj.getClass() == Util.getClassFromString(Util.DFP_AD_REQUEST_CLASS)) {
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

