package org.prebid.mobile.admob;

import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.mediation.UnifiedNativeAdMapper;
import com.google.android.gms.ads.mediation.customevent.CustomEventNativeListener;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdEventListener;

import java.util.ArrayList;
import java.util.Map;

public class PrebidNativeAdMapper extends UnifiedNativeAdMapper {

    private final PrebidNativeAd prebidAd;
    private final PrebidNativeAdEventListener prebidListener = createListener();
    private final CustomEventNativeListener adMobListener;

    public PrebidNativeAdMapper(PrebidNativeAd prebidAd, CustomEventNativeListener adMobListener) {
        super();
        this.prebidAd = prebidAd;
        this.adMobListener = adMobListener;
    }

    @Override
    public void trackViews(@NonNull View view, @NonNull Map<String, View> map, @NonNull Map<String, View> map1) {
        super.trackViews(view, map, map1);
        prebidAd.registerViewList(view, new ArrayList<>(map.values()), createListener());
    }

    @Override
    public void recordImpression() {
        prebidListener.onAdImpression();
    }

    @Override
    public void handleClick(@NonNull View view) {
        prebidListener.onAdClicked();
    }

    private PrebidNativeAdEventListener createListener() {
        return new PrebidNativeAdEventListener() {
            @Override
            public void onAdClicked() {
                adMobListener.onAdClicked();
                adMobListener.onAdOpened();
            }

            @Override
            public void onAdImpression() {
            }

            @Override
            public void onAdExpired() {
            }
        };
    }

}
