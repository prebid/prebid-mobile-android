/*
 *    Copyright 2016 APPNEXUS INC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.prebid.mobile.core;

import android.content.Context;

import java.util.ArrayList;

/**
 * Adapter interface to be implemented by the demand source request adapter class to request bids
 */
public interface DemandAdapter {

    /**
     * The BidManager SDK uses this method to call your SDK for requesting to start the bidding process.
     *
     * @param context             application context to be used by the demand source module
     * @param bidResponseListener listener to listen to the response
     * @param adUnits             objects containing information about the ad units
     */
    void requestBid(Context context, BidManager.BidResponseListener bidResponseListener, ArrayList<AdUnit> adUnits);
}
