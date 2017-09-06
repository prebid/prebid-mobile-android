package org.prebid.demandsdkadapters.dfp;

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

import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.FACEBOOK_ADLISTENER_INTERFACE;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.FACEBOOK_ADSIZE_CLASS;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.FACEBOOK_ADVIEW_CLASS;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.FACEBOOK_BIDDER_NAME;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.FACEBOOK_DESTROY_METHOD;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.FACEBOOK_GET_ERROR_CODE_METHOD;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.FACEBOOK_LOAD_AD_FROM_BID_METHOD;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.FACEBOOK_ON_AD_CLICKED_METHOD;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.FACEBOOK_ON_AD_LOADED_METHOD;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.FACEBOOK_ON_ERROR_METHOD;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.FACEBOOK_PLACEMENT_ID;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.FACEBOOK_SET_AD_LISTENER_METHOD;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.PREBID_ADM;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.PREBID_BIDDER;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.PREBID_CACHE_ID;
import static org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings.demandSet;

public class PrebidCustomEventBanner implements CustomEventBanner {
    private String bidderName;
    private Object adObject;

    @Override
    public void requestBannerAd(Context context, CustomEventBannerListener customEventBannerListener, String s, AdSize adSize, MediationAdRequest mediationAdRequest, Bundle bundle) {
        Log.d("FB-Integration", "Load Prebid content for Facebook");
        if (bundle != null) {
            String cacheId = (String) bundle.get(PREBID_CACHE_ID);
            bidderName = (String) bundle.get(PREBID_BIDDER);
            if ("appnexus".equals(bidderName) && demandSet.contains(PrebidCustomEventSettings.Demand.FACEBOOK)) {
//            if (FACEBOOK_BIDDER_NAME.equals(bidderName) && demandSet.contains(PrebidCustomEventSettings.Demand.FACEBOOK)) {
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
                    // todo remove hack
                    adm = "{\"type\":\"ID\",\"bid_id\":\"5684915014742322033\",\"placement_id\":\"1959066997713356_1959836684303054\",\"resolved_placement_id\":\"1959066997713356_1959836684303054\",\"sdk_version\":\"4.26.1-alpha-AppNexus\",\"device_id\":\"19e7a8e3-4544-49f4-bfb1-99370ecfbc73\",\"template\":7,\"payload\":\"null\"}";
                    JSONObject bid = new JSONObject(adm);
                    String placementID = bid.getString(FACEBOOK_PLACEMENT_ID);
                    Class<?> adViewClass = Class.forName(FACEBOOK_ADVIEW_CLASS);
                    Class<?> adSizeClass = Class.forName(FACEBOOK_ADSIZE_CLASS);
                    Constructor<?> adSizeConstructor = adSizeClass.getConstructor(int.class, int.class);
                    // todo add this back when facebook fixes it
//                    Object adSize = adSizeConstructor.newInstance(dfpAdSize.getWidth(), dfpAdSize.getHeight());
                    Object adSize = adSizeConstructor.newInstance(-1, dfpAdSize.getHeight());
                    Constructor<?> adViewConstructor = adViewClass.getConstructor(Context.class, String.class, adSizeClass);
                    adObject = adViewConstructor.newInstance(context, placementID, adSize);
                    Class<?> adListenerInterface = Class.forName(FACEBOOK_ADLISTENER_INTERFACE);
                    Object newAdListener = Proxy.newProxyInstance(adListenerInterface.getClassLoader(), new Class<?>[]{adListenerInterface}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                            if (method.getName().equals(FACEBOOK_ON_ERROR_METHOD)) {
                                try {
                                    Method getErrorCode = objects[1].getClass().getMethod(FACEBOOK_GET_ERROR_CODE_METHOD);
                                    // todo remove this
                                    Method getErrorMessage = objects[1].getClass().getMethod("getErrorMessage");
                                    String errorMessage = (String) getErrorMessage.invoke(objects[1]);
                                    int code = (int) getErrorCode.invoke(objects[1]);
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
                                        default:
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
