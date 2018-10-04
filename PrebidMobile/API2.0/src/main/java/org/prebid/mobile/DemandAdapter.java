package org.prebid.mobile;

import android.content.Context;

import java.util.HashMap;

/**
 * This class defines the contract between Prebid Mobile core logic and the demand adapter
 * Prebid Mobile will instantiate the class that implements this interface, and then call
 * the methods when needed.
 */
public interface DemandAdapter {

    /**
     * Prebid Mobile will create a DemandAdapterListener and pass it to this method
     * when requesting for demand. It will pass in an auction id for unique identifying
     * a request. When this method is called, the Demand Adpater should proceed to fetch
     * demand for the RequestParams, and call the listener with returned response, whether
     * successful or failure.
     *
     * @param context   Context of the apps
     * @param params    request paras for targeting
     * @param listener  Demand Adapter listener to be called upon demand ready
     * @param auctionId an unique identifier
     */

    void requestDemand(Context context, RequestParams params, DemandAdapterListener listener, String auctionId);

    void stopRequest(String auctionId);

    interface DemandAdapterListener {
        void onDemandReady(HashMap<String, String> demand, String auctionId);

        void onDemandFailed(ResultCode resultCode, String auctionId);

    }
}
