package org.prebid.mediationadapters.mopub;


import android.content.Context;

import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;

import org.json.JSONObject;
import org.prebid.mobile.prebidserver.CacheService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class PrebidCustomEventInterstitial extends CustomEventInterstitial {
    private String bidder;
    private Object adObject;

    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        if (localExtras != null) {
            String cache_id = (String) localExtras.get("hb_cache_id");
            bidder = (String) localExtras.get("hb_bidder");
//            if ("audienceNetwork".equals(bidder)) {
            if ("appnexus".equals(bidder)) {
                loadFacebookInterstitial(context, cache_id, customEventInterstitialListener);
            } else {
                if (customEventInterstitialListener != null) {
                    customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_FILL);
                }
            }
        } else {
            if (customEventInterstitialListener != null) {
                customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
            }
        }
    }

    @Override
    protected void showInterstitial() {
        if ("audienceNetwork".equals(bidder)) {
            try {
                adObject.getClass().getMethod("show").invoke(adObject);
            } catch (Exception e) {
            }
        }

    }

    @Override
    protected void onInvalidate() {
        if ("audienceNetwork".equals(bidder)) {
            try {
                adObject.getClass().getMethod("destroy").invoke(adObject);
            } catch (Exception e) {
            }
        }
    }

    private void loadFacebookInterstitial(final Context context, String cacheId, final CustomEventInterstitialListener customEventInterstitialListener) {
        CacheService cs = new CacheService(new CacheService.CacheListener() {
            @Override
            public void onResponded(JSONObject jsonObject) {
                try {
                    String adm = jsonObject.getString("adm");
                    JSONObject bid = new JSONObject(adm);
                    String placementID = bid.getString("placement_id");
                    Class<?> interstitialClass = Class.forName("com.facebook.ads.InterstitialAd");
                    Constructor<?> interstitialContructor = interstitialClass.getConstructor(Context.class, String.class);
                    adObject = interstitialContructor.newInstance(context, placementID);
                    Class<?> adLisenterInterface = Class.forName("com.facebook.ads.InterstitialAdListener");
                    Object newAdListener = Proxy.newProxyInstance(adLisenterInterface.getClassLoader(), new Class[]{adLisenterInterface}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                            if (method.getName().equals("onInterstitialDisplayed")) {
                                customEventInterstitialListener.onInterstitialShown();
                            } else if (method.getName().equals("onInterstitialDismissed")) {
                                customEventInterstitialListener.onInterstitialDismissed();
                            } else if (method.getName().equals("onError")) {
                                Method getErrorCode = objects[1].getClass().getMethod("getErrorCode");
                                switch ((int) getErrorCode.invoke(objects[1])) {
                                    case 1000:
                                        customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_CONNECTION);
                                        break;
                                    case 1001:
                                        customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_FILL);
                                        break;
                                    case 1002:
                                        customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
                                        break;
                                    case 2000:
                                        customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.SERVER_ERROR);
                                        break;
                                    case 2001:
                                        customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
                                        break;
                                    case 3001:
                                        customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
                                        break;
                                }
                            } else if (method.getName().equals("onAdLoaded")) {
                                customEventInterstitialListener.onInterstitialLoaded();
                            } else if (method.getName().equals("onAdClicked")) {
                                customEventInterstitialListener.onInterstitialClicked();
                            }
                            return null;
                        }
                    });
                    Method setAdListener = interstitialClass.getMethod("setAdListener", adLisenterInterface);
                    setAdListener.invoke(adObject, newAdListener);
                    Method loadAdFromBid = interstitialClass.getMethod("loadAdFromBid", String.class);
                    loadAdFromBid.invoke(adObject, adm);
                } catch (Exception e) {
                    if (customEventInterstitialListener != null) {
                        customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
                    }
                }
            }
        }, cacheId);
        cs.execute();
    }
}
