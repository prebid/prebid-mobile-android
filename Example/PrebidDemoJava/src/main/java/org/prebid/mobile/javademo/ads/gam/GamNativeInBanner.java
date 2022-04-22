package org.prebid.mobile.javademo.ads.gam;

import android.util.Log;
import android.view.ViewGroup;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import org.prebid.mobile.*;

import java.util.ArrayList;

public class GamNativeInBanner {

    private static final String TAG = GamNativeInBanner.class.getSimpleName();

    private static NativeAdUnit nativeAdUnit;

    public static void create(
        ViewGroup wrapper,
        String adUnitId,
        String configId,
        int autoRefreshTime
    ) {
        nativeAdUnit = new NativeAdUnit(configId);
        configureNativeAdUnit(nativeAdUnit);

        final AdManagerAdView gamView = new AdManagerAdView(wrapper.getContext());
        gamView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "On ad loaded");
            }
        });
        gamView.setAdUnitId(adUnitId);
        gamView.setAdSizes(AdSize.FLUID);

        wrapper.removeAllViews();
        wrapper.addView(gamView);

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();

        nativeAdUnit.setAutoRefreshInterval(autoRefreshTime);
        nativeAdUnit.fetchDemand(builder, resultCode -> {
            AdManagerAdRequest request = builder.build();
            gamView.loadAd(request);
        });
    }

    public static void destroy() {
        if (nativeAdUnit != null) {
            nativeAdUnit.stopAutoRefresh();
            nativeAdUnit = null;
        }
    }


    private static void configureNativeAdUnit(NativeAdUnit adUnit) {
        adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);

        try {
            NativeEventTracker tracker = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods);
            adUnit.addEventTracker(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }
        NativeTitleAsset title = new NativeTitleAsset();
        title.setLength(90);
        title.setRequired(true);
        adUnit.addAsset(title);
        NativeImageAsset icon = new NativeImageAsset(20, 20, 20, 20);
        icon.setImageType(NativeImageAsset.IMAGE_TYPE.ICON);
        icon.setRequired(true);
        adUnit.addAsset(icon);
        NativeImageAsset image = new NativeImageAsset(200, 200, 200, 200);
        image.setImageType(NativeImageAsset.IMAGE_TYPE.MAIN);
        image.setRequired(true);
        adUnit.addAsset(image);
        NativeDataAsset data = new NativeDataAsset();
        data.setLen(90);
        data.setDataType(NativeDataAsset.DATA_TYPE.SPONSORED);
        data.setRequired(true);
        adUnit.addAsset(data);
        NativeDataAsset body = new NativeDataAsset();
        body.setRequired(true);
        body.setDataType(NativeDataAsset.DATA_TYPE.DESC);
        adUnit.addAsset(body);
        NativeDataAsset cta = new NativeDataAsset();
        cta.setRequired(true);
        cta.setDataType(NativeDataAsset.DATA_TYPE.CTATEXT);
        adUnit.addAsset(cta);
    }

}
