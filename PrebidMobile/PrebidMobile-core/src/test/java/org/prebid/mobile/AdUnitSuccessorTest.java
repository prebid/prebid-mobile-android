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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.reflection.AdUnitReflection;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class AdUnitSuccessorTest {

    @Mock
    BidLoader mockBidLoader;

    String testConfigId = "123456";
    int width = 320;
    int height = 50;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAutoRefresh() {
        AdUnit adUnit = new BannerAdUnit(testConfigId, width, height);
        AdUnitReflection.setBidLoader(adUnit, mockBidLoader);

        adUnit.resumeAutoRefresh();
        verify(mockBidLoader).setupRefreshTimer();

        adUnit.stopAutoRefresh();
        verify(mockBidLoader).cancelRefresh();
    }

    @Test
    public void testBannerAdUnitCreation() {
        BannerAdUnit adUnit = new BannerAdUnit(testConfigId, width, height);
        AdUnitConfiguration configuration = adUnit.getConfiguration();

        assertEquals(1, configuration.getSizes().size());
        assertEquals(testConfigId, configuration.getConfigId());
        assertEquals(EnumSet.of(AdFormat.BANNER), configuration.getAdFormats());

        assertEquals(0, adUnit.configuration.getAutoRefreshDelay());
        adUnit.setAutoRefreshInterval(30);
        assertEquals(30_000, adUnit.configuration.getAutoRefreshDelay());
    }

    @Test
    public void testBannerAdUnitAddSize() throws Exception {
        BannerAdUnit adUnit = new BannerAdUnit(testConfigId, width, height);
        adUnit.addAdditionalSize(300, height);
        assertEquals(2, adUnit.getSizes().size());
        adUnit.addAdditionalSize(width, height);
        assertEquals(2, adUnit.getSizes().size());
    }

    @Test
    public void testBannerParametersCreation() {
        //given
        BannerAdUnit bannerAdUnit = new BannerAdUnit(testConfigId, width, height);

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
        AdUnitConfiguration configuration = adUnit.getConfiguration();
        assertEquals(testConfigId, configuration.getConfigId());
        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL), configuration.getAdFormats());
    }


    @Test
    public void testAdvancedInterstitialAdUnitCreation() {
        InterstitialAdUnit adUnit = new InterstitialAdUnit(testConfigId, height, 70);
        AdUnitConfiguration configuration = (AdUnitConfiguration) adUnit.getConfiguration();

        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL), configuration.getAdFormats());
        assertEquals(testConfigId, configuration.getConfigId());
        assertEquals(height, configuration.getMinSizePercentage().getWidth());
        assertEquals(70, configuration.getMinSizePercentage().getHeight());
    }

    @Test
    public void testVideoAdUnitCreation() {
        VideoAdUnit adUnit = new VideoAdUnit(testConfigId, width, height);
        AdUnitConfiguration configuration = (AdUnitConfiguration) adUnit.getConfiguration();

        AdSize size = configuration.getSizes().iterator().next();
        assertEquals(width, size.getWidth());
        assertEquals(height, size.getHeight());
        assertEquals(testConfigId, configuration.getConfigId());
        assertEquals(EnumSet.of(AdFormat.VAST), configuration.getAdFormats());
    }

    @Test
    public void testVideoInterstitialAdUnitCreation() {
        VideoInterstitialAdUnit adUnit = new VideoInterstitialAdUnit(testConfigId);
        AdUnitConfiguration configuration = (AdUnitConfiguration) adUnit.getConfiguration();

        assertEquals(testConfigId, configuration.getConfigId());
        assertEquals(EnumSet.of(AdFormat.VAST), configuration.getAdFormats());
    }

    @Test
    public void testRewardedVideoAdUnitCreation() {
        RewardedVideoAdUnit adUnit = new RewardedVideoAdUnit(testConfigId);
        AdUnitConfiguration configuration = adUnit.getConfiguration();
        assertEquals(testConfigId, configuration.getConfigId());
        assertEquals(EnumSet.of(AdFormat.VAST), configuration.getAdFormats());
    }

    @Test
    public void testVideoParametersCreation() {

        //given
        VideoAdUnit videoAdUnit = new VideoAdUnit(testConfigId, width, height);
        VideoInterstitialAdUnit videoInterstitialAdUnit = new VideoInterstitialAdUnit(testConfigId);
        RewardedVideoAdUnit rewardedVideoAdUnit = new RewardedVideoAdUnit(testConfigId);

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
        parameters.setLinearity(1);

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
        Integer linearity = testedVideoParameters.getLinearity();

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
        assertEquals(new Integer(1), linearity);
    }
}
