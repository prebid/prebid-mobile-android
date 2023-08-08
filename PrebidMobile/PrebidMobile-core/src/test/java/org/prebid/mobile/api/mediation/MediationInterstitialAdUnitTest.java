/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.api.mediation;

import static org.junit.Assert.assertEquals;

import android.app.Activity;
import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.config.MockMediationUtils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.EnumSet;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MediationInterstitialAdUnitTest {

    private Context context;
    private MediationInterstitialAdUnit mediationInterstitialAdUnit;

    @Before
    public void setUp() throws Exception {
        context = Robolectric.buildActivity(Activity.class).create().get();
        PrebidMobile.setPrebidServerAccountId("id");
        mediationInterstitialAdUnit = new MediationInterstitialAdUnit(context, "config", new AdSize(1, 2), new MockMediationUtils());
    }

    @After
    public void cleanup() {
        PrebidMobile.setPrebidServerAccountId(null);
    }

    @Test
    public void whenInitAdConfig_PrepareAdConfigForInterstitial() {
        AdSize adSize = new AdSize(1, 2);
        mediationInterstitialAdUnit.initAdConfig("config", adSize);
        AdUnitConfiguration adConfiguration = mediationInterstitialAdUnit.adUnitConfig;
        assertEquals("config", adConfiguration.getConfigId());
        assertEquals(EnumSet.of(AdFormat.VAST, AdFormat.INTERSTITIAL), adConfiguration.getAdFormats());
        assertEquals(adSize, adConfiguration.getMinSizePercentage());
    }

    @Test
    public void whenConstructorAndAdUnitFormatBanner_AdUnitIdentifierTypeInterstitial() {
        mediationInterstitialAdUnit = new MediationInterstitialAdUnit(
                context,
                "config",
                EnumSet.of(AdUnitFormat.BANNER),
                new MockMediationUtils()
        );
        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL), mediationInterstitialAdUnit.adUnitConfig.getAdFormats());
    }

    @Test
    public void whenConstructorAndAdUnitFormatVideo_AdUnitIdentifierTypeVideo() {
        mediationInterstitialAdUnit = new MediationInterstitialAdUnit(
            context,
            "config",
            EnumSet.of(AdUnitFormat.VIDEO),
            new MockMediationUtils()
        );
        assertEquals(EnumSet.of(AdFormat.VAST), mediationInterstitialAdUnit.adUnitConfig.getAdFormats());
    }

}