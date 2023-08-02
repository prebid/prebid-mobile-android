package org.prebid.mobile.javademo.activities.ads.gam.original;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;

import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.Signals;
import org.prebid.mobile.VideoParameters;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.addendum.PbFindSizeError;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.javademo.activities.BaseAdActivity;

import java.util.Collections;
import java.util.EnumSet;

public class GamOriginalApiVideoBanner extends BaseAdActivity {

    private static final String AD_UNIT_ID = "/21808260008/prebid-demo-original-api-video-banner";
    private static final String CONFIG_ID = "prebid-ita-video-outstream-original-api";
    private static final int WIDTH = 300;
    private static final int HEIGHT = 250;

    private BannerAdUnit adUnit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAd();
    }

    private void createAd() {
        adUnit = new BannerAdUnit(CONFIG_ID, WIDTH, HEIGHT, EnumSet.of(AdUnitFormat.VIDEO));

        VideoParameters parameters = new VideoParameters(Collections.singletonList("video/mp4"));
        parameters.setProtocols(Collections.singletonList(Signals.Protocols.VAST_2_0));
        parameters.setPlaybackMethod(Collections.singletonList(Signals.PlaybackMethod.AutoPlaySoundOff));
        parameters.setPlacement(Signals.Placement.InBanner);
        adUnit.setVideoParameters(parameters);

        final AdManagerAdView gamView = new AdManagerAdView(this);
        gamView.setAdUnitId(AD_UNIT_ID);
        gamView.setAdSizes(new AdSize(WIDTH, HEIGHT));
        gamView.setAdListener(createListener(gamView));

        getAdWrapperView().addView(gamView);

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();

        adUnit.setAutoRefreshInterval(getRefreshTimeSeconds());
        adUnit.fetchDemand(builder, resultCode -> {
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
                        gamView.setAdSizes(new com.google.android.gms.ads.AdSize(width, height));
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
