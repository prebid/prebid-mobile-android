package org.prebid.mobile.rendering.bidding.display;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.enums.BannerAdPosition;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;
import org.prebid.mobile.rendering.utils.helpers.VisibilityChecker;

public class MoPubBannerAdUnit extends BaseAdUnit {
    private static final String TAG = MoPubBannerAdUnit.class.getSimpleName();

    private final ScreenStateReceiver mScreenStateReceiver = new ScreenStateReceiver();

    private boolean mAdFailed;

    public MoPubBannerAdUnit(Context context, String configId, AdSize size) {
        super(context, configId, size);
        mScreenStateReceiver.register(context);
    }

    @Override
    protected final void initAdConfig(String configId, AdSize adSize) {
        mAdUnitConfig.addSize(adSize);
        mAdUnitConfig.setConfigId(configId);
        mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
    }

    @Override
    protected final boolean isAdObjectSupported(
        @Nullable
            Object adObject) {
        return ReflectionUtils.isMoPubBannerView(adObject);
    }

    @Override
    public void destroy() {
        super.destroy();
        mScreenStateReceiver.unregister();
    }

    @Override
    protected void initBidLoader() {
        super.initBidLoader();

        final VisibilityTrackerOption visibilityTrackerOption = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);
        final VisibilityChecker visibilityChecker = new VisibilityChecker(visibilityTrackerOption);
        mBidLoader.setBidRefreshListener(() -> {
            Object moPubView = mAdViewReference.get();
            if (!(moPubView instanceof View)) {
                return false;
            }

            if (mAdFailed) {
                mAdFailed = false;
                return true;
            }

            final boolean isWindowVisibleToUser = mScreenStateReceiver.isScreenOn();
            return visibilityChecker.isVisibleForRefresh(((View) moPubView)) && isWindowVisibleToUser;
        });
    }

    @Override
    public final void fetchDemand(
        @Nullable
            Object mopubView,
        @NonNull
            OnFetchCompleteListener listener) {
        super.fetchDemand(mopubView, listener);
    }

    /**
     * Sets NativeAdConfiguration and enables Native Ad requests
     *
     * @param configuration - configured NativeAdConfiguration class
     */
    public void setNativeAdConfiguration(NativeAdConfiguration configuration) {
        mAdUnitConfig.setNativeAdConfiguration(configuration);
    }

    public final void addAdditionalSizes(AdSize... sizes) {
        mAdUnitConfig.addSizes(sizes);
    }

    public final void setRefreshInterval(int seconds) {
        mAdUnitConfig.setAutoRefreshDelay(seconds);
    }

    public void setAdPosition(BannerAdPosition bannerAdPosition) {
        final AdPosition adPosition = BannerAdPosition.mapToAdPosition(bannerAdPosition);
        mAdUnitConfig.setAdPosition(adPosition);
    }

    public BannerAdPosition getAdPosition() {
        return BannerAdPosition.mapToDisplayAdPosition(mAdUnitConfig.getAdPositionValue());
    }

    public void stopRefresh() {
        if (mBidLoader != null) {
            mBidLoader.cancelRefresh();
        }
    }

    public void onAdFailed() {
        mAdFailed = true;
    }
}
