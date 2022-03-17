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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.testutils.BaseSetup;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;
import org.prebid.mobile.units.configuration.BaseAdUnitConfiguration;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class AdUnitSuccessorTest {

    @Mock
    DemandFetcher mockDemandFetcher;
    String testConfigId = "123456";

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testResumeAutoRefresh() throws IllegalAccessException {
        AdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        ReflectionUtils.setVariableValueInObject(adUnit, "fetcher", mockDemandFetcher);
        adUnit.resumeAutoRefresh();
        verify(mockDemandFetcher).start();
    }

    @Test
    public void testStopAutoRefresh() throws IllegalAccessException {
        AdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        ReflectionUtils.setVariableValueInObject(adUnit, "fetcher", mockDemandFetcher);

        adUnit.stopAutoRefresh();

        verify(mockDemandFetcher).stop();
    }

    //Banner AdUnit
    @Test
    public void testBannerAdUnitCreation() throws Exception {
        BannerAdUnit adUnit = new BannerAdUnit(testConfigId, 320, 50);
        AdUnitConfiguration configuration = (AdUnitConfiguration) adUnit.getConfiguration();

        assertEquals(1, configuration.getSizes().size());
        assertEquals(testConfigId, configuration.getConfigId());
        assertEquals(AdType.BANNER, configuration.getAdType());
        assertEquals(0, FieldUtils.readField(adUnit, "periodMillis", true));
    }

    @Test
    public void testBannerAdUnitAddSize() throws Exception {
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        adUnit.addAdditionalSize(300, 250);
        assertEquals(2, adUnit.getSizes().size());
        adUnit.addAdditionalSize(320, 50);
        assertEquals(2, adUnit.getSizes().size());
    }

    @Test
    public void testBannerParametersCreation() throws Exception {

        //given
        BannerAdUnit bannerAdUnit = new BannerAdUnit("123456", 320, 50);

        BannerAdUnit.Parameters parameters = new BannerAdUnit.Parameters();
        parameters.setApi(Arrays.asList(Signals.Api.VPAID_1, Signals.Api.VPAID_2));

        bannerAdUnit.setParameters(parameters);

        //when
        BannerAdUnit.Parameters testedBannerParameters = bannerAdUnit.getParameters();
        List<Signals.Api> api = testedBannerParameters.getApi();

        //then
        assertEquals(2, api.size());
        assertTrue(api.contains(new Signals.Api(1)) && api.contains(new Signals.Api(2)));

    }

    @Test
    public void testInterstitialAdUnitCreation() throws Exception {
        InterstitialAdUnit adUnit = new InterstitialAdUnit(testConfigId);
        AdUnitConfiguration configuration = (AdUnitConfiguration) adUnit.getConfiguration();
        assertEquals(testConfigId, configuration.getConfigId());
        assertEquals(AdType.INTERSTITIAL, configuration.getAdType());
    }


    @Test
    public void testAdvancedInterstitialAdUnitCreation() {
        InterstitialAdUnit adUnit = new InterstitialAdUnit(testConfigId, 50, 70);
        AdUnitConfiguration configuration = (AdUnitConfiguration) adUnit.getConfiguration();

        assertEquals(AdType.INTERSTITIAL, configuration.getAdType());
        assertEquals(testConfigId, configuration.getConfigId());
        assertEquals(50, configuration.getMinSizePercentage().getWidth());
        assertEquals(70, configuration.getMinSizePercentage().getHeight());
    }

    @Test
    public void testVideoAdUnitCreation() {
        VideoAdUnit adUnit = new VideoAdUnit(testConfigId, 320, 50);
        AdUnitConfiguration configuration = (AdUnitConfiguration) adUnit.getConfiguration();

        AdSize size = configuration.getSizes().iterator().next();
        assertEquals(320, size.getWidth());
        assertEquals(50, size.getHeight());
        assertEquals(testConfigId, configuration.getConfigId());
        assertEquals(AdType.VIDEO, configuration.getAdType());
    }

    @Test
    public void testVideoInterstitialAdUnitCreation() {
        VideoInterstitialAdUnit adUnit = new VideoInterstitialAdUnit(testConfigId);
        AdUnitConfiguration configuration = (AdUnitConfiguration) adUnit.getConfiguration();

        assertEquals(testConfigId, configuration.getConfigId());
        assertEquals(AdType.VIDEO_INTERSTITIAL, configuration.getAdType());
    }

    @Test
    public void testRewardedVideoAdUnitCreation() {
        RewardedVideoAdUnit adUnit = new RewardedVideoAdUnit("123456");
        BaseAdUnitConfiguration configuration = adUnit.getConfiguration();
        assertEquals("123456", configuration.getConfigId());
        assertEquals(AdType.REWARDED_VIDEO, configuration.getAdType());
    }

    @Test
    public void testVideoParametersCreation() {

        //given
        VideoAdUnit videoAdUnit = new VideoAdUnit("123456", 320, 50);
        VideoInterstitialAdUnit videoInterstitialAdUnit = new VideoInterstitialAdUnit("123456");
        RewardedVideoAdUnit rewardedVideoAdUnit = new RewardedVideoAdUnit("123456");

        List<VideoBaseAdUnit> videoBaseAdUnitList = Arrays.asList(videoAdUnit, videoInterstitialAdUnit, rewardedVideoAdUnit);

        for (VideoBaseAdUnit videoBaseAdUnit : videoBaseAdUnitList) {
            setupAndCheckVideoParametersHelper(videoBaseAdUnit);
        }

    }

    private void setupAndCheckVideoParametersHelper(VideoBaseAdUnit videoBaseAdUnit) {

        VideoAdUnit.Parameters parameters = new VideoAdUnit.Parameters();

        parameters.setApi(Arrays.asList(Signals.Api.VPAID_1, Signals.Api.VPAID_2));
        parameters.setMaxBitrate(1500);
        parameters.setMinBitrate(300);
        parameters.setMaxDuration(30);
        parameters.setMinDuration(5);
        parameters.setMimes(Arrays.asList("video/x-flv", "video/mp4"));
        parameters.setPlaybackMethod(Arrays.asList(Signals.PlaybackMethod.AutoPlaySoundOn, Signals.PlaybackMethod.ClickToPlay));
        parameters.setProtocols(Arrays.asList(Signals.Protocols.VAST_2_0, Signals.Protocols.VAST_3_0));
        parameters.setStartDelay(Signals.StartDelay.PreRoll);
        parameters.setPlacement(Signals.Placement.InBanner);

        videoBaseAdUnit.setParameters(parameters);

        //when
        VideoAdUnit.Parameters testedVideoParameters = videoBaseAdUnit.getParameters();

        List<Signals.Api> api = testedVideoParameters.getApi();
        Integer maxBitrate = testedVideoParameters.getMaxBitrate();
        Integer minBitrate = testedVideoParameters.getMinBitrate();
        Integer maxDuration = testedVideoParameters.getMaxDuration();
        Integer minDuration = testedVideoParameters.getMinDuration();
        List<String> mimes = testedVideoParameters.getMimes();
        List<Signals.PlaybackMethod> playbackMethod = testedVideoParameters.getPlaybackMethod();
        List<Signals.Protocols> protocols = testedVideoParameters.getProtocols();
        Signals.StartDelay startDelay = testedVideoParameters.getStartDelay();
        Signals.Placement placement = testedVideoParameters.getPlacement();

        //then
        assertEquals(2, api.size());
        assertTrue(api.contains(new Signals.Api(1)) && api.contains(new Signals.Api(2)));
        assertEquals(new Integer(1500), maxBitrate);
        assertEquals(new Integer(300), minBitrate);
        assertEquals(new Integer(30), maxDuration);
        assertEquals(new Integer(5), minDuration);
        assertEquals(2, mimes.size());
        assertTrue(mimes.contains("video/x-flv") && mimes.contains("video/mp4"));
        assertEquals(2, playbackMethod.size());
        assertTrue(playbackMethod.contains(new Signals.PlaybackMethod(1)) && playbackMethod.contains(new Signals.PlaybackMethod(3)));
        assertEquals(2, protocols.size());
        assertTrue(protocols.contains(new Signals.Protocols(2)) && protocols.contains(new Signals.Protocols(3)));
        assertEquals(new Signals.StartDelay(0), startDelay);
        assertEquals(new Signals.Placement(2), placement);
    }
}
