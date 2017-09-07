package org.prebid.demandsdkadapters.common;


import android.content.Context;

import org.json.JSONObject;
import org.prebid.mobile.core.ErrorCode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_ADLISTENER_INTERFACE;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_ADSIZE_CLASS;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_ADVIEW_CLASS;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_DESTROY_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_GET_ERROR_CODE_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_LOAD_AD_FROM_BID_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_ON_AD_CLICKED_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_ON_AD_LOADED_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_ON_ERROR_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_PLACEMENT_ID;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_SET_AD_LISTENER_METHOD;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.PREBID_ADM;

public class BannerController {
    private Object adObject;
    private PrebidCustomEventSettings.Demand demand;
    private String cacheId;

    public BannerController(String cacheId, PrebidCustomEventSettings.Demand demand) {
        this.demand = demand;
        this.cacheId = cacheId;
    }

    public void loadAd(final Context context, final int width, final int height, final AdListener listener) {
        CacheService cs = new CacheService(new CacheService.CacheListener() {
            @Override
            public void onResponded(JSONObject jsonObject) {
                switch (demand) {
                    case FACEBOOK:
                        loadFacebookBanner(context, jsonObject, width, height, listener);
                        break;
                }
            }
        }, cacheId);
        cs.execute();

    }

    private void loadFacebookBanner(Context context, JSONObject cacheResponse, int width, int height, final AdListener listener) { // todo retrieve this from cache response
        try {
            String adm = cacheResponse.getString(PREBID_ADM);
            // todo remove hack
            adm = "{\"type\":\"ID\",\"bid_id\":\"5684915014742322033\",\"placement_id\":\"1959066997713356_1959836684303054\",\"resolved_placement_id\":\"1959066997713356_1959836684303054\",\"sdk_version\":\"4.26.1-alpha-AppNexus\",\"device_id\":\"19e7a8e3-4544-49f4-bfb1-99370ecfbc73\",\"template\":7,\"payload\":\"null\"}";
            JSONObject bid = new JSONObject(adm);
            String placementID = bid.getString(FACEBOOK_PLACEMENT_ID);
            Class<?> adViewClass = Class.forName(FACEBOOK_ADVIEW_CLASS);
            Class<?> adSizeClass = Class.forName(FACEBOOK_ADSIZE_CLASS);
            Constructor<?> adSizeConstructor = adSizeClass.getConstructor(int.class, int.class);
            // todo add this back when facebook fixes it
//                    Object adSize = adSizeConstructor.newInstance(dfpAdSize.getWidth(), dfpAdSize.getHeight());
            Object adSize = adSizeConstructor.newInstance(-1, height);
            Constructor<?> adViewConstructor = adViewClass.getConstructor(Context.class, String.class, adSizeClass);
            adObject = adViewConstructor.newInstance(context, placementID, adSize);
            Class<?> adListenerInterface = Class.forName(FACEBOOK_ADLISTENER_INTERFACE);
            Object newAdListener = Proxy.newProxyInstance(adListenerInterface.getClassLoader(), new Class<?>[]{adListenerInterface}, new InvocationHandler() {
                @Override
                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                    if (method.getName().equals(FACEBOOK_ON_ERROR_METHOD)) {
                        try {
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
                        } catch (Exception e) {

                        }

                    } else if (method.getName().equals(FACEBOOK_ON_AD_LOADED_METHOD)) {
                        listener.onAdLoaded(adObject);
                    } else if (method.getName().equals(FACEBOOK_ON_AD_CLICKED_METHOD)) {
                        listener.onAdClicked(adObject);
                    }
                    return null;
                }
            });
            Method setAdListener = adViewClass.getMethod(FACEBOOK_SET_AD_LISTENER_METHOD, adListenerInterface);
            setAdListener.invoke(adObject, newAdListener);
            Method loadAdFromBid = adViewClass.getMethod(FACEBOOK_LOAD_AD_FROM_BID_METHOD, String.class);
            loadAdFromBid.invoke(adObject, adm);
        } catch (Exception e) {
            if (listener != null) {
                listener.onAdFailed(adObject, ErrorCode.INTERNAL_ERROR);
            }
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
