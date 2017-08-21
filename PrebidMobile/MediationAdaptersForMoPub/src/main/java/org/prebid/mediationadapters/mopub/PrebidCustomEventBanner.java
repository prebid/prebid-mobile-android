package org.prebid.mediationadapters.mopub;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;

import org.json.JSONObject;
import org.prebid.mobile.prebidserver.CacheService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class PrebidCustomEventBanner extends CustomEventBanner {
    private String bidderName;
    private Object adObject;

    @Override
    protected void loadBanner(final Context context, final CustomEventBannerListener customEventBannerListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        Log.d("FB-Integration", "Load Prebid content for Facebook");
        if (localExtras != null) {
            String cache_id = (String) localExtras.get("hb_cache_id");
            bidderName = (String) localExtras.get("hb_bidder");
            int width = (int) localExtras.get("com_mopub_ad_width");
            int height = (int) localExtras.get("com_mopub_ad_height");
            if ("audienceNetwork".equals(bidderName)) {
                loadFacebookBanner(context, cache_id, width, height, customEventBannerListener);
            } else {
                if (customEventBannerListener != null) {
                    customEventBannerListener.onBannerFailed(MoPubErrorCode.NO_FILL);
                }
            }
        } else {
            if (customEventBannerListener != null) {
                customEventBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
        }
    }

    @Override
    protected void onInvalidate() {
        if ("audienceNetwork".equals(bidderName)) {
            try {
                adObject.getClass().getMethod("destroy").invoke(adObject);
            } catch (Exception e) {
            }
        }
    }

    private void loadFacebookBanner(final Context context, final String cache_id, final int width, final int height, final CustomEventBannerListener customEventBannerListener) {
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
                    Object adSize = adSizeConstructor.newInstance(width, height);
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
                                            customEventBannerListener.onBannerFailed(MoPubErrorCode.NO_CONNECTION);
                                            break;
                                        case 1001:
                                            customEventBannerListener.onBannerFailed(MoPubErrorCode.NO_FILL);
                                            break;
                                        case 1002:
                                            customEventBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
                                            break;
                                        case 2000:
                                            customEventBannerListener.onBannerFailed(MoPubErrorCode.SERVER_ERROR);
                                            break;
                                        case 2001:
                                            customEventBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
                                            break;
                                        case 3001:
                                            customEventBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
                                            break;
                                    }
                                } catch (Exception e) {

                                }

                            } else if (method.getName().equals("onAdLoaded")) {
                                if (customEventBannerListener != null) {
                                    customEventBannerListener.onBannerLoaded((View) adObject);
                                }
                            } else if (method.getName().equals("onAdClicked")) {
                                if (customEventBannerListener != null) {
                                    customEventBannerListener.onBannerClicked();
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
                        customEventBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
                    }
                }
            }
        }, cache_id);
        cs.execute();
    }
}
