package org.prebid.mobile.mopub.mock;

import android.content.Context;
import androidx.annotation.NonNull;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.MediationRewardedVideoAdUnit;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;

public class OpenMediationRewardedVideoAdUnit extends MediationRewardedVideoAdUnit {

    public OpenMediationRewardedVideoAdUnit(Context context, String configId, PrebidMediationDelegate mediationDelegate) {
        super(context, configId, mediationDelegate);
    }

    @Override
    public void fetchDemand(@NonNull OnFetchCompleteListener listener) {
        super.fetchDemand(listener);
    }

    public void onResponse(BidResponse bidResponse) {
        onResponseReceived(bidResponse);
    }

}