package org.prebid.mobile.api.original;

import androidx.annotation.Nullable;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.data.BidInfo;

public class PrebidAdUnit {

    private final ConfigurableAdUnit adUnit;

    public PrebidAdUnit(String configId) {
        adUnit = new ConfigurableAdUnit(configId);
    }

    public void fetchDemand(
            PrebidRequest request,
            OnFetchDemandResult listener
    ) {
        baseFetchDemand(request, null, listener);
    }

    public void fetchDemand(
            Object adObject,
            PrebidRequest request,
            OnFetchDemandResult listener
    ) {
        baseFetchDemand(request, adObject, listener);
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
            userListener.onComplete(new BidInfo(ResultCode.INVALID_PREBID_REQUEST_OBJECT, null));
            return;
        }

        adUnit.setConfigurationBasedOnRequest(request);

        OnCompleteListenerImpl innerListener = new OnCompleteListenerImpl(adUnit, request, adObject, userListener);
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
