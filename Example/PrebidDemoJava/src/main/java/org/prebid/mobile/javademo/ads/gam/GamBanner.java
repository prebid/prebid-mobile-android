package org.prebid.mobile.javademo.ads.gam;

import android.util.Log;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.BannerBaseAdUnit;
import org.prebid.mobile.Signals;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.addendum.PbFindSizeError;

import java.util.Collections;

public class GamBanner {

    private static final String TAG = GamBanner.class.getSimpleName();

    public static BannerAdUnit adUnit;

    public static void create(
        ViewGroup wrapper,
        String adUnitId,
        String configId,
        int width,
        int height,
        int autoRefreshTime
    ) {
        BannerBaseAdUnit.Parameters parameters = new BannerBaseAdUnit.Parameters();
        parameters.setApi(Collections.singletonList(Signals.Api.MRAID_2));
        adUnit = new BannerAdUnit(configId, width, height);
        adUnit.setParameters(parameters);

        /* For GAM less than version 20 use PublisherAdView */
        final AdManagerAdView gamView = new AdManagerAdView(wrapper.getContext());
        gamView.setAdUnitId(adUnitId);
        gamView.setAdSizes(new com.google.android.gms.ads.AdSize(width, height));

        wrapper.removeAllViews();
        wrapper.addView(gamView);

        gamView.setAdListener(createListener(gamView));

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        adUnit.setAutoRefreshInterval(autoRefreshTime);
        adUnit.fetchDemand(builder, resultCode -> {
            /* For GAM less than version 20 use PublisherAdRequest */
            AdManagerAdRequest request = builder.build();
            gamView.loadAd(request);
        });
    }

    public static void destroy() {
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
    }

    private static AdListener createListener(AdManagerAdView gamView) {
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
                        Log.d(TAG, "Can't find prebid creative size: " + error.getDescription());
                    }
                });
            }
        };
    }

}
