package org.prebid.mobile.javademo.activities.ads.gam.original;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.formats.AdManagerAdViewOptions;
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeCustomFormatAd;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;
import org.prebid.mobile.BannerParameters;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeAsset;
import org.prebid.mobile.NativeDataAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.NativeImageAsset;
import org.prebid.mobile.NativeParameters;
import org.prebid.mobile.NativeTitleAsset;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdListener;
import org.prebid.mobile.VideoParameters;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.addendum.PbFindSizeError;
import org.prebid.mobile.api.original.PrebidAdUnit;
import org.prebid.mobile.api.original.PrebidRequest;
import org.prebid.mobile.javademo.R;
import org.prebid.mobile.javademo.activities.BaseAdActivity;
import org.prebid.mobile.javademo.utils.ImageUtils;

import java.util.ArrayList;
import java.util.Random;

public class GamOriginalApiMultiformatBannerVideoNativeInApp extends BaseAdActivity {

    private static final String AD_UNIT_ID = "/21808260008/prebid-demo-multiformat";
    private static final String CONFIG_ID_BANNER = "prebid-ita-banner-300-250";
    private static final String CONFIG_ID_VIDEO = "prebid-ita-video-outstream-original-api";
    private static final String CONFIG_ID_NATIVE = "prebid-ita-banner-native-styles";
    private static final String CUSTOM_FORMAT_ID = "12304464";

    private PrebidAdUnit prebidAdUnit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAd();
    }

    private void createAd() {
        // Random only for test cases. For production use one config id.
        ArrayList<String> configIds = Lists.newArrayList(CONFIG_ID_BANNER, CONFIG_ID_VIDEO, CONFIG_ID_NATIVE);
        String configId = configIds.get((new Random().nextInt(3)));

        // 1. Create PrebidAdUnit with configId
        prebidAdUnit = new PrebidAdUnit(configId);

        // 2. Create PrebidRequest with needed multiformat parameters
        PrebidRequest prebidRequest = new PrebidRequest();
        prebidRequest.setBannerParameters(createBannerParameters());
        prebidRequest.setVideoParameters(createVideoParameters());
        prebidRequest.setNativeParameters(creativeNativeParameters());

        // 3. Make a bid request to Prebid Server
        AdManagerAdRequest gamRequest = new AdManagerAdRequest.Builder().build();
        prebidAdUnit.fetchDemand(gamRequest, prebidRequest, bidInfo -> {
            loadGam(gamRequest);
        });
    }

    private void loadGam(AdManagerAdRequest gamRequest) {
        // 4. Load GAM ad
        OnAdManagerAdViewLoadedListener onBannerLoaded = adManagerAdView -> showBannerAd(adManagerAdView);

        NativeAd.OnNativeAdLoadedListener onNativeLoaded = nativeAd -> showNativeAd(nativeAd, getAdWrapperView());

        NativeCustomFormatAd.OnCustomFormatAdLoadedListener onPrebidNativeAdLoaded = this::showPrebidNativeAd;

        AdLoader adLoader = new AdLoader.Builder(this, AD_UNIT_ID)
                .forAdManagerAdView(onBannerLoaded, AdSize.BANNER, AdSize.MEDIUM_RECTANGLE)
                .forNativeAd(onNativeLoaded)
                .forCustomFormatAd(CUSTOM_FORMAT_ID, onPrebidNativeAdLoaded, null)
                .withAdListener(new AdListenerWithToast(this))
                .withAdManagerAdViewOptions(new AdManagerAdViewOptions.Builder().build())
                .build();

        adLoader.loadAd(gamRequest);
    }

    private void showBannerAd(AdManagerAdView adView) {
        // 5.1. Show banner
        getAdWrapperView().addView(adView);
        AdViewUtils.findPrebidCreativeSize(adView, new AdViewUtils.PbFindSizeListener() {

            @Override
            public void success(int width, int height) {
                adView.setAdSizes(new AdSize(width, height));
            }

            @Override
            public void failure(@NonNull PbFindSizeError error) {
            }

        });
    }

    private void showNativeAd(NativeAd ad, ViewGroup adWrapperView) {
        // 5.2. Show GAM native
        View nativeContainer = View.inflate(adWrapperView.getContext(), R.layout.layout_native, null);

        TextView title = nativeContainer.findViewById(R.id.tvTitle);
        title.setText(ad.getHeadline());

        ImageView icon = nativeContainer.findViewById(R.id.imgIcon);
        String iconUrl = ad.getIcon().getUri().toString();
        ImageUtils.download(iconUrl, icon);

        ImageView image = nativeContainer.findViewById(R.id.imgImage);
        String imageUrl = ad.getImages().get(0).getUri().toString();
        ImageUtils.download(imageUrl, image);

        TextView description = nativeContainer.findViewById(R.id.tvDesc);
        description.setText(ad.getBody());

        Button cta = nativeContainer.findViewById(R.id.btnCta);
        cta.setText(ad.getCallToAction());

        adWrapperView.addView(nativeContainer);
    }

    private void showPrebidNativeAd(NativeCustomFormatAd customNativeAd) {
        // 5.3. Show Prebid native
        AdViewUtils.findNative(customNativeAd, new PrebidNativeAdListener() {
            @Override
            public void onPrebidNativeLoaded(PrebidNativeAd ad) {
                inflatePrebidNativeAd(ad, getAdWrapperView());
            }

            @Override
            public void onPrebidNativeNotFound() {
                Log.e("PrebidFindNative", "onPrebidNativeNotFound");
            }

            @Override
            public void onPrebidNativeNotValid() {
                Log.e("PrebidFindNative", "onPrebidNativeNotValid");
            }
        });
    }

    private BannerParameters createBannerParameters() {
        BannerParameters parameters = new BannerParameters();
        parameters.setAdSizes(Sets.newHashSet(new org.prebid.mobile.AdSize(300, 250)));
        return parameters;
    }

    private VideoParameters createVideoParameters() {
        VideoParameters parameters = new VideoParameters(Lists.newArrayList("video/mp4"));
        parameters.setAdSize(new org.prebid.mobile.AdSize(320, 480));
        return parameters;
    }

    private NativeParameters creativeNativeParameters() {
        ArrayList<NativeAsset> nativeAssets = Lists.newArrayList();

        NativeTitleAsset title = new NativeTitleAsset();
        title.setLength(90);
        title.setRequired(true);
        nativeAssets.add(title);

        NativeImageAsset icon = new NativeImageAsset(20, 20, 20, 20);
        icon.setImageType(NativeImageAsset.IMAGE_TYPE.ICON);
        icon.setRequired(true);
        nativeAssets.add(icon);

        NativeImageAsset image = new NativeImageAsset(200, 200, 200, 200);
        image.setImageType(NativeImageAsset.IMAGE_TYPE.MAIN);
        image.setRequired(true);
        nativeAssets.add(image);

        NativeDataAsset data = new NativeDataAsset();
        data.setLen(90);
        data.setDataType(NativeDataAsset.DATA_TYPE.SPONSORED);
        data.setRequired(true);
        nativeAssets.add(data);

        NativeDataAsset body = new NativeDataAsset();
        body.setRequired(true);
        body.setDataType(NativeDataAsset.DATA_TYPE.DESC);
        nativeAssets.add(body);

        NativeDataAsset cta = new NativeDataAsset();
        cta.setRequired(true);
        cta.setDataType(NativeDataAsset.DATA_TYPE.CTATEXT);
        nativeAssets.add(cta);

        ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS);
        NativeEventTracker tracker = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods);

        NativeParameters nativeParameters = new NativeParameters(nativeAssets);
        nativeParameters.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        nativeParameters.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        nativeParameters.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        nativeParameters.addEventTracker(tracker);
        return nativeParameters;
    }

    private void inflatePrebidNativeAd(
            final PrebidNativeAd ad,
            ViewGroup wrapper
    ) {
        View nativeContainer = View.inflate(wrapper.getContext(), R.layout.layout_native, null);

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

        ad.registerView(nativeContainer, Lists.newArrayList(icon, title, image, description, cta), null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (prebidAdUnit != null) {
            prebidAdUnit.destroy();
        }
    }


    private static class AdListenerWithToast extends AdListener {

        private final Context applicationContext;

        public AdListenerWithToast(Context context) {
            this.applicationContext = context.getApplicationContext();
        }

        @Override
        public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);

            Toast.makeText(
                    applicationContext,
                    "Ad failed to load!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

}
