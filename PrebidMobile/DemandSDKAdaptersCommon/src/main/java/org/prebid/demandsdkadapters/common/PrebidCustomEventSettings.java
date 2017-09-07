package org.prebid.demandsdkadapters.common;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;

public class PrebidCustomEventSettings {
    // Prebid constants
    public static final String PREBID_CACHE_ID = "hb_cache_id";
    public static final String PREBID_BIDDER = "hb_bidder";
    public static final String PREBID_ADM = "adm";
    // MoPub constants
    public static final String MOPUB_WIDTH = "com_mopub_ad_width";
    public static final String MOPUB_HEIGHT = "com_mopub_ad_height";
    // Facebook constants
    public static final String FACEBOOK_PLACEMENT_ID = "placement_id";
    public static final String FACEBOOK_BIDDER_NAME = "audienceNetwork";
    public static final String FACEBOOK_ADVIEW_CLASS = "com.facebook.ads.AdView";
    public static final String FACEBOOK_ADSIZE_CLASS = "com.facebook.ads.AdSize";
    public static final String FACEBOOK_ADLISTENER_INTERFACE = "com.facebook.ads.AdListener";
    public static final String FACEBOOK_AD_INTERFACE = "com.facebook.ads.Ad";
    public static final String FACEBOOK_ADERROR_CLASS = "com.facebook.ads.AdError";
    public static final String FACEBOOK_INTERSTITIAL_CLASS = "com.facebook.ads.InterstitialAd";
    public static final String FACEBOOK_INTERSTITIAL_ADLISTENER_INTERFACE = "com.facebook.ads.InterstitialAdListener";
    public static final String FACEBOOK_ON_ERROR_METHOD = "onError";
    public static final String FACEBOOK_ON_AD_LOADED_METHOD = "onAdLoaded";
    public static final String FACEBOOK_ON_AD_CLICKED_METHOD = "onAdClicked";
    public static final String FACEBOOK_ON_INTERSTITIAL_DISPLAYED_METHOD = "onInterstitialDisplayed";
    public static final String FACEBOOK_ON_INTERSTITIAL_DISMISSED_METHOD = "onInterstitialDismissed";
    public static final String FACEBOOK_GET_ERROR_CODE_METHOD = "getErrorCode";
    public static final String FACEBOOK_SET_AD_LISTENER_METHOD = "setAdListener";
    public static final String FACEBOOK_LOAD_AD_FROM_BID_METHOD = "loadAdFromBid";
    public static final String FACEBOOK_DESTROY_METHOD = "destroy";
    public static final String FACEBOOK_SHOW_METHOD = "show";

    // Demand constants
    public enum Demand {
        FACEBOOK
    }

    static HashSet<Demand> demandSet = new HashSet<Demand>();

    public static boolean isDemandEnabled(Demand demand) {
        return demandSet.contains(demand);
    }

    public static void enableDemand(Demand demand) throws Exception {
        String errorMessage = "Prebid SDK uses %s that's not compatible with the version you're using.";
        switch (demand) {
            case FACEBOOK:
                try {
                    Class adViewClass = Class.forName(FACEBOOK_ADVIEW_CLASS);
                    Class adSizeClass = Class.forName(FACEBOOK_ADSIZE_CLASS);
                    Constructor<?> adSizeConstructor = adSizeClass.getConstructor(int.class, int.class);
                    Constructor adViewContructor = adViewClass.getConstructor(Context.class, String.class, adSizeClass);
                    Class<?> adListenerInterface = Class.forName(FACEBOOK_ADLISTENER_INTERFACE);
                    Class<?> adClass = Class.forName(FACEBOOK_AD_INTERFACE);
                    Class<?> adErrorClass = Class.forName(FACEBOOK_ADERROR_CLASS);
                    Method onError = adListenerInterface.getMethod(FACEBOOK_ON_ERROR_METHOD, adClass, adErrorClass);
                    Method onAdLoaded = adListenerInterface.getMethod(FACEBOOK_ON_AD_LOADED_METHOD, adClass);
                    Method onAdClicked = adListenerInterface.getMethod(FACEBOOK_ON_AD_CLICKED_METHOD, adClass);
                    Method getErrorCode = adErrorClass.getMethod(FACEBOOK_GET_ERROR_CODE_METHOD);
                    Method setAdListener = adViewClass.getMethod(FACEBOOK_SET_AD_LISTENER_METHOD, adListenerInterface);
                    Method loadAdFromBid = adViewClass.getMethod(FACEBOOK_LOAD_AD_FROM_BID_METHOD, String.class);
                    Method destroy = adViewClass.getMethod(FACEBOOK_DESTROY_METHOD);
                    Class<?> interstitialClass = Class.forName(FACEBOOK_INTERSTITIAL_CLASS);
                    Constructor<?> interstitialContructor = interstitialClass.getConstructor(Context.class, String.class);
                    Class<?> interstitialAdListenerInterface = Class.forName(FACEBOOK_INTERSTITIAL_ADLISTENER_INTERFACE);
                    onError = interstitialAdListenerInterface.getMethod(FACEBOOK_ON_ERROR_METHOD, adClass, adErrorClass);
                    onAdLoaded = interstitialAdListenerInterface.getMethod(FACEBOOK_ON_AD_LOADED_METHOD, adClass);
                    onAdClicked = interstitialAdListenerInterface.getMethod(FACEBOOK_ON_AD_CLICKED_METHOD, adClass);
                    Method onInterstitialDisplayed = interstitialAdListenerInterface.getMethod(FACEBOOK_ON_INTERSTITIAL_DISPLAYED_METHOD, adClass);
                    Method onInterstitialDismissed = interstitialAdListenerInterface.getMethod(FACEBOOK_ON_INTERSTITIAL_DISMISSED_METHOD, adClass);
                    setAdListener = interstitialClass.getMethod(FACEBOOK_SET_AD_LISTENER_METHOD, interstitialAdListenerInterface);
                    loadAdFromBid = interstitialClass.getMethod(FACEBOOK_LOAD_AD_FROM_BID_METHOD, String.class);
                    destroy = interstitialClass.getMethod(FACEBOOK_DESTROY_METHOD);
                    Method show = interstitialClass.getMethod(FACEBOOK_SHOW_METHOD);
                } catch (Exception e) {
                    throw new Exception(String.format(Locale.ENGLISH, errorMessage, "facebook audience network sdk"));
                }
                break;
        }
        demandSet.add(demand);
    }

}

