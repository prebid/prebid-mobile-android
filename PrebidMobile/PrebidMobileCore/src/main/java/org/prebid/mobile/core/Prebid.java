/*
 *    Copyright 2016 APPNEXUS INC
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

package org.prebid.mobile.core;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Prebid class is the Entry point for Apps in the Prebid Module.
 */
public class Prebid {
    private static String PREBID_SERVER = "org.prebid.mobile.prebidserver.PrebidServerAdapter";

    private static String MOPUB_ADVIEW_CLASS = "com.mopub.mobileads.MoPubView";
    private static String MOPUB_INTERSTITIAL_CLASS = "com.mopub.mobileads.MoPubInterstitial";
    private static String DFP_ADREQUEST_CLASS = "com.google.android.gms.ads.doubleclick.PublisherAdRequest";

    private static boolean secureConnection = true; //by default, always use secured connection
    private static String accountId;
    private static final int kMoPubQueryStringLimit = 4000;

    //region Public APIs

    /**
     * Listener Interface to be used with attachTopBidWhenReady for Banner.
     */
    public interface OnAttachCompleteListener {

        /**
         * Called whenever the bid has been attached to the Banner view, or when the timeout has occurred. Which ever is the earliest.
         */
        void onAttachComplete(Object adObj);
    }

    /**
     * This method is used to:
     * - Validate inputs of ad units
     * - Validate the setup of the demand adapter
     * - Start the bid manager
     *
     * @param context Application context
     * @param adUnits List of Ad Slot Configurations to register
     * @throws PrebidException
     */

    public static void init(Context context, ArrayList<AdUnit> adUnits, String accountId) throws PrebidException {
        LogUtil.i("Initializing with a list of AdUnits");
        // validate context
        if (context == null) {
            throw new PrebidException(PrebidException.PrebidError.NULL_CONTEXT);
        }
        // validate account id
        if (TextUtils.isEmpty(accountId)) {
            throw new PrebidException(PrebidException.PrebidError.INVALID_ACCOUNT_ID);
        }
        Prebid.accountId = accountId;
        // validate ad units and register them
        if (adUnits == null || adUnits.isEmpty()) {
            throw new PrebidException(PrebidException.PrebidError.EMPTY_ADUNITS);
        }
        for (AdUnit adUnit : adUnits) {
            if (adUnit.getAdType().equals(AdType.BANNER) && adUnit.getSizes().isEmpty()) {
                LogUtil.e("Sizes are not added to BannerAdUnit with code: " + adUnit.getCode());
                throw new PrebidException(PrebidException.PrebidError.BANNER_AD_UNIT_NO_SIZE);
            }
            if (adUnit.getAdType().equals(AdType.INTERSTITIAL)) {
                ((InterstitialAdUnit) adUnit).setInterstitialSizes(context);
            }
            BidManager.registerAdUnit(adUnit);
        }
        // set up demand adapter
        try {
            Class<?> adapterClass = Class.forName(PREBID_SERVER);
            DemandAdapter adapter = (DemandAdapter) adapterClass.newInstance();
            if (adapter != null) {
                BidManager.adapter = adapter;
            } else {
                throw new PrebidException(PrebidException.PrebidError.UNABLE_TO_INITIALIZE_DEMAND_SOURCE);
            }
        } catch (Exception e) {
            throw new PrebidException(PrebidException.PrebidError.UNABLE_TO_INITIALIZE_DEMAND_SOURCE);
        }
        // set up bid manager
        BidManager.setBidsExpirationRunnable(context);
        // start ad requests
        BidManager.requestBidsForAdUnits(context, adUnits);
    }

    public static void attachBids(Object adObj, String adUnitCode, Context context) {
        if (adObj == null) {
            //LogUtil.e(TAG, "Request is null, unable to set keywords");
        } else {
            detachUsedBid(adObj);

            if (adObj.getClass() == getClassFromString(MOPUB_ADVIEW_CLASS)
                    || adObj.getClass() == getClassFromString(MOPUB_INTERSTITIAL_CLASS)) {
                handleMoPubKeywordsUpdate(adObj, adUnitCode, context);
            } else if (adObj.getClass() == getClassFromString(DFP_ADREQUEST_CLASS)) {
                handleDFPCustomTargetingUpdate(adObj, adUnitCode, context);
            }
        }
    }

    public static void detachUsedBid(Object adObj) {
        if (adObj != null) {
            if (adObj.getClass() == getClassFromString(MOPUB_ADVIEW_CLASS)) {
                removeUsedKeywordsForMoPub(adObj);
            } else if (adObj.getClass() == getClassFromString(DFP_ADREQUEST_CLASS)) {
                removeUsedCustomTargetingForDFP(adObj);
            }
        }
    }

    public static void attachBidsWhenReady(final Object adObject, String adUnitCode, final OnAttachCompleteListener listener, int timeOut, final Context context) {
        BidManager.getKeywordsWhenReadyForAdUnit(adUnitCode, timeOut, new BidManager.BidReadyListener() {
            @Override
            public void onBidReady(String adUnitCode) {
                attachBids(adObject, adUnitCode, context);
                listener.onAttachComplete(adObject);
            }
        });
    }

    //endregion

    //region helper methods
    private static Class getClassFromString(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    // call instance methods
    private static Object callMethodOnObject(Object object, String methodName, Object... params) {
        try {
            int len = params.length;
            Class<?>[] classes = new Class[len];
            for (int i = 0; i < len; i++) {
                classes[i] = params[i].getClass();
            }
            Method method = object.getClass().getMethod(methodName, classes);
            return method.invoke(object, params);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final LinkedList<String> usedKeywordsList = new LinkedList<String>();

    private static void handleMoPubKeywordsUpdate(Object adViewObj, String adUnitCode, Context context) {
        ArrayList<Pair<String, String>> keywordPairs = BidManager.getKeywordsForAdUnit(adUnitCode, context);

        if (keywordPairs != null && !keywordPairs.isEmpty()) {
            StringBuilder keywords = new StringBuilder();
            for (Pair<String, String> p : keywordPairs) {
                keywords.append(p.first).append(":").append(p.second).append(",");
                Class mopub_fb_adapter = getClassFromString("org.prebid.mediationadapters.mopub.FBCustomEventBanner");
                if (mopub_fb_adapter != null) {
                    Map<String, Object> localExtras = (Map) callMethodOnObject(adViewObj, "getLocalExtras");
                    if (localExtras != null) {
                        if ("hb_cache_id".equals(p.first) || "hb_bidder".equals(p.first)) {
                            localExtras.put(p.first, p.second);
                        }
                    } else {
                        LogUtil.e("To get facebook demand, enable local extras on MoPubView.");
                    }
                }
            }
            keywords.append("hb_creative_type:mediation,"); // todo this should be returned by prebid server, and called webview_rendering, native_rendering
            String prebidKeywords = keywords.toString();
            String adViewKeywords = (String) callMethodOnObject(adViewObj, "getKeywords");
            // retrieve keywords from mopub adview
            if (!TextUtils.isEmpty(adViewKeywords)) {
                adViewKeywords = prebidKeywords + adViewKeywords;
            } else {
                adViewKeywords = prebidKeywords;
            }
            // only set keywords if less than mopub query string limit
            if (adViewKeywords.length() <= kMoPubQueryStringLimit) {
                synchronized (usedKeywordsList) {
                    usedKeywordsList.add(prebidKeywords);
                }
                callMethodOnObject(adViewObj, "setKeywords", adViewKeywords);
            }
        }
    }

    private static void removeUsedKeywordsForMoPub(Object adViewObj) {
        String adViewKeywords = (String) callMethodOnObject(adViewObj, "getKeywords");
        if (!TextUtils.isEmpty(adViewKeywords) && !usedKeywordsList.isEmpty()) {
            // Copy used keywords to a temporary list to avoid concurrent modification
            // while iterating through the list
            LinkedList<String> tempUsedKeywords = new LinkedList<String>();
            for (String usedKeyword : usedKeywordsList) {
                if (!TextUtils.isEmpty(usedKeyword) && adViewKeywords.contains(usedKeyword)) {
                    adViewKeywords = adViewKeywords.replace(usedKeyword, "");
                    tempUsedKeywords.add(usedKeyword);
                }
            }
            callMethodOnObject(adViewObj, "setKeywords", adViewKeywords);

            for (String string : tempUsedKeywords) {
                synchronized (usedKeywordsList) {
                    usedKeywordsList.remove(string);
                }
            }

        }
    }

    private static final Set<String> usedKeywordKeys = new HashSet<String>();

    private static void handleDFPCustomTargetingUpdate(Object adRequestObj, String adUnitCode, Context context) {
        Bundle bundle = (Bundle) callMethodOnObject(adRequestObj, "getCustomTargeting");
        if (bundle != null) {
            ArrayList<Pair<String, String>> prebidKeywords = BidManager.getKeywordsForAdUnit(adUnitCode, context);
            if (prebidKeywords != null && !prebidKeywords.isEmpty()) {
                // retrieve keywords from mopub adview
                for (Pair<String, String> keywordPair : prebidKeywords) {
                    bundle.putString(keywordPair.first, keywordPair.second);
                    usedKeywordKeys.add(keywordPair.first);
                    Class dfp_fb_adapter = getClassFromString("org.prebid.mediationadapters.dfp.FBCustomEventBanner");
                    if (dfp_fb_adapter != null) {
                        Bundle customEventExtras = (Bundle) callMethodOnObject(adRequestObj, "getCustomEventExtrasBundle", dfp_fb_adapter);
                        if (customEventExtras != null) {
                            if ("hb_cache_id".equals(keywordPair.first) || "hb_bidder".equals(keywordPair.first)) {
                                customEventExtras.putString(keywordPair.first, keywordPair.second);
                            }
                        } else {
                            LogUtil.e("To get Facebook demand, enable custom event extras before building your publisher ad requests");
                        }
                    }
                }
            }
            // todo formalize the following lines. probably through prebid server
            bundle.putString("hb_creative_type", "mediation");
            usedKeywordKeys.add("hb_creative_type");
        }
    }

    private static void removeUsedCustomTargetingForDFP(Object adRequestObj) {
        Bundle bundle = (Bundle) callMethodOnObject(adRequestObj, "getCustomTargeting");
        if (bundle != null) {
            for (String key : usedKeywordKeys) {
                bundle.remove(key);
            }
        }
    }
    //endregion


    public static String getAccountId() {
        return accountId;
    }

    /**
     * Get whether to use secure connection
     *
     * @return true if ad requests should be loaded over secure connection
     */
    public static boolean isSecureConnection() {
        return secureConnection;
    }

    /**
     * Set whether to use secure connection for ad requests
     *
     * @param secureConnection true to use secure connection
     */
    public static void shouldLoadOverSecureConnection(boolean secureConnection) {
        // Only enables overrides for MoPub, DFP should always load over secured connection
        if (getClassFromString(MOPUB_ADVIEW_CLASS) != null) {
            Prebid.secureConnection = secureConnection;
        }
    }

    @VisibleForTesting
    public static void setTestServer(String serverAdapter) {
        PREBID_SERVER = serverAdapter;
    }
}
