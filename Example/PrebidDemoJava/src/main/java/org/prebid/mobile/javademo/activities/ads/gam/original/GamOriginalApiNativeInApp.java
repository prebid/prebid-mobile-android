package org.prebid.mobile.javademo.activities.ads.gam.original;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeCustomFormatAd;

import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeDataAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.NativeImageAsset;
import org.prebid.mobile.NativeTitleAsset;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdEventListener;
import org.prebid.mobile.PrebidNativeAdListener;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.javademo.R;
import org.prebid.mobile.javademo.activities.BaseAdActivity;
import org.prebid.mobile.javademo.utils.ImageUtils;

import java.util.ArrayList;

public class GamOriginalApiNativeInApp extends BaseAdActivity {

    private static final String AD_UNIT_ID = "/21808260008/apollo_custom_template_native_ad_unit";
    private static final String CONFIG_ID = "imp-prebid-banner-native-styles";
    private static final String CUSTOM_FORMAT_ID = "11934135";
    private static final String TAG = "GamOriginalNative";

    private AdManagerAdView adView;
    private NativeAd unifiedNativeAd;
    private NativeAdUnit adUnit;
    private AdLoader adLoader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAd();
    }

    private void createAd() {
        adUnit = new NativeAdUnit(CONFIG_ID);
        configureNativeAdUnit(adUnit);

        final AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        adLoader = createAdLoader(getAdWrapperView());
        adUnit.fetchDemand(adRequest, resultCode -> adLoader.loadAd(adRequest));
    }

    private void inflatePrebidNativeAd(
        final PrebidNativeAd ad,
        ViewGroup wrapper
    ) {
        View nativeContainer = View.inflate(wrapper.getContext(), R.layout.layout_native, null);
        ad.registerView(nativeContainer, new PrebidNativeAdEventListener() {
            @Override
            public void onAdClicked() {
            }

            @Override
            public void onAdImpression() {
            }

            @Override
            public void onAdExpired() {
            }
        });
        ImageView icon = nativeContainer.findViewById(R.id.imgIcon);

        ImageUtils.download(ad.getIconUrl(), icon);

        TextView title = nativeContainer.findViewById(R.id.tvTitle);
        title.setText(ad.getTitle());
        ImageView image = nativeContainer.findViewById(R.id.imgImage);

        ImageUtils.download(ad.getImageUrl(), image);

        TextView description = nativeContainer.findViewById(R.id.tvDesc);
        description.setText(ad.getDescription());
        Button cta = nativeContainer.findViewById(R.id.btnCta);
        cta.setText(ad.getCallToAction());
        wrapper.addView(nativeContainer);
    }

    private AdLoader createAdLoader(
        ViewGroup wrapper
    ) {
        OnAdManagerAdViewLoadedListener onGamAdLoaded = adManagerAdView -> {
            Log.d(TAG, "Gam loaded");
            adView = adManagerAdView;
            wrapper.addView(adManagerAdView);
        };

        NativeAd.OnNativeAdLoadedListener onUnifiedAdLoaded = unifiedNativeAd -> {
            Log.d(TAG, "Unified native loaded");
            this.unifiedNativeAd = unifiedNativeAd;
        };

        NativeCustomFormatAd.OnCustomFormatAdLoadedListener onCustomAdLoaded = nativeCustomTemplateAd -> {
            Log.d(TAG, "Custom ad loaded");
            AdViewUtils.findNative(nativeCustomTemplateAd, new PrebidNativeAdListener() {
                @Override
                public void onPrebidNativeLoaded(PrebidNativeAd ad) {
                    inflatePrebidNativeAd(ad, wrapper);
                }

                @Override
                public void onPrebidNativeNotFound() {
                    Log.e(TAG, "onPrebidNativeNotFound");
                }

                @Override
                public void onPrebidNativeNotValid() {
                    Log.e(TAG, "onPrebidNativeNotValid");
                }
            });
        };

        return new AdLoader.Builder(wrapper.getContext(), AD_UNIT_ID)
            .forAdManagerAdView(onGamAdLoaded, AdSize.BANNER)
            .forNativeAd(onUnifiedAdLoaded)
            .forCustomFormatAd(CUSTOM_FORMAT_ID, onCustomAdLoaded, (customAd, s) -> {
            })
            .withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.e(TAG, "DFP onAdFailedToLoad");
                }
            })
            .build();
    }

    private void configureNativeAdUnit(NativeAdUnit adUnit) {
        adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);

        ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS);
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
        if (adView != null) {
            adView.destroy();
        }
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
        }
        if (unifiedNativeAd != null) {
            unifiedNativeAd.destroy();
        }
    }
}
