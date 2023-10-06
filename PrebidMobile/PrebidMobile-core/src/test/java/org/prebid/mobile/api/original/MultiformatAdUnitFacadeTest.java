package org.prebid.mobile.api.original;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.BannerParameters;
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.DataObject;
import org.prebid.mobile.NativeAsset;
import org.prebid.mobile.NativeParameters;
import org.prebid.mobile.NativeTitleAsset;
import org.prebid.mobile.VideoParameters;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.models.PlacementType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MultiformatAdUnitFacadeTest {

    private MultiformatAdUnitFacade subject;

    private static final String configId = "testConfigId";

    @Test
    public void configuration_configId() {
        PrebidRequest request = new PrebidRequest();

        subject = new MultiformatAdUnitFacade(configId, request);
        AdUnitConfiguration configuration = subject.getConfiguration();

        assertEquals(configId, configuration.getConfigId());
    }

    @Test
    public void configuration_bannerParameters() {
        PrebidRequest request = new PrebidRequest();

        BannerParameters bannerParameters = new BannerParameters();
        HashSet<AdSize> adSizes = new HashSet<>(Arrays.asList(new AdSize(300, 250), new AdSize(320, 50)));
        bannerParameters.setAdSizes(adSizes);
        request.setBannerParameters(bannerParameters);


        subject = new MultiformatAdUnitFacade(configId, request);
        AdUnitConfiguration configuration = subject.getConfiguration();


        assertEquals(1, configuration.getAdFormats().size());
        assertEquals(AdFormat.BANNER, configuration.getAdFormats().toArray()[0]);
        assertEquals(bannerParameters, configuration.getBannerParameters());

        HashSet<AdSize> configurationSizes = configuration.getSizes();
        assertThat(
                new HashSet<>(Arrays.asList(new AdSize(300, 250), new AdSize(320, 50))),
                Matchers.equalTo(configurationSizes)
        );
    }

    @Test
    public void configuration_interstitialBannerParameters() {
        PrebidRequest request = new PrebidRequest();
        request.setInterstitial(true);

        BannerParameters bannerParameters = new BannerParameters();
        HashSet<AdSize> adSizes = new HashSet<>(Arrays.asList(new AdSize(320, 480)));
        bannerParameters.setAdSizes(adSizes);
        bannerParameters.setInterstitialMinWidthPercentage(30);
        bannerParameters.setInterstitialMinHeightPercentage(30);
        request.setBannerParameters(bannerParameters);


        subject = new MultiformatAdUnitFacade(configId, request);
        AdUnitConfiguration configuration = subject.getConfiguration();


        assertEquals(1, configuration.getAdFormats().size());
        assertEquals(AdFormat.INTERSTITIAL, configuration.getAdFormats().toArray()[0]);
        assertEquals(bannerParameters, configuration.getBannerParameters());
        assertEquals(new AdSize(30, 30), configuration.getMinSizePercentage());
        assertEquals(AdPosition.FULLSCREEN.getValue(), configuration.getAdPositionValue());

        HashSet<AdSize> configurationSizes = configuration.getSizes();
        assertThat(
                new HashSet<>(Arrays.asList(new AdSize(320, 480))),
                Matchers.equalTo(configurationSizes)
        );
    }

    @Test
    public void configuration_videoParameters() {
        PrebidRequest request = new PrebidRequest();

        VideoParameters videoParameters = new VideoParameters(Lists.newArrayList("video/mp4"));
        videoParameters.setAdSize(new AdSize(300, 250));
        request.setVideoParameters(videoParameters);


        subject = new MultiformatAdUnitFacade(configId, request);
        AdUnitConfiguration configuration = subject.getConfiguration();


        assertEquals(1, configuration.getAdFormats().size());
        assertEquals(AdFormat.VAST, configuration.getAdFormats().toArray()[0]);
        assertEquals(videoParameters, configuration.getVideoParameters());

        HashSet<AdSize> configurationSizes = configuration.getSizes();
        assertThat(
                new HashSet<>(Arrays.asList(new AdSize(300, 250))),
                Matchers.equalTo(configurationSizes)
        );
    }

    @Test
    public void configuration_interstitialVideoParameters() {
        PrebidRequest request = new PrebidRequest();
        request.setInterstitial(true);

        VideoParameters videoParameters = new VideoParameters(Lists.newArrayList("video/mp4"));
        videoParameters.setAdSize(new AdSize(300, 250));
        request.setVideoParameters(videoParameters);


        subject = new MultiformatAdUnitFacade(configId, request);
        AdUnitConfiguration configuration = subject.getConfiguration();


        assertEquals(1, configuration.getAdFormats().size());
        assertEquals(AdFormat.VAST, configuration.getAdFormats().toArray()[0]);
        assertEquals(videoParameters, configuration.getVideoParameters());
        assertEquals(PlacementType.INTERSTITIAL.getValue(), configuration.getPlacementTypeValue());
        assertEquals(AdPosition.FULLSCREEN.getValue(), configuration.getAdPositionValue());

        HashSet<AdSize> configurationSizes = configuration.getSizes();
        assertThat(
                new HashSet<>(Arrays.asList(new AdSize(300, 250))),
                Matchers.equalTo(configurationSizes)
        );
    }

    @Test
    public void configuration_rewardedVideoParameters() {
        PrebidRequest request = new PrebidRequest();
        request.setRewarded(true);

        VideoParameters videoParameters = new VideoParameters(Lists.newArrayList("video/mp4"));
        videoParameters.setAdSize(new AdSize(300, 250));
        request.setVideoParameters(videoParameters);


        subject = new MultiformatAdUnitFacade(configId, request);
        AdUnitConfiguration configuration = subject.getConfiguration();


        assertEquals(1, configuration.getAdFormats().size());
        assertEquals(AdFormat.VAST, configuration.getAdFormats().toArray()[0]);
        assertEquals(videoParameters, configuration.getVideoParameters());
        assertEquals(PlacementType.INTERSTITIAL.getValue(), configuration.getPlacementTypeValue());
        assertEquals(AdPosition.FULLSCREEN.getValue(), configuration.getAdPositionValue());
        assertTrue(configuration.isRewarded());

        HashSet<AdSize> configurationSizes = configuration.getSizes();
        assertThat(
                new HashSet<>(Arrays.asList(new AdSize(300, 250))),
                Matchers.equalTo(configurationSizes)
        );
    }

    @Test
    public void configuration_nativeParameters() {
        PrebidRequest request = new PrebidRequest();

        ArrayList<NativeAsset> assets = Lists.newArrayList();
        assets.add(new NativeTitleAsset());
        NativeParameters nativeParameters = new NativeParameters(assets);
        request.setNativeParameters(nativeParameters);


        subject = new MultiformatAdUnitFacade(configId, request);
        AdUnitConfiguration configuration = subject.getConfiguration();


        assertEquals(1, configuration.getAdFormats().size());
        assertEquals(AdFormat.NATIVE, configuration.getAdFormats().toArray()[0]);
        assertEquals(nativeParameters.getNativeConfiguration(), configuration.getNativeConfiguration());
    }

    @Test
    public void configuration_allBannerParameters() {
        PrebidRequest request = new PrebidRequest();

        BannerParameters bannerParameters = new BannerParameters();
        HashSet<AdSize> adSizes = new HashSet<>(Arrays.asList(new AdSize(320, 50)));
        bannerParameters.setAdSizes(adSizes);
        request.setBannerParameters(bannerParameters);

        VideoParameters videoParameters = new VideoParameters(Lists.newArrayList("video/mp4"));
        videoParameters.setAdSize(new AdSize(320, 480));
        request.setVideoParameters(videoParameters);

        ArrayList<NativeAsset> assets = Lists.newArrayList();
        assets.add(new NativeTitleAsset());
        NativeParameters nativeParameters = new NativeParameters(assets);
        request.setNativeParameters(nativeParameters);


        subject = new MultiformatAdUnitFacade(configId, request);
        AdUnitConfiguration configuration = subject.getConfiguration();


        assertEquals(bannerParameters, configuration.getBannerParameters());
        assertEquals(videoParameters, configuration.getVideoParameters());
        assertEquals(nativeParameters.getNativeConfiguration(), configuration.getNativeConfiguration());

        assertEquals(3, configuration.getAdFormats().size());
        ArrayList<AdFormat> expectedAdFormats = Lists.newArrayList(AdFormat.BANNER, AdFormat.NATIVE, AdFormat.VAST);
        assertArrayEquals(
                expectedAdFormats.toArray(),
                configuration.getAdFormats().toArray()
        );

        HashSet<AdSize> configurationSizes = configuration.getSizes();
        assertThat(
                new HashSet<>(Arrays.asList(new AdSize(320, 50), new AdSize(320, 480))),
                Matchers.equalTo(configurationSizes)
        );
    }

    @Test
    public void configuration_emptyParameters() {
        PrebidRequest request = new PrebidRequest();

        subject = new MultiformatAdUnitFacade(configId, request);

        AdUnitConfiguration configuration = subject.getConfiguration();
        EnumSet<AdFormat> adFormats = configuration.getAdFormats();
        assertTrue(adFormats.isEmpty());
    }

    @Test
    public void configuration_additionalParameters() {
        PrebidRequest request = new PrebidRequest();

        String expectedGpid = "/12345/home_screen#identifier";
        request.setGpid(expectedGpid);

        ContentObject expectedAppContent = new ContentObject();
        expectedAppContent.addCategory("Category");
        request.setAppContent(expectedAppContent);

        ArrayList<DataObject> expectedUserData = Lists.newArrayList(new DataObject());
        request.setUserData(expectedUserData);

        HashMap<String, Set<String>> expectedExtData = new HashMap<>();
        HashSet<String> data = new HashSet<>();
        data.add("Data 1");
        data.add("Data 2");
        expectedExtData.put("Key", data);
        request.setExtData(expectedExtData);

        HashSet<String> expectedExtKeywords = new HashSet<>();
        expectedExtKeywords.add("Keyword 1");
        expectedExtKeywords.add("Keyword 2");
        request.setExtKeywords(expectedExtKeywords);

        subject = new MultiformatAdUnitFacade(configId, request);

        AdUnitConfiguration configuration = subject.getConfiguration();

        assertEquals(
                expectedAppContent,
                configuration.getAppContent()
        );
        assertEquals(
                expectedUserData,
                configuration.getUserData()
        );
        assertEquals(
                expectedExtData,
                configuration.getExtDataDictionary()
        );
        assertEquals(
                expectedExtKeywords,
                configuration.getExtKeywordsSet()
        );
        assertEquals(
                expectedGpid,
                configuration.getGpid()
        );
    }


}