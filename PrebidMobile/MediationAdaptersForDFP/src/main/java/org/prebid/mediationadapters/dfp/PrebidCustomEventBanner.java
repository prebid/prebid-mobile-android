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

public class PrebidCustomEventBanner implements CustomEventBanner {
    private String bidderName;
    private Object adObject;

    @Override
    public void requestBannerAd(Context context, CustomEventBannerListener customEventBannerListener, String s, AdSize adSize, MediationAdRequest mediationAdRequest, Bundle bundle) {
        Log.d("FB-Integration", "Load Prebid content for Facebook");
        if (bundle != null) {
            String cacheId = (String) bundle.get("hb_cache_id");
            bidderName = (String) bundle.get("hb_bidder");
            if ("audienceNetwork".equals(bidderName)) {
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
                    String adm = jsonObject.getString("adm");
                    JSONObject bid = new JSONObject(adm);
                    String placementID = bid.getString("placement_id");
                    Class<?> adViewClass = Class.forName("com.facebook.ads.AdView");
                    Class<?> adSizeClass = Class.forName("com.facebook.ads.AdSize");
                    Constructor<?> adSizeConstructor = adSizeClass.getConstructor(int.class, int.class);
                    Object adSize = adSizeConstructor.newInstance(dfpAdSize.getWidth(), dfpAdSize.getHeight());
                    Constructor<?> adViewConstructor = adViewClass.getConstructor(Context.class, String.class, adSizeClass);
                    adObject = adViewConstructor.newInstance(context, placementID, adSize);
                    Class<?> adListenerInterface = Class.forName("com.facebook.ads.AdListener");
                    Object newAdListener = Proxy.newProxyInstance(adListenerInterface.getClassLoader(), new Class<?>[]{adListenerInterface}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                            if (method.getName().equals("onError")) {
                                try {
                                    Method getErrorCode = objects[1].getClass().getMethod("getErrorCode");
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

                            } else if (method.getName().equals("onAdLoaded")) {
                                if (customEventBannerListener != null) {
                                    customEventBannerListener.onAdLoaded((View) adObject);
                                }
                            } else if (method.getName().equals("onAdClicked")) {
                                if (customEventBannerListener != null) {
                                    customEventBannerListener.onAdClicked();
                                }
                            }
                            return null;
                        }
                    });
                    Method setAdListener = adViewClass.getMethod("setAdListener", adListenerInterface);
                    setAdListener.invoke(adObject, newAdListener);
                    Method loadAdFromBid = adViewClass.getMethod("loadAdFromBid", String.class);
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
}
