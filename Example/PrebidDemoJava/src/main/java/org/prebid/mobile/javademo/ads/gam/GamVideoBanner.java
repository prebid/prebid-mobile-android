package org.prebid.mobile.javademo.ads.gam;

import android.util.Log;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import org.prebid.mobile.Signals;
import org.prebid.mobile.VideoAdUnit;
import org.prebid.mobile.VideoBaseAdUnit;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.addendum.PbFindSizeError;

import java.util.Collections;

public class GamVideoBanner {

    private static final String TAG = GamVideoBanner.class.getSimpleName();

    private static VideoAdUnit adUnit;

    public static void create(
        ViewGroup wrapper,
        String adUnitId,
        String configId,
        int autoRefreshTime
    ) {
        VideoBaseAdUnit.Parameters parameters = new VideoBaseAdUnit.Parameters();
        parameters.setMimes(Collections.singletonList("video/mp4"));
        parameters.setProtocols(Collections.singletonList(Signals.Protocols.VAST_2_0));
        parameters.setPlaybackMethod(Collections.singletonList(Signals.PlaybackMethod.AutoPlaySoundOff));
        parameters.setPlacement(Signals.Placement.InBanner);

        adUnit = new VideoAdUnit(configId, 300, 250);
        adUnit.setParameters(parameters);

        final AdManagerAdView gamView = new AdManagerAdView(wrapper.getContext());
        gamView.setAdUnitId(adUnitId);
        gamView.setAdSizes(new AdSize(300, 250));
        gamView.setAdListener(createListener(gamView));

        wrapper.removeAllViews();
        wrapper.addView(gamView);

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();

        adUnit.setAutoRefreshInterval(autoRefreshTime);
        adUnit.fetchDemand(builder, resultCode -> {
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
                        gamView.setAdSizes(new com.google.android.gms.ads.AdSize(width, height));
                    }

                    @Override
                    public void failure(@NonNull PbFindSizeError error) {
                        Log.d(TAG, "Can't find prebid creative size: " + error);
                    }
                });
            }
        };
    }

}
