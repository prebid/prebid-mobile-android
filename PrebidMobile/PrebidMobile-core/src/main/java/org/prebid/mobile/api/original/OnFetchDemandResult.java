package org.prebid.mobile.api.original;

import org.prebid.mobile.api.data.BidInfo;

public interface OnFetchDemandResult {

    void onComplete(BidInfo bidInfo);

}
