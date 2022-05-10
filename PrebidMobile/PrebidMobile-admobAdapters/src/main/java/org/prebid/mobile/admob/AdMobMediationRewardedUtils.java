package org.prebid.mobile.admob;

import android.os.Bundle;
import androidx.annotation.Nullable;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;

import java.util.HashMap;

public class AdMobMediationRewardedUtils implements PrebidMediationDelegate {

    private final Bundle extras;

    public AdMobMediationRewardedUtils(Bundle adMobExtrasBundle) {
        this.extras = adMobExtrasBundle;
    }

    @Override
    public void setResponseToLocalExtras(@Nullable BidResponse response) {
        if (response != null) {
            extras.putString(PrebidRewardedAdapter.EXTRA_RESPONSE_ID, response.getId());
        }
    }

    @Override
    public void handleKeywordsUpdate(@Nullable HashMap<String, String> keywords) {

    }

    @Override
    public boolean canPerformRefresh() {
        return true;
    }

}
