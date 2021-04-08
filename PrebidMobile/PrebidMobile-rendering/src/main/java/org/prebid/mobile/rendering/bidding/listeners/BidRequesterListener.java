package org.prebid.mobile.rendering.bidding.listeners;

import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.errors.AdException;

public interface BidRequesterListener {

    void onFetchCompleted(BidResponse response);

    void onError(AdException exception);
}
