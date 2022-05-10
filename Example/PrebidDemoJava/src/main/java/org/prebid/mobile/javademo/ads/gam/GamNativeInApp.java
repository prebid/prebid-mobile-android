package org.prebid.mobile.javademo.ads.gam;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeCustomFormatAd;
import org.prebid.mobile.*;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.javademo.R;
import org.prebid.mobile.javademo.utils.DownloadImageTask;

import java.util.ArrayList;

public class GamNativeInApp {

    private static final String TAG = GamNativeInApp.class.getSimpleName();

    private static AdManagerAdView adView;
    private static NativeAd unifiedNativeAd;
    private static NativeAdUnit adUnit;
    private static AdLoader adLoader;

    public static void create(
        ViewGroup wrapper,
        String adUnitId,
        String configId,
        String customFormatId
    ) {
        adUnit = new NativeAdUnit(configId);
        configureNativeAdUnit(adUnit);

        final AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        adLoader = createAdLoader(wrapper, adUnitId, customFormatId);
        adUnit.fetchDemand(adRequest, resultCode -> {
            if (resultCode != ResultCode.SUCCESS) {
                Toast.makeText(wrapper.getContext(), "Native Ad Unit: " + resultCode.name(), Toast.LENGTH_SHORT).show();
            }
            adLoader.loadAd(adRequest);
        });
    }

    public static void destroy() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        if (unifiedNativeAd != null) {
            unifiedNativeAd.destroy();
            unifiedNativeAd = null;
        }
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
        adLoader = null;
    }

    private static void inflatePrebidNativeAd(
        final PrebidNativeAd ad,
        ViewGroup wrapper
    ) {
        View nativeContainer = View.inflate(wrapper.getContext(), R.layout.layout_native, null);
        ad.registerView(nativeContainer, new PrebidNativeAdEventListener() {
            @Override
            public void onAdClicked() {
                Toast.makeText(wrapper.getContext(), "onAdClicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdImpression() {
                Toast.makeText(wrapper.getContext(), "onAdImpression", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdExpired() {
                Toast.makeText(wrapper.getContext(), "onAdExpired", Toast.LENGTH_SHORT).show();
            }
        });
        ImageView icon = nativeContainer.findViewById(R.id.imgIcon);
        loadImage(icon, ad.getIconUrl());
        TextView title = nativeContainer.findViewById(R.id.tvTitle);
        title.setText(ad.getTitle());
        ImageView image = nativeContainer.findViewById(R.id.imgImage);
        loadImage(image, ad.getImageUrl());
        TextView description = nativeContainer.findViewById(R.id.tvDesc);
        description.setText(ad.getDescription());
        Button cta = nativeContainer.findViewById(R.id.btnCta);
        cta.setText(ad.getCallToAction());
        wrapper.addView(nativeContainer);
    }

    private static AdLoader createAdLoader(
        ViewGroup wrapper,
        String adUnitId,
        String customFormatId
    ) {
        OnAdManagerAdViewLoadedListener onGamAdLoaded = adManagerAdView -> {
            Log.d(TAG, "Gam loaded");
            adView = adManagerAdView;
            wrapper.addView(adManagerAdView);
        };

        NativeAd.OnNativeAdLoadedListener onUnifiedAdLoaded = unifiedNativeAd -> {
            Log.d(TAG, "Unified native loaded");
            GamNativeInApp.unifiedNativeAd = unifiedNativeAd;
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
                    // inflate nativeCustomTemplateAd
                }

                @Override
                public void onPrebidNativeNotValid() {
                    Log.e(TAG, "onPrebidNativeNotFound");
                    // show your own content
                }
            });
        };

        return new AdLoader.Builder(wrapper.getContext(), adUnitId)
            .forAdManagerAdView(onGamAdLoaded, AdSize.BANNER)
            .forNativeAd(onUnifiedAdLoaded)
            .forCustomFormatAd(customFormatId, onCustomAdLoaded, (customAd, s) -> {})
            .withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Toast.makeText(wrapper.getContext(), "DFP onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                }
            })
            .build();
    }

    private static void configureNativeAdUnit(NativeAdUnit adUnit) {
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

    private static void loadImage(
        ImageView image,
        String url
    ) {
        new DownloadImageTask(image).execute(url);
    }

}
