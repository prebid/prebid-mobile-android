package org.prebid.mobile.mopub.mock;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.MediationBaseAdUnit;
import org.prebid.mobile.rendering.bidding.display.MediationRewardedVideoAdUnit;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;

public class OpenMediationRewardedVideoAdUnit extends MediationRewardedVideoAdUnit {

    public OpenMediationRewardedVideoAdUnit(Context context, @NonNull String mopubAdUnitId, String configId, PrebidMediationDelegate mediationDelegate) {
        super(context, mopubAdUnitId, configId, mediationDelegate);
    }

    @Override
    public void fetchDemand(@Nullable Object adObject, @NonNull OnFetchCompleteListener listener) {
        super.fetchDemand(adObject, listener);
    }

    public void onResponse(BidResponse bidResponse) {
        onResponseReceived(bidResponse);
    }

}