package org.prebid.mobile.rendering.bidding.listeners;

import androidx.annotation.MainThread;

import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;

public interface OnFetchCompleteListener {

    @MainThread
    void onComplete(FetchDemandResult result);
}
