package org.prebid.mobile.mopub.mock;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.MediationBaseAdUnit;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;

public class OpenMediationBaseAdUnit extends MediationBaseAdUnit {

    public OpenMediationBaseAdUnit(Context context, String configId, AdSize adSize, PrebidMediationDelegate mediationDelegate) {
        super(context, configId, adSize, mediationDelegate);
    }

    @Override
    public void fetchDemand(@Nullable Object adObject, @NonNull OnFetchCompleteListener listener) {
        super.fetchDemand(adObject, listener);
    }

    @Override
    public void onResponseReceived(BidResponse response) {
        super.onResponseReceived(response);
    }

    @Override
    public void initAdConfig(String configId, AdSize adSize) {
        mAdUnitConfig.setConfigId(configId);
    }

    @Override
    protected boolean isAdObjectSupported(@Nullable Object adObject) {
        return true;
    }

}
