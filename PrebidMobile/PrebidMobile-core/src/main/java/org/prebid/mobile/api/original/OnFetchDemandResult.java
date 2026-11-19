package org.prebid.mobile.api.original;

import androidx.annotation.NonNull;

import org.prebid.mobile.api.data.BidInfo;

/**
 * Fetch demand listener that receives the full {@link BidInfo}. Used by the original API
 * ({@link PrebidAdUnit} and {@code AdUnit}) and by {@code fetchDemandBidInfo} of the Mediation API
 * ad units.
 * <p>
 * {@link BidInfo#getResultCode()} is an {@link org.prebid.mobile.ResultCode} in both APIs, also
 * for Mediation API ad units, whose {@code fetchDemand} reports a
 * {@link org.prebid.mobile.api.data.FetchDemandResult} instead.
 */
public interface OnFetchDemandResult {

    void onComplete(@NonNull BidInfo bidInfo);

}
