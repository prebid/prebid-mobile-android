package org.prebid.demandsdkadapters.common;

import android.content.Context;

import org.json.JSONObject;
import org.prebid.mobile.core.ErrorCode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_DESTROY_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_GET_ERROR_CODE_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_INTERSTITIAL_ADLISTENER_INTERFACE;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_INTERSTITIAL_CLASS;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_LOAD_AD_FROM_BID_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_ON_AD_CLICKED_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_ON_AD_LOADED_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_ON_ERROR_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_ON_INTERSTITIAL_DISMISSED_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_ON_INTERSTITIAL_DISPLAYED_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_PLACEMENT_ID;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_SET_AD_LISTENER_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_SHOW_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.PREBID_ADM;

public class InterstitialController {
    private Object adObject;
    private PrebidCustomEventSettings.Demand demand;
    private String cacheId;

    public InterstitialController(PrebidCustomEventSettings.Demand demand, String cacheId) {
        this.demand = demand;
        this.cacheId = cacheId;
    }

    public void loadAd(final Context context, final AdListener listener) {
        CacheService cs = new CacheService(new CacheService.CacheListener() {
            @Override
            public void onResponded(JSONObject jsonObject) {
                switch (demand) {
                    case FACEBOOK:
                        loadFacebookIntersitial(context, jsonObject, listener);
                        break;
                }
            }
        }, cacheId);
        cs.execute();
    }

    private void loadFacebookIntersitial(Context context, JSONObject cacheResponse, final AdListener listener) {
        try {
            String adm = cacheResponse.getString(PREBID_ADM);
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
                        listener.onInterstitialShown(adObject);
                    } else if (method.getName().equals(FACEBOOK_ON_INTERSTITIAL_DISMISSED_METHOD)) {
                        listener.onInterstitialClosed(adObject);
                    } else if (method.getName().equals(FACEBOOK_ON_ERROR_METHOD)) {
                        Method getErrorCode = objects[1].getClass().getMethod(FACEBOOK_GET_ERROR_CODE_METHOD);
                        switch ((int) getErrorCode.invoke(objects[1])) {
                            case 1000:
                                listener.onAdFailed(adObject, ErrorCode.NETWORK_ERROR);
                                break;
                            case 1001:
                                listener.onAdFailed(adObject, ErrorCode.NO_BIDS);
                                break;
                            case 1002:
                                listener.onAdFailed(adObject, ErrorCode.INVALID_REQUEST);
                                break;
                            case 2000:
                                listener.onAdFailed(adObject, ErrorCode.INTERNAL_ERROR);
                                break;
                            case 2001:
                                listener.onAdFailed(adObject, ErrorCode.INTERNAL_ERROR);
                                break;
                            case 3001:
                                listener.onAdFailed(adObject, ErrorCode.INTERNAL_ERROR);
                                break;
                            default:
                                listener.onAdFailed(adObject, ErrorCode.INTERNAL_ERROR);
                                break;
                        }
                    } else if (method.getName().equals(FACEBOOK_ON_AD_LOADED_METHOD)) {
                        listener.onAdLoaded(adObject);
                    } else if (method.getName().equals(FACEBOOK_ON_AD_CLICKED_METHOD)) {
                        listener.onAdClicked(adObject);
                    }
                    return null;
                }
            });
            Method setAdListener = interstitialClass.getMethod(FACEBOOK_SET_AD_LISTENER_METHOD, adLisenterInterface);
            setAdListener.invoke(adObject, newAdListener);
            Method loadAdFromBid = interstitialClass.getMethod(FACEBOOK_LOAD_AD_FROM_BID_METHOD, String.class);
            loadAdFromBid.invoke(adObject, adm);
        } catch (Exception e) {
            if (listener != null) {
                listener.onAdFailed(adObject, ErrorCode.INTERNAL_ERROR);
            }
        }
    }

    public void show() {
        switch (demand) {
            case FACEBOOK:
                try {
                    adObject.getClass().getMethod(FACEBOOK_SHOW_METHOD).invoke(adObject);
                } catch (Exception e) {
                }
                break;
        }
    }

    public void destroy() {
        switch (demand) {
            case FACEBOOK:
                try {
                    adObject.getClass().getMethod(FACEBOOK_DESTROY_METHOD).invoke(adObject);
                } catch (Exception e) {
                }
                break;
        }
    }
}

