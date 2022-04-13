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

    //AppNexus
    // Prebid server config ids
    static final String PBS_ACCOUNT_ID_APPNEXUS = "bfa84af2-bd16-4d35-96ad-31c6bb888df0";
    static final String PBS_CONFIG_ID_300x250_APPNEXUS = "6ace8c7d-88c0-4623-8117-75bc3f0a2e45";
    static final String PBS_CONFIG_ID_320x50_APPNEXUS = "625c6125-f19e-4d5b-95c5-55501526b2a4";
    static final String PBS_CONFIG_ID_INTERSTITIAL_APPNEXUS = "625c6125-f19e-4d5b-95c5-55501526b2a4";
    static final String PBS_CONFIG_ID_NATIVE_APPNEXUS = "03f3341f-1737-402c-bc7d-bc81dfebe9cf"; // 25e17008-5081-4676-94d5-923ced4359d3

    // DFP ad unit ids
    static final String DFP_BANNER_ADUNIT_ID_300x250_APPNEXUS = "/19968336/PriceCheck_300x250";
    static final String DFP_BANNER_ADUNIT_ID_ALL_SIZES_APPNEXUS = "/19968336/PrebidMobileValidator_Banner_All_Sizes";
    static final String DFP_INTERSTITIAL_ADUNIT_ID_APPNEXUS = "/19968336/PriceCheck_Interstitial";
    static final String DFP_IN_BANNER_NATIVE_ADUNIT_ID_APPNEXUS = "/19968336/Wei_Prebid_Native_Test";
    static final String DFP_NATIVE_NATIVE_ADUNIT_ID_APPNEXUS = "/19968336/Abhas_test_native_native_adunit";
    //RubiconProject
    // Prebid server config ids
    static final String PBS_ACCOUNT_ID_RUBICON = "1001";
    static final String PBS_CONFIG_ID_300x250_RUBICON = "1001-1";
    static final String PBS_CONFIG_ID_INTERSTITIAL_RUBICON = "1001-1";
    static final String PBS_STORED_RESPONSE_300x250_RUBICON = "1001-rubicon-300x250";
    static final String PBS_STORED_RESPONSE_VAST_RUBICON = "sample_video_response";

    // DFP ad unit ids
    static final String DFP_BANNER_ADUNIT_ID_300x250_RUBICON = "/5300653/pavliuchyk_test_adunit_1x1_puc";
    static final String DFP_INTERSTITIAL_ADUNIT_ID_RUBICON = "/5300653/pavliuchyk_test_adunit_1x1_puc";
    static final String DFP_VAST_ADUNIT_ID_RUBICON = "/5300653/test_adunit_vast_pavliuchyk";
    static final String DFP_REWARDED_ADUNIT_ID_RUBICON = "/5300653/test_adunit_vast_rewarded-video_pavliuchyk";
}
