package org.prebid.mobile.rendering.bidding.display;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdPosition;

public class MoPubInterstitialAdUnit extends BaseAdUnit {
    private static final String TAG = MoPubInterstitialAdUnit.class.getSimpleName();

    /**
     * Constructor to fetch demand for a display interstitial ad with specified minHeightPercentage and minWidthPercentage
     */
    public MoPubInterstitialAdUnit(Context context, String configId, AdSize minSizePercentage) {
        super(context, configId, minSizePercentage);
    }

    /**
     * Constructor to fetch demand for either display or video interstitial ads
     */
    public MoPubInterstitialAdUnit(Context context, String configId,
                                   @NonNull
                                           AdUnitFormat adUnitFormat) {
        super(context, configId, null);
        setAdUnitType(adUnitFormat);
    }

    @Override
    public final void fetchDemand(
        @Nullable
            Object mopubView,
        @NonNull
            OnFetchCompleteListener listener) {
        super.fetchDemand(mopubView, listener);
    }

    @Override
    protected final void initAdConfig(String configId, AdSize minSizePercentage) {
        mAdUnitConfig.setMinSizePercentage(minSizePercentage);
        mAdUnitConfig.setConfigId(configId);
        mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.INTERSTITIAL);
        mAdUnitConfig.setAdPosition(AdPosition.FULLSCREEN);
    }

    @Override
    protected final boolean isAdObjectSupported(
        @Nullable
            Object adObject) {
        return ReflectionUtils.isMoPubInterstitialView(adObject);
    }

    private void setAdUnitType(AdUnitFormat adUnitFormat) {
        switch (adUnitFormat) {
            case DISPLAY:
                mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.INTERSTITIAL);
                break;
            case VIDEO:
                mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.VAST);
                break;
        }
    }
}
