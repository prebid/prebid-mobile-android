package org.prebid.mobile.api.original;

import static org.prebid.mobile.PrebidMobile.AUTO_REFRESH_DELAY_MAX;
import static org.prebid.mobile.PrebidMobile.AUTO_REFRESH_DELAY_MIN;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.data.BidInfo;

/**
 * Universal ad unit for original API. It allows to make multi-format request.
 * Fetch demand result provides access to bid info data {@link BidInfo}.
 */
public class PrebidAdUnit {

    @NonNull
    private final String configId;
    @Nullable
    private MultiformatAdUnitFacade adUnit;

    /**
     * Default constructor.
     */
    public PrebidAdUnit(@NonNull String configId) {
        this.configId = configId;
    }

    /**
     * Loads ad and calls listener with bid info data.
     *
     * @param request  request object
     * @param listener callback when operation is completed (success or fail)
     */
    public void fetchDemand(
            PrebidRequest request,
            OnFetchDemandResult listener
    ) {
        baseFetchDemand(request, null, listener);
    }

    /**
     * Loads ad, applies keywords to the ad object, and calls listener with bid info data.
     * @param adObject AdMob's ({@code AdManagerAdRequest} or @{@code AdManagerAdRequest.Builder})
     *                 or AppLovin's ({@code MaxNativeAdLoader}) ad object
     * @param request request object
     * @param listener callback when operation is completed (success or fail)
     */
    public void fetchDemand(
            Object adObject,
            PrebidRequest request,
            OnFetchDemandResult listener
    ) {
        baseFetchDemand(request, adObject, listener);
    }

    /**
     * Auto refresh interval for banner ad.
     */
    public void setAutoRefreshInterval(
            @IntRange(from = AUTO_REFRESH_DELAY_MIN / 1000, to = AUTO_REFRESH_DELAY_MAX / 1000) int seconds
    ) {
        if (adUnit != null) {
            adUnit.setAutoRefreshInterval(seconds);
        }
    }

    /**
     * Resumes auto refresh interval after stopping.
     */
    public void resumeAutoRefresh() {
        if (adUnit != null) {
            adUnit.resumeAutoRefresh();
        }
    }

    /**
     * Stops auto refresh interval.
     */
    public void stopAutoRefresh() {
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
        }
    }

    /**
     * Destroy ad unit and stop downloading.
     */
    public void destroy() {
        if (adUnit != null) {
            adUnit.destroy();
        }
    }


    private void baseFetchDemand(
            @Nullable PrebidRequest request,
            @Nullable Object adObject,
            @Nullable OnFetchDemandResult userListener
    ) {
        if (userListener == null) {
            LogUtil.error("Parameter OnFetchDemandResult in fetchDemand() must be not null.");
            return;
        }

        if (request == null || requestDoesNotHaveAnyConfiguration(request)) {
            userListener.onComplete(BidInfo.create(ResultCode.INVALID_PREBID_REQUEST_OBJECT, null, null));
            return;
        }

        if (adUnit != null) {
            adUnit.destroy();
        }

        adUnit = new MultiformatAdUnitFacade(configId, request);

        OnCompleteListenerImpl innerListener = new OnCompleteListenerImpl(adUnit, adObject, userListener);
        if (adObject != null) {
            adUnit.fetchDemand(adObject, innerListener);
        } else {
            adUnit.fetchDemand(innerListener);
        }
    }


    private boolean requestDoesNotHaveAnyConfiguration(PrebidRequest request) {
        return request.getBannerParameters() == null &&
                request.getVideoParameters() == null &&
                request.getNativeParameters() == null;
    }

}
