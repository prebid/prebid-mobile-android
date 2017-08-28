package org.prebid.mediationadapters.dfp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

import org.json.JSONObject;
import org.prebid.mobile.prebidserver.CacheService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.prebid.mediationadapters.dfp.PrebidCustomEventSettings.*;

public class PrebidCustomEventBanner implements CustomEventBanner {
    private String bidderName;
    private Object adObject;

    @Override
    public void requestBannerAd(Context context, CustomEventBannerListener customEventBannerListener, String s, AdSize adSize, MediationAdRequest mediationAdRequest, Bundle bundle) {
        Log.d("FB-Integration", "Load Prebid content for Facebook");
        if (bundle != null) {
            String cacheId = (String) bundle.get(PREBID_CACHE_ID);
            bidderName = (String) bundle.get(PREBID_BIDDER);
            if (FACEBOOK_BIDDER_NAME.equals(bidderName) && demandSet.contains(PrebidCustomEventSettings.Demand.FACEBOOK)) {
                loadFacebookBanner(context, cacheId, adSize, customEventBannerListener);
            } else {
                if (customEventBannerListener != null) {
                    customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                }
            }
        } else {
            if (customEventBannerListener != null) {
                customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
            }
        }
    }

    private void loadFacebookBanner(final Context context, String cacheId, final AdSize dfpAdSize, final CustomEventBannerListener customEventBannerListener) {
        CacheService cs = new CacheService(new CacheService.CacheListener() {
            @Override
            public void onResponded(JSONObject jsonObject) {
                try {
                    String adm = jsonObject.getString(PREBID_ADM);
                    JSONObject bid = new JSONObject(adm);
                    String placementID = bid.getString(FACEBOOK_PLACEMENT_ID);
                    Class<?> adViewClass = Class.forName(FACEBOOK_ADVIEW_CLASS);
                    Class<?> adSizeClass = Class.forName(FACEBOOK_ADSIZE_CLASS);
                    Constructor<?> adSizeConstructor = adSizeClass.getConstructor(int.class, int.class);
                    Object adSize = adSizeConstructor.newInstance(dfpAdSize.getWidth(), dfpAdSize.getHeight());
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
                                            customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
                                            break;
                                        case 1001:
                                            customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                                            break;
                                        case 1002:
                                            customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
                                            break;
                                        case 2000:
                                            customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                                            break;
                                        case 2001:
                                            customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                                            break;
                                        case 3001:
                                            customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                                            break;
                                    }
                                } catch (Exception e) {

                                }

                            } else if (method.getName().equals(FACEBOOK_ON_AD_LOADED_METHOD)) {
                                if (customEventBannerListener != null) {
                                    customEventBannerListener.onAdLoaded((View) adObject);
                                }
                            } else if (method.getName().equals(FACEBOOK_ON_AD_CLICKED_METHOD)) {
                                if (customEventBannerListener != null) {
                                    customEventBannerListener.onAdClicked();
                                }
                            }
                            return null;
                        }
                    });
                    Method setAdListener = adViewClass.getMethod(FACEBOOK_SET_AD_LISTENER_METHOD, adListenerInterface);
                    setAdListener.invoke(adObject, newAdListener);
                    Method loadAdFromBid = adViewClass.getMethod(FACEBOOK_LOAD_AD_FROM_BID_METHOD, String.class);
                    loadAdFromBid.invoke(adObject, adm);
                } catch (Exception e) {
                    if (customEventBannerListener != null) {
                        customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                    }
                }
            }
        }, cacheId);
        cs.execute();
    }

    // Google custom event banner implementation
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
}
