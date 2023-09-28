package org.prebid.mobile.api.original;

import androidx.annotation.NonNull;

import org.prebid.mobile.api.data.BidInfo;

public interface OnFetchDemandResult {

    void onComplete(@NonNull BidInfo bidInfo);

}
