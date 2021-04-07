package com.openx.apollo.bidding.listeners;

import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.errors.AdException;

public interface BidRequesterListener {

    void onFetchCompleted(BidResponse response);

    void onError(AdException exception);
}
