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
    // MoPub ad unit ids
    static final String MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS = "a935eac11acd416f92640411234fbba6";
    static final String MOPUB_BANNER_ADUNIT_ID_320x50_APPNEXUS = "9dbccb87ab4d4a178450c0bc986b4571";
    static final String MOPUB_INTERSTITIAL_ADUNIT_ID_APPNEXUS = "2829868d308643edbec0795977f17437";
    // DFP ad unit ids
    static final String DFP_BANNER_ADUNIT_ID_300x250_APPNEXUS = "/19968336/PriceCheck_300x250";
    static final String DFP_BANNER_ADUNIT_ID_ALL_SIZES_APPNEXUS = "/19968336/PrebidMobileValidator_Banner_All_Sizes";
    static final String DFP_INTERSTITIAL_ADUNIT_ID_APPNEXUS = "/19968336/PriceCheck_Interstitial";

    //RubiconProject
    // Prebid server config ids
    static final String PBS_ACCOUNT_ID_RUBICON = "1001";
    static final String PBS_CONFIG_ID_300x250_RUBICON = "1001-1";
    static final String PBS_CONFIG_ID_INTERSTITIAL_RUBICON = "";
    // MoPub ad unit ids
    static final String MOPUB_BANNER_ADUNIT_ID_300x250_RUBICON = "a108b8dd5ebc472098167e6f1c118120";
    static final String MOPUB_INTERSTITIAL_ADUNIT_ID_RUBICON = "";
    // DFP ad unit ids
    static final String DFP_BANNER_ADUNIT_ID_300x250_RUBICON = "/5300653/test_adunit_pavliuchyk_300x250_prebid-server.rubiconproject.com_puc";
    static final String DFP_INTERSTITIAL_ADUNIT_ID_RUBICON = "";

    static String PBS_ACCOUNT_ID = PBS_ACCOUNT_ID_APPNEXUS;
    static String PBS_CONFIG_ID_300x250 = PBS_CONFIG_ID_300x250_APPNEXUS;
    static String PBS_CONFIG_ID_320x50 = PBS_CONFIG_ID_320x50_APPNEXUS;
    static String PBS_CONFIG_ID_INTERSTITIAL = PBS_CONFIG_ID_INTERSTITIAL_APPNEXUS;
    // MoPub ad unit ids
    static String MOPUB_BANNER_ADUNIT_ID_300x250 = MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS;
    static String MOPUB_BANNER_ADUNIT_ID_320x50 = MOPUB_BANNER_ADUNIT_ID_320x50_APPNEXUS;
    static String MOPUB_INTERSTITIAL_ADUNIT_ID = MOPUB_INTERSTITIAL_ADUNIT_ID_APPNEXUS;
    // DFP ad unit ids
    static String DFP_BANNER_ADUNIT_ID_300x250 = DFP_BANNER_ADUNIT_ID_300x250_APPNEXUS;
    static String DFP_BANNER_ADUNIT_ID_ALL_SIZES = DFP_BANNER_ADUNIT_ID_ALL_SIZES_APPNEXUS;
    static String DFP_INTERSTITIAL_ADUNIT_ID = DFP_INTERSTITIAL_ADUNIT_ID_APPNEXUS;

}
