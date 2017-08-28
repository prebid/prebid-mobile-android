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

import static org.prebid.mediationadapters.mopub.PrebidCustomEventSettings.*;

public class PrebidCustomEventInterstitial extends CustomEventInterstitial {
    private String bidder;
    private Object adObject;

    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        if (localExtras != null) {
            String cache_id = (String) localExtras.get(PREBID_CACHE_ID);
            bidder = (String) localExtras.get(PREBID_BIDDER);
            if (FACEBOOK_BIDDER_NAME.equals(bidder) && demandSet.contains(PrebidCustomEventSettings.Demand.FACEBOOK)) {
//            if ("appnexus".equals(bidder)) {
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
        if (FACEBOOK_BIDDER_NAME.equals(bidder)) {
            try {
                adObject.getClass().getMethod(FACEBOOK_SHOW_METHOD).invoke(adObject);
            } catch (Exception e) {
            }
        }

    }

    @Override
    protected void onInvalidate() {
        if (FACEBOOK_BIDDER_NAME.equals(bidder)) {
            try {
                adObject.getClass().getMethod(FACEBOOK_DESTROY_METHOD).invoke(adObject);
            } catch (Exception e) {
            }
        }
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
                                customEventInterstitialListener.onInterstitialShown();
                            } else if (method.getName().equals(FACEBOOK_ON_INTERSTITIAL_DISMISSED_METHOD)) {
                                customEventInterstitialListener.onInterstitialDismissed();
                            } else if (method.getName().equals(FACEBOOK_ON_ERROR_METHOD)) {
                                Method getErrorCode = objects[1].getClass().getMethod(FACEBOOK_GET_ERROR_CODE_METHOD);
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
                            } else if (method.getName().equals(FACEBOOK_ON_AD_LOADED_METHOD)) {
                                customEventInterstitialListener.onInterstitialLoaded();
                            } else if (method.getName().equals(FACEBOOK_ON_AD_CLICKED_METHOD)) {
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
