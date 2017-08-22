package org.prebid.mediationadapters.dfp;


import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

import org.json.JSONObject;
import org.prebid.mobile.prebidserver.CacheService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class PrebidCustomEventInterstitial implements CustomEventInterstitial {
    private String bidderName;
    private Object adObject;

    @Override
    public void requestInterstitialAd(Context context, CustomEventInterstitialListener customEventInterstitialListener, String s, MediationAdRequest mediationAdRequest, Bundle bundle) {
        if (bundle != null) {
            String cacheId = (String) bundle.get("hb_cache_id");
            bidderName = (String) bundle.get("hb_bidder");
            if ("audienceNetwork".equals(bidderName)) {
                loadFacebookInterstitial(context, cacheId, customEventInterstitialListener);
            } else {
                if (customEventInterstitialListener != null) {
                    customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                }
            }
        } else {
            if (customEventInterstitialListener != null) {
                customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
            }
        }
    }

    @Override
    public void showInterstitial() {
        if ("audienceNetwork".equals(bidderName)) {
            try {
                adObject.getClass().getMethod("show").invoke(adObject);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onDestroy() {
        if ("audienceNetwork".equals(bidderName)) {
            try {
                adObject.getClass().getMethod("destroy").invoke(adObject);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

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
                                customEventInterstitialListener.onAdOpened();
                            } else if (method.getName().equals("onInterstitialDismissed")) {
                                customEventInterstitialListener.onAdClosed();
                            } else if (method.getName().equals("onError")) {
                                Method getErrorCode = objects[1].getClass().getMethod("getErrorCode");
                                switch ((int) getErrorCode.invoke(objects[1])) {
                                    case 1000:
                                        customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
                                        break;
                                    case 1001:
                                        customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                                        break;
                                    case 1002:
                                        customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
                                        break;
                                    case 2000:
                                        customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                                        break;
                                    case 2001:
                                        customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                                        break;
                                    case 3001:
                                        customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                                        break;
                                }
                            } else if (method.getName().equals("onAdLoaded")) {
                                customEventInterstitialListener.onAdLoaded();
                            } else if (method.getName().equals("onAdClicked")) {
                                customEventInterstitialListener.onAdClicked();
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
                        customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                    }
                }
            }
        }, cacheId);
        cs.execute();

    }
}
