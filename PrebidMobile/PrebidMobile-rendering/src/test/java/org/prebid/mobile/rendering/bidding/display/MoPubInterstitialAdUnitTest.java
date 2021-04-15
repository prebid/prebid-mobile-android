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

package org.prebid.mobile.rendering.bidding.display;

import android.app.Activity;
import android.content.Context;

import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MoPubInterstitialAdUnitTest {

    private Context mContext;
    private MoPubInterstitialAdUnit mMopubInterstitialAdUnit;
    private MoPubInterstitial mMopubInterstitial;

    @Before
    public void setUp() throws Exception {
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        mMopubInterstitial = new MoPubInterstitial((Activity) mContext, "unit");
        PrebidRenderingSettings.setAccountId("id");
        mMopubInterstitialAdUnit = new MoPubInterstitialAdUnit(mContext, "config", new AdSize(1, 2));
    }

    @After
    public void cleanup() {
        PrebidRenderingSettings.setAccountId(null);
    }

    @Test
    public void whenInitAdConfig_PrepareAdConfigForInterstitial() {
        AdSize adSize = new AdSize(1, 2);
        mMopubInterstitialAdUnit.initAdConfig("config", adSize);
        AdConfiguration adConfiguration = mMopubInterstitialAdUnit.mAdUnitConfig;
        assertEquals("config", adConfiguration.getConfigId());
        assertEquals(AdConfiguration.AdUnitIdentifierType.INTERSTITIAL, adConfiguration.getAdUnitIdentifierType());
        assertEquals(adSize, adConfiguration.getMinSizePercentage());
    }

    @Test
    public void whenIsMopubViewAndMoPubInterstitialPassed_ReturnTrue() {
        assertTrue(mMopubInterstitialAdUnit.isAdObjectSupported(mMopubInterstitial));
    }

    @Test
    public void whenIsMopubViewAndAnyObjectPassed_ReturnFalse() {
        MoPubView moPubView = new MoPubView(mContext);
        assertFalse(mMopubInterstitialAdUnit.isAdObjectSupported(moPubView));
    }

    @Test
    public void whenConstructorAndAdUnitFormatVideo_AdUnitIdentifierTypeVideo() {
        mMopubInterstitialAdUnit = new MoPubInterstitialAdUnit(mContext, "config", AdUnitFormat.VIDEO);
        assertEquals(AdConfiguration.AdUnitIdentifierType.VAST, mMopubInterstitialAdUnit.mAdUnitConfig.getAdUnitIdentifierType());
    }
}