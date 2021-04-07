package com.openx.apollo.bidding.listeners;

import androidx.annotation.MainThread;

import com.openx.apollo.bidding.data.FetchDemandResult;

public interface OnFetchCompleteListener {

    @MainThread
    void onComplete(FetchDemandResult result);
}
