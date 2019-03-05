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

package org.prebid.mobile.app;


public class Constants {
    private Constants() {
    }

    static final String AD_TYPE_NAME = "adType";
    static final String AD_SERVER_NAME = "adServer";
    static final String AD_SIZE_NAME = "adSize";
    static final String AUTO_REFRESH_NAME = "autoRefresh";

    // Prebid server config ids
    static final String PBS_ACCOUNT_ID = "prebid.org";
    static final String PBS_CONFIG_ID_300x250_APPNEXUS_DEMAND = "6ace8c7d-88c0-4623-8117-75bc3f0a2e45";
    static final String PBS_CONFIG_ID_320x50_APPNEXUS_DEMAND = "625c6125-f19e-4d5b-95c5-55501526b2a4";
    static final String PBS_CONFIG_ID_INTERSTITIAL_APPNEXUS_DEMAND = "625c6125-f19e-4d5b-95c5-55501526b2a4";
    // MoPub ad unit ids
    static final String MOPUB_BANNER_ADUNIT_ID_300x250 = "a477c050091b43f4ae5bcdb410dce4f8";
    static final String MOPUB_BANNER_ADUNIT_ID_320x50 = "9dbccb87ab4d4a178450c0bc986b4571";
    static final String MOPUB_INTERSTITIAL_ADUNIT_ID = "2829868d308643edbec0795977f17437";
    // DFP ad unit ids
    static final String DFP_BANNER_ADUNIT_ID_300x250 = "/19968336/PriceCheck_300x250";
    static final String DFP_BANNER_ALL_SIZES = "/19968336/PrebidMobileValidator_Banner_All_Sizes";
    static final String DFP_INTERSTITIAL_ADUNIT_ID = "/19968336/PriceCheck_Interstitial";
}
