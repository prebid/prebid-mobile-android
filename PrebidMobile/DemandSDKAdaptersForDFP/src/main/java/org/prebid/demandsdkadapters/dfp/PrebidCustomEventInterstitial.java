package org.prebid.demandsdkadapters.dfp;


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

import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.*;

public class PrebidCustomEventInterstitial implements CustomEventInterstitial {
    private String bidderName;
    private Object adObject;

    @Override
    public void requestInterstitialAd(Context context, CustomEventInterstitialListener customEventInterstitialListener, String s, MediationAdRequest mediationAdRequest, Bundle bundle) {
        if (bundle != null) {
            String cacheId = (String) bundle.get(PREBID_CACHE_ID);
            bidderName = (String) bundle.get(PREBID_BIDDER);
            if (FACEBOOK_BIDDER_NAME.equals(bidderName) && demandSet.contains(PrebidCustomEventSettings.Demand.FACEBOOK)) {
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
        if (FACEBOOK_BIDDER_NAME.equals(bidderName)) {
            try {
                adObject.getClass().getMethod(FACEBOOK_SHOW_METHOD).invoke(adObject);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onDestroy() {
        if (FACEBOOK_BIDDER_NAME.equals(bidderName)) {
            try {
                adObject.getClass().getMethod(FACEBOOK_DESTROY_METHOD).invoke(adObject);
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

                    String adm = jsonObject.getString(PREBID_ADM);
                    JSONObject bid = new JSONObject(adm);
                    String placementID = bid.getString(FACEBOOK_PLACEMENT_ID);
                    Class<?> interstitialClass = Class.forName(FACEBOOK_INTERSTITIAL_CLASS);
                    Constructor<?> interstitialContructor = interstitialClass.getConstructor(Context.class, String.class);
                    adObject = interstitialContructor.newInstance(context, placementID);
                    Class<?> adLisenterInterface = Class.forName(FACEBOOK_INTERSTITIAL_ADLISTENER_INTERFACE);
                    Object newAdListener = Proxy.newProxyInstance(adLisenterInterface.getClassLoader(), new Class[]{adLisenterInterface}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                            if (method.getName().equals(FACEBOOK_ON_INTERSTITIAL_DISPLAYED_METHOD)) {
                                customEventInterstitialListener.onAdOpened();
                            } else if (method.getName().equals(FACEBOOK_ON_INTERSTITIAL_DISMISSED_METHOD)) {
                                customEventInterstitialListener.onAdClosed();
                            } else if (method.getName().equals(FACEBOOK_ON_ERROR_METHOD)) {
                                Method getErrorCode = objects[1].getClass().getMethod(FACEBOOK_GET_ERROR_CODE_METHOD);
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
                            } else if (method.getName().equals(FACEBOOK_ON_AD_LOADED_METHOD)) {
                                customEventInterstitialListener.onAdLoaded();
                            } else if (method.getName().equals(FACEBOOK_ON_AD_CLICKED_METHOD)) {
                                customEventInterstitialListener.onAdClicked();
                            }
                            return null;
                        }
                    });
                    Method setAdListener = interstitialClass.getMethod(FACEBOOK_SET_AD_LISTENER_METHOD, adLisenterInterface);
                    setAdListener.invoke(adObject, newAdListener);
                    Method loadAdFromBid = interstitialClass.getMethod(FACEBOOK_LOAD_AD_FROM_BID_METHOD, String.class);
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
