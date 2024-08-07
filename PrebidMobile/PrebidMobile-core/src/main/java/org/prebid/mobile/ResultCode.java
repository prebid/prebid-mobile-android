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

/**
 * Result code for fetch demand.
 */
public enum ResultCode {
    /**
     * The attaching keywords was successful, which means
     * there was demand and the demand was set on the ad object.
     */
    SUCCESS,
    /**
     * The ad request failed due to empty account id
     */
    INVALID_ACCOUNT_ID,
    /**
     * The ad request failed due to empty config id on the ad unit
     */
    INVALID_CONFIG_ID,
    /**
     * The ad request failed because a CUSTOM host used without providing host url
     */
    INVALID_HOST_URL,
    /**
     * For banner view, we don't support multi-size request
     */
    INVALID_SIZE,
    /**
     * Unable to obtain the Application Context, check if you have set it through PrebidMobile.setApplicationContext()
     */
    INVALID_CONTEXT,
    /**
     * Currently, we only support Banner, Interstitial, DFP Banner, Interstitial
     */
    INVALID_AD_OBJECT,
    /**
     * The ad request failed due to a network error.
     */
    NETWORK_ERROR,
    /**
     * The ad request took longer than set time out
     */
    TIMEOUT,
    /**
     * No bids available from demand source
     */
    NO_BIDS,
    /**
     * Prebid Server responded with some error messages
     */
    PREBID_SERVER_ERROR,
    /**
     * Missing assets requirement for native ad unit
     */
    INVALID_NATIVE_REQUEST,
    /**
     * Check @{@link org.prebid.mobile.api.original.PrebidRequest} object that you put into fetchDemand().
     */
    INVALID_PREBID_REQUEST_OBJECT
}
