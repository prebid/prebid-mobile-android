package org.prebid.mobile.core.mockobjects;

import android.content.Context;
import android.os.Handler;

import org.prebid.mobile.core.AdUnit;
import org.prebid.mobile.core.BidManager;
import org.prebid.mobile.core.BidResponse;
import org.prebid.mobile.core.DemandAdapter;
import org.prebid.mobile.core.ErrorCode;

import java.util.ArrayList;
import java.util.HashMap;

public class MockServer implements DemandAdapter {
    private static HashMap<String, ArrayList<BidResponse>> testSetUps = new HashMap<>();
    private static long delayedMillis = 0; // default no delay

    public static void setDelayedMillis(long delayedMillis) {
        MockServer.delayedMillis = delayedMillis;
    }

    public static void addTestSetup(String configId, ArrayList<BidResponse> bids) {
        testSetUps.remove(configId);
        testSetUps.put(configId, bids);
    }

    public static void clearSetUps() {
        testSetUps.clear();
    }

    @Override
    public void requestBid(Context context, final BidManager.BidResponseListener bidResponseListener, final ArrayList<AdUnit> adUnits) {
        if (bidResponseListener != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (AdUnit adUnit : adUnits) {
                        ArrayList<BidResponse> bidResponses = testSetUps.get(adUnit.getConfigId());
                        if (bidResponses != null && !bidResponses.isEmpty()) {
                            bidResponseListener.onBidSuccess(adUnit, bidResponses);
                        } else {
                            bidResponseListener.onBidFailure(adUnit, ErrorCode.NO_BIDS);
                        }
                    }
                }
            }, delayedMillis);
        }

    }
}
