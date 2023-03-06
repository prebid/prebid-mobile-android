package org.prebid.mobile.javademo.activities.ads.gam.original;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;

import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeDataAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.NativeImageAsset;
import org.prebid.mobile.NativeTitleAsset;
import org.prebid.mobile.javademo.activities.BaseAdActivity;

import java.util.ArrayList;

public class GamOriginalApiNativeStyles extends BaseAdActivity {

    private static final String AD_UNIT_ID = "/21808260008/prebid-demo-original-native-styles";
    private static final String CONFIG_ID = "imp-prebid-banner-native-styles";

    private static NativeAdUnit nativeAdUnit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAd();
    }

    private void createAd() {
        nativeAdUnit = new NativeAdUnit(CONFIG_ID);
        configureNativeAdUnit(nativeAdUnit);

        final AdManagerAdView gamView = new AdManagerAdView(this);
        gamView.setAdUnitId(AD_UNIT_ID);
        gamView.setAdSizes(AdSize.FLUID);

        getAdWrapperView().addView(gamView);

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();

        nativeAdUnit.setAutoRefreshInterval(getRefreshTimeSeconds());
        nativeAdUnit.fetchDemand(builder, resultCode -> {
            AdManagerAdRequest request = builder.build();
            gamView.loadAd(request);
        });
    }

    private void configureNativeAdUnit(NativeAdUnit adUnit) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (nativeAdUnit != null) {
            nativeAdUnit.stopAutoRefresh();
        }
    }
}
