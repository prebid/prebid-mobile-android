package org.prebid.mobile.admob;

import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.mediation.UnifiedNativeAdMapper;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdEventListener;

import java.util.ArrayList;
import java.util.Map;

public class PrebidNativeAdMapper extends UnifiedNativeAdMapper {

    private final PrebidNativeAd prebidAd;

    public PrebidNativeAdMapper(PrebidNativeAd prebidAd) {
        super();
        this.prebidAd = prebidAd;
    }

    @Override
    public void trackViews(@NonNull View view, @NonNull Map<String, View> map, @NonNull Map<String, View> map1) {
        super.trackViews(view, map, map1);
        prebidAd.registerViewList(view, new ArrayList<>(map.values()), createListener());
    }

    private PrebidNativeAdEventListener createListener() {
        return new PrebidNativeAdEventListener() {
            @Override
            public void onAdClicked() {

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
