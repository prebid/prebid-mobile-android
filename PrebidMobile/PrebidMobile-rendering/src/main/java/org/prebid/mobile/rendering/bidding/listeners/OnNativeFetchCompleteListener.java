package org.prebid.mobile.rendering.bidding.listeners;

import org.prebid.mobile.rendering.bidding.data.NativeFetchDemandResult;

public interface OnNativeFetchCompleteListener {
    void onComplete(NativeFetchDemandResult result);
}
