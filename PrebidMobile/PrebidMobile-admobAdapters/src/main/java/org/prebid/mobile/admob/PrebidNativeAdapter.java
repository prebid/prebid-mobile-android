package org.prebid.mobile.admob;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.MediationNativeAdCallback;
import com.google.android.gms.ads.mediation.MediationNativeAdConfiguration;
import com.google.android.gms.ads.mediation.UnifiedNativeAdMapper;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.ParametersMatcher;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.rendering.bidding.events.EventsNotifier;

import java.util.HashMap;
import java.util.Set;

public class PrebidNativeAdapter extends PrebidBaseAdapter {

    private static final String TAG = "PrebidNative";

    @Override
    public void loadNativeAd(
            @NonNull MediationNativeAdConfiguration configuration,
            @NonNull MediationAdLoadCallback<UnifiedNativeAdMapper, MediationNativeAdCallback> adMobLoadListener
    ) {
        Bundle extras = configuration.getMediationExtras();
        String cacheId = extras.getString(NativeAdUnit.BUNDLE_KEY_CACHE_ID);
        if (cacheId == null) {
            adMobLoadListener.onFailure(AdErrors.emptyNativeCacheId());
            return;
        }

        Bundle extrasCopy = new Bundle(extras);
        extrasCopy.remove(NativeAdUnit.BUNDLE_KEY_CACHE_ID);
        HashMap<String, String> prebidParameters = convertToMap(extrasCopy);

        String serverParameter = configuration.getServerParameters().getString(MediationConfiguration.CUSTOM_EVENT_SERVER_PARAMETER_FIELD);
        if (!ParametersMatcher.doParametersMatch(serverParameter, prebidParameters)) {
            adMobLoadListener.onFailure(AdErrors.notMatchedParameters());
            return;
        }
        LogUtil.verbose(TAG, "Parameters are matched! (" + serverParameter + ")");

        PrebidNativeAd nativeAd = PrebidNativeAd.create(cacheId);
        if (nativeAd == null) {
            adMobLoadListener.onFailure(AdErrors.prebidNativeAdIsNull());
            return;
        }
        EventsNotifier.notify(nativeAd.getWinEvent());

        PrebidNativeAdMapper mapper = new PrebidNativeAdMapper(nativeAd);
        mapper.configure(configuration, adMobLoadListener);
    }

    private HashMap<String, String> convertToMap(Bundle bundle) {
        HashMap<String, String> result = new HashMap<>();
        Set<String> keySet = bundle.keySet();
        for (String key : keySet) {
            String value = bundle.getString(key);
            result.put(key, value);
        }
        return result;
    }

}
