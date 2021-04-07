package com.openx.apollo.bidding.listeners;

import com.openx.apollo.bidding.data.NativeFetchDemandResult;

public interface OnNativeFetchCompleteListener {
    void onComplete(NativeFetchDemandResult result);
}
