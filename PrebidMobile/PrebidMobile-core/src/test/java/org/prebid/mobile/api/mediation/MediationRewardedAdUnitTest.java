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

import android.app.Activity;
import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.BannerParameters;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.Signals;
import org.prebid.mobile.VideoParameters;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.rendering.bidding.config.MockMediationUtils;
import org.prebid.mobile.rendering.models.AdPosition;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MediationRewardedAdUnitTest {

    private Context context;

    @Before
    public void setUp() {
        context = Robolectric.buildActivity(Activity.class).create().get();
        PrebidMobile.setPrebidServerAccountId("id");
    }

    @After
    public void cleanup() {
        PrebidMobile.setPrebidServerAccountId(null);
    }

    @Test
    public void whenConstructorAndAdUnitFormatBanner_AdUnitIdentifierTypeInterstitial() {
        MediationRewardedAdUnit rewardedAdUnit = new MediationRewardedAdUnit(
                context,
                "config",
                EnumSet.of(AdUnitFormat.BANNER),
                new MockMediationUtils()
        );

        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL), rewardedAdUnit.adUnitConfig.getAdFormats());
        assertTrue(rewardedAdUnit.adUnitConfig.isRewarded());
        assertEquals(AdPosition.FULLSCREEN.getValue(), rewardedAdUnit.adUnitConfig.getAdPositionValue());
    }

    @Test
    public void whenConstructorAndAdUnitFormatVideo_AdUnitIdentifierTypeVideo() {
        MediationRewardedAdUnit rewardedAdUnit = new MediationRewardedAdUnit(
                context,
                "config",
                EnumSet.of(AdUnitFormat.VIDEO),
                new MockMediationUtils()
        );

        assertEquals(EnumSet.of(AdFormat.VAST), rewardedAdUnit.adUnitConfig.getAdFormats());
        assertTrue(rewardedAdUnit.adUnitConfig.isRewarded());
        assertEquals(AdPosition.FULLSCREEN.getValue(), rewardedAdUnit.adUnitConfig.getAdPositionValue());
    }

    @Test
    public void whenConstructorAndAdUnitFormatBannerVideo_AdUnitIdentifierTypeMultiformat() {
        MediationRewardedAdUnit rewardedAdUnit = new MediationRewardedAdUnit(
                context,
                "config",
                EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO),
                new MockMediationUtils()
        );

        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL, AdFormat.VAST), rewardedAdUnit.adUnitConfig.getAdFormats());
        assertTrue(rewardedAdUnit.adUnitConfig.isRewarded());
    }

    @Test
    public void whenDefaultMediationRewardedAdUnit_AdUnitIdentifierTypeVideo() {
        MediationRewardedAdUnit rewardedAdUnit = new MediationRewardedAdUnit(
                context,
                "config",
                new MockMediationUtils()
        );

        assertEquals(EnumSet.of(AdFormat.VAST), rewardedAdUnit.adUnitConfig.getAdFormats());
        assertTrue(rewardedAdUnit.adUnitConfig.isRewarded());
    }

    @Test
    public void whenInitAdConfig_ConfiguresRewardedFullscreenState() {
        MediationRewardedAdUnit rewardedAdUnit = new MediationRewardedAdUnit(
                context,
                "config",
                new MockMediationUtils()
        );

        rewardedAdUnit.initAdConfig("config", null);

        assertTrue(rewardedAdUnit.adUnitConfig.isRewarded());
        assertEquals(AdPosition.FULLSCREEN.getValue(), rewardedAdUnit.adUnitConfig.getAdPositionValue());
    }

    @Test
    public void bannerAndVideoParameters_AccessibleAfterFormatChange() {
        MediationRewardedAdUnit rewardedAdUnit = new MediationRewardedAdUnit(
                context,
                "config",
                EnumSet.of(AdUnitFormat.BANNER),
                new MockMediationUtils()
        );
        BannerParameters bannerParameters = new BannerParameters();
        bannerParameters.setApi(new ArrayList<Signals.Api>() {{
            add(Signals.Api.MRAID_1);
        }});
        VideoParameters videoParameters = new VideoParameters(new ArrayList<>());
        videoParameters.setMaxDuration(30);

        assertNull(rewardedAdUnit.getBannerParameters());
        assertNull(rewardedAdUnit.getVideoParameters());

        rewardedAdUnit.setBannerParameters(bannerParameters);
        rewardedAdUnit.setVideoParameters(videoParameters);

        assertEquals(bannerParameters, rewardedAdUnit.getBannerParameters());
        assertEquals(videoParameters, rewardedAdUnit.getVideoParameters());
    }

}
