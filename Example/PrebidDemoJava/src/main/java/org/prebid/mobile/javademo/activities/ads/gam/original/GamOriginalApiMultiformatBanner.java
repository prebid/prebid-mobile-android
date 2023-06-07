package org.prebid.mobile.javademo.activities.ads.gam.original;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;

import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.BannerParameters;
import org.prebid.mobile.Signals;
import org.prebid.mobile.VideoParameters;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.addendum.PbFindSizeError;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.javademo.activities.BaseAdActivity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;

public class GamOriginalApiMultiformatBanner extends BaseAdActivity {

    private static final String AD_UNIT_ID = "/21808260008/prebid-demo-original-banner-multiformat";
    private static final String CONFIG_ID_BANNER = "prebid-ita-banner-300-250";
    private static final String CONFIG_ID_VIDEO = "prebid-ita-video-outstream-original-api";
    private static final int WIDTH = 300;
    private static final int HEIGHT = 250;

    public BannerAdUnit adUnit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAd();
    }

    private void createAd() {
        String configId;
        if (new Random().nextBoolean()) {
            configId = CONFIG_ID_BANNER;
        } else {
            configId = CONFIG_ID_VIDEO;
        }

        adUnit = new BannerAdUnit(configId, WIDTH, HEIGHT, EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO));
        adUnit.setVideoParameters(new VideoParameters(Collections.singletonList("video/mp4")));

        BannerParameters parameters = new BannerParameters();
        parameters.setApi(Collections.singletonList(Signals.Api.MRAID_2));
        adUnit.setBannerParameters(parameters);

        /* For GAM less than version 20 use PublisherAdView */
        final AdManagerAdView gamView = new AdManagerAdView(this);
        gamView.setAdUnitId(AD_UNIT_ID);
        gamView.setAdSizes(new AdSize(WIDTH, HEIGHT));

        getAdWrapperView().addView(gamView);

        gamView.setAdListener(createListener(gamView));

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        adUnit.setAutoRefreshInterval(getRefreshTimeSeconds());
        adUnit.fetchDemand(builder, resultCode -> {
            /* For GAM less than version 20 use PublisherAdRequest */
            AdManagerAdRequest request = builder.build();
            gamView.loadAd(request);
        });
    }

    private AdListener createListener(AdManagerAdView gamView) {
        return new AdListener() {
            @Override
            public void onAdLoaded() {
                AdViewUtils.findPrebidCreativeSize(gamView, new AdViewUtils.PbFindSizeListener() {
                    @Override
                    public void success(
                            int width,
                            int height
                    ) {
                        gamView.setAdSizes(new AdSize(width, height));
                    }

                    @Override
                    public void failure(@NonNull PbFindSizeError error) {
                    }
                });
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
        }
    }
}
