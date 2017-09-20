/*
 *    Copyright 2016 Prebid.org, Inc.
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
import android.text.TextUtils;
import android.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static org.prebid.mobile.core.PrebidDemandSettings.PREBID_BIDDER;
import static org.prebid.mobile.core.PrebidDemandSettings.PREBID_CACHE_ID;
import static org.prebid.mobile.core.PrebidDemandSettings.PREBID_CREATIVE_LOAD_TYPE;
import static org.prebid.mobile.core.PrebidDemandSettings.PREBID_DEMAND_SDK;
import static org.prebid.mobile.core.PrebidDemandSettings.PREBID_DFP_CUSTOM_EVENT_BANNER;
import static org.prebid.mobile.core.PrebidDemandSettings.PREBID_DFP_CUSTOM_EVENT_INTERSTITIAL;
import static org.prebid.mobile.core.PrebidDemandSettings.PREBID_MOPUB_CUSTOM_EVENT_BANNER;

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
    private static boolean useLocalCache = true;
    private static Host host = Host.APPNEXUS;
    private static AdServer adServer = AdServer.UNKNOWN;

    public enum AdServer {
        DFP,
        MOPUB,
        UNKNOWN
    }

    public enum Host {
        APPNEXUS,
        RUBICON
    }

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

    public static AdServer getAdServer() {
        return adServer;
    }


    public static Host getHost() {
        return host;
    }

    public static boolean useLocalCache() {
        return useLocalCache;
    }

    /**
     * This method is used to
     * - Validate inputs of ad units
     * - Validate the setup of the demand adapter
     * - Start the bid manager
     *
     * @deprecated this method will be removed in the future, please use {@link #init(Context, ArrayList, String, AdServer)} instead for better performance
     */
    @Deprecated
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
        // set up cache manager
        CacheManager.init(context);
        // start ad requests
        BidManager.requestBidsForAdUnits(context, adUnits);
    }

    /**
     * This method is used to:
     * - Validate inputs of ad units
     * - Validate the setup of the demand adapter
     * - Start the bid manager
     *
     * @param context   Application context
     * @param adUnits   List of Ad Slot Configurations to register
     * @param accountId Prebid Server account
     * @param adServer  Primary AdServer you're using for you app
     * @throws PrebidException
     * @deprecated this method will be removed in the future, please use {@link #init(Context, ArrayList, String, AdServer, Host)} instead for better performance
     */
    @Deprecated
    public static void init(Context context, ArrayList<AdUnit> adUnits, String accountId, AdServer adServer) throws PrebidException {
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
        Prebid.adServer = adServer;
        if (AdServer.MOPUB.equals(Prebid.adServer)) {
            useLocalCache = false;
        }
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
        // set up cache manager
        CacheManager.init(context);
        // start ad requests
        BidManager.requestBidsForAdUnits(context, adUnits);
    }

    /**
     * This method is used to:
     * - Validate inputs of ad units
     * - Validate the setup of the demand adapter
     * - Start the bid manager
     *
     * @param context   Application context
     * @param adUnits   List of Ad Slot Configurations to register
     * @param accountId Prebid Server account
     * @param adServer  Primary AdServer you're using for your app
     * @param host      Host you're using for your app
     * @throws PrebidException
     */
    public static void init(Context context, ArrayList<AdUnit> adUnits, String accountId, AdServer adServer, Host host) throws PrebidException {
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
        Prebid.adServer = adServer;
        if (AdServer.MOPUB.equals(Prebid.adServer)) {
            Prebid.useLocalCache = false;
        }
        if (host == null)
            throw new PrebidException(PrebidException.PrebidError.NULL_HOST);
        Prebid.host = host;
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
        // set up cache manager
        CacheManager.init(context);
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
            if (adObj.getClass() == getClassFromString(MOPUB_ADVIEW_CLASS)
                    || adObj.getClass() == getClassFromString(MOPUB_INTERSTITIAL_CLASS)) {
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
            // check if the bid should be load in demand sdk or MoPub ad view/interstitial object
            boolean bidShouldBeLoadedInDemandSdk = false;
            String bidder = "";
            for (Pair<String, String> p : keywordPairs) {
                if (PREBID_CREATIVE_LOAD_TYPE.equals(p.first)) {
                    if (PREBID_DEMAND_SDK.equals(p.second)) {
                        bidShouldBeLoadedInDemandSdk = true;
                    }
                }
                if (PREBID_BIDDER.equals(p.first)) {
                    bidder = p.second;
                }
            }
            if (bidShouldBeLoadedInDemandSdk) {
                // pass bids that require demand sdk only when rendering is enabled
                // save the cache id locally on the ad object for later use
                Class mediation_adapter = getClassFromString(PREBID_MOPUB_CUSTOM_EVENT_BANNER);
                if (mediation_adapter != null && PrebidDemandSettings.isDemandEnabled(bidder)) {
                    for (Pair<String, String> p : keywordPairs) {
                        keywords.append(p.first).append(":").append(p.second).append(",");
                        if (PREBID_CACHE_ID.equals(p.first) || PREBID_BIDDER.equals(p.first)) {
                            Map<String, Object> localExtras = (Map) callMethodOnObject(adViewObj, "getLocalExtras");
                            Map newExtras = new HashMap();
                            if (localExtras != null) {
                                for (String s : localExtras.keySet()) {
                                    newExtras.put(s, localExtras.get(s));
                                }
                            }
                            newExtras.put(p.first, p.second);
                            try {
                                Method method = adViewObj.getClass().getMethod("setLocalExtras", Map.class);
                                method.invoke(adViewObj, newExtras);
                            } catch (Exception e) {
                                e.printStackTrace();
                                LogUtil.e("Client side demand won't be working since setting localExtras failed for MoPubView");
                            }
                        }
                    }
                }
            } else {
                // pass all bids that do not require demand sdk
                for (Pair<String, String> p : keywordPairs) {
                    keywords.append(p.first).append(":").append(p.second).append(",");
                }
            }
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
                boolean bidShouldBeLoadedInDemandSdk = false;
                String bidder = "";
                for (Pair<String, String> p : prebidKeywords) {
                    if (PREBID_CREATIVE_LOAD_TYPE.equals(p.first)) {
                        if (PREBID_DEMAND_SDK.equals(p.second)) {
                            bidShouldBeLoadedInDemandSdk = true;
                        }
                    }
                    if (PREBID_BIDDER.equals(p.first)) {
                        bidder = p.second;
                    }
                }
                if (bidShouldBeLoadedInDemandSdk) {
                    // pass bids that require demand sdk only when rendering is enabled
                    // save the cache id locally on the ad object for later use
                    Class mediation_adapter = null;
                    AdUnit adUnit = BidManager.getAdUnitByCode(adUnitCode);
                    if (AdType.BANNER.equals(adUnit.getAdType())) {
                        mediation_adapter = getClassFromString(PREBID_DFP_CUSTOM_EVENT_BANNER);
                    } else if (AdType.INTERSTITIAL.equals(adUnit.getAdType())) {
                        mediation_adapter = getClassFromString(PREBID_DFP_CUSTOM_EVENT_INTERSTITIAL);
                    }
                    if (mediation_adapter != null && PrebidDemandSettings.isDemandEnabled(bidder)) {
                        for (Pair<String, String> keywordPair : prebidKeywords) {
                            bundle.putString(keywordPair.first, keywordPair.second);
                            usedKeywordKeys.add(keywordPair.first);
                            // set custom event extras for the requested type
                            if (mediation_adapter != null) {
                                Bundle customEventExtras = (Bundle) callMethodOnObject(adRequestObj, "getCustomEventExtrasBundle", mediation_adapter);
                                if (customEventExtras != null) {
                                    if (PREBID_CACHE_ID.equals(keywordPair.first) || PREBID_BIDDER.equals(keywordPair.first)) {
                                        customEventExtras.putString(keywordPair.first, keywordPair.second);
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // pass all bids that do not require demand sdk
                    for (Pair<String, String> keywordPair : prebidKeywords) {
                        bundle.putString(keywordPair.first, keywordPair.second);
                        usedKeywordKeys.add(keywordPair.first);
                    }
                }
            }

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
        if (Prebid.adServer.equals(AdServer.MOPUB)) {
            Prebid.secureConnection = secureConnection;
        }
    }
}
