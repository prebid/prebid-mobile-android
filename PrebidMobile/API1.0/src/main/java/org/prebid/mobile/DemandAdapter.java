/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

import android.support.annotation.MainThread;

import java.util.HashMap;

/**
 * This class defines the contract between PrebidMobile Mobile core logic and the demand adapter
 * PrebidMobile Mobile will instantiate the class that implements this interface, and then call
 * the methods when needed.
 */
interface DemandAdapter {

    /**
     * PrebidMobile Mobile will create a DemandAdapterListener and pass it to this method
     * when requesting for demand. It will pass in an auction id for unique identifying
     * a request. When this method is called, the Demand Adpater should proceed to fetch
     * demand for the RequestParams, and call the listener with returned response, whether
     * successful or failure.
     *
     * @param params    request paras for targeting
     * @param listener  Demand Adapter listener to be called upon demand ready
     * @param auctionId an unique identifier
     */

    void requestDemand(RequestParams params, DemandAdapterListener listener, String auctionId);

    void stopRequest(String auctionId);

    interface DemandAdapterListener {
        @MainThread
        void onDemandReady(HashMap<String, String> demand, String auctionId);

        @MainThread
        void onDemandFailed(ResultCode resultCode, String auctionId);

    }
}
