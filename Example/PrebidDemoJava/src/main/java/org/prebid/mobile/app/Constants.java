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
    static final String PBS_CONFIG_ID_NATIVE_APPNEXUS = "25e17008-5081-4676-94d5-923ced4359d3";
    // MoPub ad unit ids
    static final String MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS = "a935eac11acd416f92640411234fbba6";
    static final String MOPUB_BANNER_ADUNIT_ID_320x50_APPNEXUS = "9dbccb87ab4d4a178450c0bc986b4571";
    static final String MOPUB_INTERSTITIAL_ADUNIT_ID_APPNEXUS = "2829868d308643edbec0795977f17437";
    static final String MOPUB_IN_BANNER_NATIVE_ADUNIT_ID_APPNEXUS = "037a743e5d184129ab79c941240efff8";
    // DFP ad unit ids
    static final String DFP_BANNER_ADUNIT_ID_300x250_APPNEXUS = "/19968336/PriceCheck_300x250";
    static final String DFP_BANNER_ADUNIT_ID_ALL_SIZES_APPNEXUS = "/19968336/PrebidMobileValidator_Banner_All_Sizes";
    static final String DFP_INTERSTITIAL_ADUNIT_ID_APPNEXUS = "/19968336/PriceCheck_Interstitial";
    static final String DFP_IN_BANNER_NATIVE_ADUNIT_ID_APPNEXUS = "/19968336/Wei_Prebid_Native_Test";
    //RubiconProject
    // Prebid server config ids
    static final String PBS_ACCOUNT_ID_RUBICON = "1001";
    static final String PBS_CONFIG_ID_300x250_RUBICON = "1001-1";
    static final String PBS_CONFIG_ID_INTERSTITIAL_RUBICON = "1001-1";
    static final String PBS_STORED_RESPONSE_300x250_RUBICON = "1001-rubicon-300x250";
    static final String PBS_STORED_RESPONSE_VAST_RUBICON = "sample_video_response";
    // MoPub ad unit ids
    static final String MOPUB_BANNER_ADUNIT_ID_300x250_RUBICON = "a108b8dd5ebc472098167e6f1c118120";
    static final String MOPUB_INTERSTITIAL_ADUNIT_ID_RUBICON = "d5c75d9f0b8742cab579610930077c35";
    static final String MOPUB_INTERSTITIAL_VIDEO_ADUNIT_ID_RUBICON = "723dd84529b04075aa003a152ede0c4b";
    static final String MP_ADUNITID_REWARDED = "066483fc44bf4793b4e275522ef7c428";
    // DFP ad unit ids
    static final String DFP_BANNER_ADUNIT_ID_300x250_RUBICON = "/5300653/pavliuchyk_test_adunit_1x1_puc";
    static final String DFP_INTERSTITIAL_ADUNIT_ID_RUBICON = "/5300653/pavliuchyk_test_adunit_1x1_puc";
    static final String DFP_VAST_ADUNIT_ID_RUBICON = "/5300653/test_adunit_vast_pavliuchyk";
    static final String DFP_REWARDED_ADUNIT_ID_RUBICON = "/5300653/test_adunit_vast_rewarded-video_pavliuchyk";
}
