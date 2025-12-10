package org.prebid.mobile.rendering.utils.helpers;

import androidx.annotation.Nullable;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

public class RenderingExceptionParser {

    private RenderingExceptionParser() {
    }

    public static boolean isBidInvalid(@Nullable BidResponse bidResponse) {
        return bidResponse == null || bidResponse.getWinningBid() == null;
    }

    @Nullable
    public static AdException getPrebidException(@Nullable BidResponse bidResponse, @Nullable AdException knownException) {
        if (bidResponse == null) {
            if (knownException != null) {
                return knownException;
            } else {
                return new AdException(AdException.INTERNAL_ERROR, "Unknown exception");
            }
        }

        if (bidResponse.getWinningBid() == null) {
            return new AdException(AdException.NO_BIDS, "There are no bids or bids don't have required targeting keywords");
        }
        return null;
    }

}
