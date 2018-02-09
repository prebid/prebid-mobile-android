package org.prebid.demandsdkadapters.common;


import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;
import org.prebid.mobile.core.CacheManager;
import org.prebid.mobile.core.ErrorCode;
import org.prebid.mobile.core.LogUtil;
import org.prebid.mobile.core.PrebidDemandSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;

import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_ADLISTENER_INTERFACE;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_ADSIZE_BANNER_320_50;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_ADSIZE_BANNER_HEIGHT_50;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_ADSIZE_BANNER_HEIGHT_90;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_ADSIZE_CLASS;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_ADSIZE_RECTANGLE_HEIGHT_250;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_ADVIEW_CLASS;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_DESTROY_METHOD;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_DISABLE_AUTOREFRESH;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_GET_ERROR_CODE_METHOD;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_GET_ERROR_MESSAGE_METHOD;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_LOAD_AD_FROM_BID_METHOD;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_ON_AD_CLICKED_METHOD;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_ON_AD_LOADED_METHOD;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_ON_ERROR_METHOD;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_PLACEMENT_ID;
import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_SET_AD_LISTENER_METHOD;
import static org.prebid.mobile.core.PrebidDemandSettings.PREBID_ADM;

public class BannerController {
    private Object adObject;
    private PrebidDemandSettings.Demand demand;
    private String cacheId;

    public BannerController(String cacheId, PrebidDemandSettings.Demand demand) {
        this.demand = demand;
        this.cacheId = cacheId;
    }

    public void loadAd(final Context context, final int width, final int height, final AdListener listener) {
        LogUtil.i("Retrieving cached adm from server.");
        CacheManager.CacheListener cacheListener = new CacheManager.CacheListener() {
            @Override
            public void onResponded(JSONObject jsonObject) {
                switch (demand) {
                    case FACEBOOK:
                        LogUtil.i("Loading Facebook banner demand.");
                        loadFacebookBanner(context, jsonObject, width, height, listener);
                        break;
                }
            }
        };
        CacheManager.getCacheManager().getCache(cacheId, cacheListener);
    }

    private void loadFacebookBanner(Context context, JSONObject cacheResponse, int width, int height, final AdListener listener) {
        try {
            String adm = cacheResponse.getString(PREBID_ADM);
            JSONObject bid = new JSONObject(adm);
            String placementID = bid.getString(FACEBOOK_PLACEMENT_ID);
            Class<?> adViewClass = Class.forName(FACEBOOK_ADVIEW_CLASS);
            Class<?> adSizeClass = Class.forName(FACEBOOK_ADSIZE_CLASS);
            Object adSize = null;
            if (width == 320 && height == 50) {
                adSize = adSizeClass.getDeclaredField(FACEBOOK_ADSIZE_BANNER_320_50).get(null);
            } else if (height == 50) {
                adSize = adSizeClass.getDeclaredField(FACEBOOK_ADSIZE_BANNER_HEIGHT_50).get(null);
            } else if (height == 90) {
                adSize = adSizeClass.getDeclaredField(FACEBOOK_ADSIZE_BANNER_HEIGHT_90).get(null);
            } else if (height == 250) {
                adSize = adSizeClass.getDeclaredField(FACEBOOK_ADSIZE_RECTANGLE_HEIGHT_250).get(null);
            }
            if (adSize != null) {
                Constructor<?> adViewConstructor = adViewClass.getConstructor(Context.class, String.class, adSizeClass);
                adObject = adViewConstructor.newInstance(context, placementID, adSize);
                Method disableAutoRefresh = adViewClass.getMethod(FACEBOOK_DISABLE_AUTOREFRESH);
                disableAutoRefresh.invoke(adObject);
                ((Activity) context).getWindow().addContentView((View) adObject, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                ((View) adObject).setVisibility(View.GONE);
                Class<?> adListenerInterface = Class.forName(FACEBOOK_ADLISTENER_INTERFACE);
                Object newAdListener = Proxy.newProxyInstance(adListenerInterface.getClassLoader(), new Class<?>[]{adListenerInterface}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                        if (method.getName().equals(FACEBOOK_ON_ERROR_METHOD)) {
                            try {
                                Method getErrorCode = objects[1].getClass().getMethod(FACEBOOK_GET_ERROR_CODE_METHOD);
                                int errorCode = (int) getErrorCode.invoke(objects[1]);
                                Method getErrorMessage = objects[1].getClass().getMethod(FACEBOOK_GET_ERROR_MESSAGE_METHOD);
                                String errorMessage = (String) getErrorMessage.invoke(objects[1]);
                                LogUtil.i(String.format(Locale.ENGLISH, "Facebook demand failed to load because of %s, error code is %d.", errorMessage, errorCode));
                                switch (errorCode) {
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
                            LogUtil.i("Facebook demand ad loaded.");
                            ((ViewGroup) ((View) adObject).getParent()).removeView((View) adObject);
                            ((View) adObject).setVisibility(View.VISIBLE);
                            listener.onAdLoaded(adObject);
                        } else if (method.getName().equals(FACEBOOK_ON_AD_CLICKED_METHOD)) {
                            LogUtil.i("Facebook demand ad clicked.");
                            listener.onAdClicked(adObject);
                        }
                        return null;
                    }
                });
                Method setAdListener = adViewClass.getMethod(FACEBOOK_SET_AD_LISTENER_METHOD, adListenerInterface);
                setAdListener.invoke(adObject, newAdListener);
                Method loadAdFromBid = adViewClass.getMethod(FACEBOOK_LOAD_AD_FROM_BID_METHOD, String.class);
                loadAdFromBid.invoke(adObject, adm);
            } else {
                LogUtil.i("Facebook does not support ad heights other than 50, 90 and 250.");
                listener.onAdFailed(null, ErrorCode.INVALID_REQUEST);
            }
        } catch (Exception e) {
            LogUtil.i(String.format(Locale.ENGLISH, "Facebook demand failed to load because of %s.", e.getMessage()));
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
