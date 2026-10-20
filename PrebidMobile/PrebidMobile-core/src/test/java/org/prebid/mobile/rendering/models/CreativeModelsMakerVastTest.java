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

package org.prebid.mobile.rendering.models;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.loading.AdLoadListener;
import org.prebid.mobile.rendering.loading.VastParserExtractor;
import org.prebid.mobile.rendering.models.internal.VastExtractorResult;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.MyShadowAsyncTask;
import org.prebid.mobile.rendering.networking.modelcontrollers.AsyncVastLoader;
import org.prebid.mobile.rendering.parser.AdResponseParserBase;
import org.prebid.mobile.rendering.parser.AdResponseParserVast;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.video.VideoAdEvent;
import org.prebid.mobile.rendering.video.VideoCreativeModel;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.prebid.mobile.rendering.video.VideoAdEvent.Event.AD_IMPRESSION;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, shadows = {MyShadowAsyncTask.class})
@SuppressWarnings("unchecked")
@LooperMode(LEGACY)
public class CreativeModelsMakerVastTest {

    private AdUnitConfiguration adConfiguration;
    private AdLoadListener mockListener;

    @Before
    public void setUp() throws Exception {
        adConfiguration = new AdUnitConfiguration();
        adConfiguration.setRewarded(true);
        mockListener = mock(AdLoadListener.class);
    }

    @Test
    public void testMakeModelsInline() throws Exception {
        String testFileName = "vast_inline_linear.xml";

        BaseNetworkTask.GetUrlResult adResponse = new BaseNetworkTask.GetUrlResult();
        adResponse.responseString = ResourceUtils.convertResourceToString(testFileName);

        CreativeModelsMakerVast creativeModelsMakerVast = new CreativeModelsMakerVast(null, mockListener);

        List<AdResponseParserBase> parsers = getVastParsers(adResponse);

        AdResponseParserVast rootParser = (AdResponseParserVast) parsers.get(0);
        AdResponseParserVast latestParser = (AdResponseParserVast) parsers.get(1);

        // Null ad configuration
        creativeModelsMakerVast.makeModels(null, rootParser, latestParser);
        verify(mockListener).onFailedToLoadAd(any(AdException.class), any());

        // Valid - Inline
        creativeModelsMakerVast.makeModels(adConfiguration, rootParser, latestParser);

        ArgumentCaptor<CreativeModelsMaker.Result> argumentCaptor = ArgumentCaptor.forClass(CreativeModelsMaker.Result.class);
        verify(mockListener).onCreativeModelReady(argumentCaptor.capture());

        CreativeModelsMaker.Result result = argumentCaptor.getValue();
        assertVastInline(result.creativeModels, false);
    }

    // To test wrappers, we use a mock server to handle the vast wrapper redirect
    @Test
    public void testMakeModelsWrapper() throws Exception {
        String testWrapperFileName = "test_ad_response_vast_wrapper_macro_ad_tag";
        String testInlineFileName = "vast_inline_linear.xml";

        AppInfoManager.setUserAgent("user-agent");

        // Set mock server response as inline vast
        String inlineVastString = ResourceUtils.convertResourceToString(testInlineFileName);
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                                                .setBody(inlineVastString));

        // Replace vast ad tag macro inside wrapper with mock server URL
        HttpUrl baseUrl = mockWebServer.url("/vast_wrapper");
        String urlString = baseUrl.url().toString();
        String responseString = ResourceUtils.convertResourceToString(testWrapperFileName);
        responseString = responseString.replace("%%VAST_AD_TAG%%", urlString);

        // Set wrapper ad response as parameter for makeModels()
        BaseNetworkTask.GetUrlResult adResponse = new BaseNetworkTask.GetUrlResult();
        adResponse.responseString = responseString;

        List<AdResponseParserBase> parsers = getVastParsers(adResponse);

        AdResponseParserVast rootParser = (AdResponseParserVast) parsers.get(0);
        AdResponseParserVast latestParser = (AdResponseParserVast) parsers.get(1);

        // Execute makeModels()
        CreativeModelsMakerVast creativeModelsMakerVast = new CreativeModelsMakerVast(null, mockListener);
        creativeModelsMakerVast.makeModels(adConfiguration, rootParser, latestParser);

        // Get result
        ArgumentCaptor<CreativeModelsMaker.Result> argumentCaptor = ArgumentCaptor.forClass(CreativeModelsMaker.Result.class);
        verify(mockListener).onCreativeModelReady(argumentCaptor.capture());

        CreativeModelsMaker.Result result = argumentCaptor.getValue();
        VideoCreativeModel model = (VideoCreativeModel) result.creativeModels.get(0);

        // Assert wrapper specific events
        HashMap<VideoAdEvent.Event, ArrayList<String>> videoEventUrls = model.getVideoEventUrls();
        assertEquals("http://myTrackingURL/wrapper/click",
                     videoEventUrls.get(VideoAdEvent.Event.AD_CLICK).get(0));
        videoEventUrls.get(VideoAdEvent.Event.AD_CLICK).remove(0);
        assertEquals("http://myTrackingURL/wrapper/impression",
                     videoEventUrls.get(VideoAdEvent.Event.AD_IMPRESSION).get(0));
        videoEventUrls.get(VideoAdEvent.Event.AD_IMPRESSION).remove(0);
        assertEquals("http://myTrackingURL/wrapper/creativeView",
                     videoEventUrls.get(VideoAdEvent.Event.AD_CREATIVEVIEW).get(0));
        videoEventUrls.get(VideoAdEvent.Event.AD_CREATIVEVIEW).remove(0);

        // Assert inline
        assertVastInline(result.creativeModels, true);

        mockWebServer.shutdown();
    }

    @Test
    public void testNonOptInCompanion() throws Exception {
        String testFileName = "vast_inline_linear.xml";

        BaseNetworkTask.GetUrlResult adResponse = new BaseNetworkTask.GetUrlResult();
        adResponse.responseString = ResourceUtils.convertResourceToString(testFileName);

        CreativeModelsMakerVast creativeModelsMakerVast = new CreativeModelsMakerVast(null, mockListener);

        List<AdResponseParserBase> parsers = getVastParsers(adResponse);

        AdResponseParserVast rootParser = (AdResponseParserVast) parsers.get(0);
        AdResponseParserVast latestParser = (AdResponseParserVast) parsers.get(1);

        // Valid - Inline, non-opt-in
        adConfiguration.setRewarded(false);
        creativeModelsMakerVast.makeModels(adConfiguration, rootParser, latestParser);

        ArgumentCaptor<CreativeModelsMaker.Result> argumentCaptor = ArgumentCaptor.forClass(CreativeModelsMaker.Result.class);
        verify(mockListener).onCreativeModelReady(argumentCaptor.capture());

        CreativeModelsMaker.Result result = argumentCaptor.getValue();

        // Expect only two video creative model, because contains companion ad
        assertEquals(2, result.creativeModels.size());
        assertTrue(result.creativeModels.get(0) instanceof VideoCreativeModel);
    }

    @Test
    public void testRightLimits() throws Exception {
        // Subject initialization
        String testFileName = "vast_inline_linear.xml";

        BaseNetworkTask.GetUrlResult adResponse = new BaseNetworkTask.GetUrlResult();
        adResponse.responseString = ResourceUtils.convertResourceToString(testFileName);

        CreativeModelsMakerVast creativeModelsMakerVast = new CreativeModelsMakerVast(null, mockListener);

        List<AdResponseParserBase> parsers = getVastParsers(adResponse);

        AdResponseParserVast rootParser = (AdResponseParserVast) parsers.get(0);
        AdResponseParserVast latestParser = (AdResponseParserVast) parsers.get(1);

        // Current video duration 96 sec
        adConfiguration.setMaxVideoDuration(100);

        // Run
        creativeModelsMakerVast.makeModels(adConfiguration, rootParser, latestParser);

        // Check
        verify(mockListener).onCreativeModelReady(any());
    }

    @Test
    public void testWrongLimits() throws Exception {
        // Subject initialization
        String testFileName = "vast_inline_linear.xml";

        BaseNetworkTask.GetUrlResult adResponse = new BaseNetworkTask.GetUrlResult();
        adResponse.responseString = ResourceUtils.convertResourceToString(testFileName);

        CreativeModelsMakerVast creativeModelsMakerVast = new CreativeModelsMakerVast(null, mockListener);

        List<AdResponseParserBase> parsers = getVastParsers(adResponse);

        AdResponseParserVast rootParser = (AdResponseParserVast) parsers.get(0);
        AdResponseParserVast latestParser = (AdResponseParserVast) parsers.get(1);

        // Current video duration 96 sec
        adConfiguration.setMaxVideoDuration(90);

        // Run
        creativeModelsMakerVast.makeModels(adConfiguration, rootParser, latestParser);

        // Check
        ArgumentCaptor<AdException> errorMessage = ArgumentCaptor.forClass(AdException.class);
        verify(mockListener).onFailedToLoadAd(errorMessage.capture(), any());

        MatcherAssert.assertThat(
                errorMessage.getValue().getMessage(),
                containsString("Video duration can't be more then ad unit max video duration")
        );
    }

    // An ad may have several Linear creatives. The video and everything that belongs to it come from
    // the first Linear with a media file the SDK can play.
    @Test
    public void testMakeModels_unsupportedLinearBeforeSupportedLinear() throws Exception {
        String xml = vast(inlineAd("1",
                linear("vpaid", "application/javascript", "00:00:10", 640, 480),
                linear("mp4", "video/mp4", "00:00:15", 640, 480)
        ));

        CreativeModelsMaker.Result result = makeModels(xml);

        assertEquals(1, result.creativeModels.size());
        VideoCreativeModel videoModel = (VideoCreativeModel) result.creativeModels.get(0);
        assertEquals("https://example.com/mp4.media", videoModel.getMediaUrl());
        assertEquals(15_000, videoModel.getMediaDuration());
        assertEquals(5_000, videoModel.getSkipOffset());
        assertEquals("https://example.com/mp4/clickThrough", videoModel.getVastClickthroughUrl());
        HashMap<VideoAdEvent.Event, ArrayList<String>> videoEventUrls = videoModel.getVideoEventUrls();
        assertEquals(Arrays.asList("https://example.com/mp4/start"), videoEventUrls.get(VideoAdEvent.Event.AD_START));
        assertEquals(Arrays.asList("https://example.com/mp4/click"), videoEventUrls.get(VideoAdEvent.Event.AD_CLICK));
    }

    @Test
    public void testMakeModels_supportedLinearBeforeUnsupportedLinear() throws Exception {
        String xml = vast(inlineAd("1",
                linear("mp4", "video/mp4", "00:00:15", 640, 480),
                linear("vpaid", "application/javascript", "00:00:10", 640, 480)
        ));

        CreativeModelsMaker.Result result = makeModels(xml);

        VideoCreativeModel videoModel = (VideoCreativeModel) result.creativeModels.get(0);
        assertEquals("https://example.com/mp4.media", videoModel.getMediaUrl());
        assertEquals(15_000, videoModel.getMediaDuration());
        assertEquals(Arrays.asList("https://example.com/mp4/start"), videoModel.getVideoEventUrls().get(VideoAdEvent.Event.AD_START));
    }

    // The media file used to be picked from all the Linear creatives and the duration from the first one.
    @Test
    public void testMakeModels_twoPlayableLinears() throws Exception {
        String xml = vast(inlineAd("1",
                linear("small", "video/mp4", "00:00:15", 640, 480),
                linear("big", "video/mp4", "00:00:30", 1280, 720)
        ));

        CreativeModelsMaker.Result result = makeModels(xml);

        VideoCreativeModel videoModel = (VideoCreativeModel) result.creativeModels.get(0);
        assertEquals("https://example.com/small.media", videoModel.getMediaUrl());
        assertEquals(15_000, videoModel.getMediaDuration());
        assertEquals(Arrays.asList("https://example.com/small/start"), videoModel.getVideoEventUrls().get(VideoAdEvent.Event.AD_START));
    }

    // The same across ads: the first ad has no playable Linear, the second one does.
    // Everything in the models has to come from the ad that gets rendered.
    @Test
    public void testMakeModels_unplayableAdBeforePlayableAd() throws Exception {
        String xml = vast(
                inlineAd("1",
                        linear("vpaid", "application/javascript", "00:00:10", 640, 480),
                        companion("https://example.com/companion-1.png")
                ),
                inlineAd("2",
                        linear("mp4", "video/mp4", "00:00:15", 640, 480),
                        companion("https://example.com/companion-2.png")
                )
        );

        CreativeModelsMaker.Result result = makeModels(xml);

        assertEquals(2, result.creativeModels.size());
        VideoCreativeModel videoModel = (VideoCreativeModel) result.creativeModels.get(0);
        assertEquals("https://example.com/mp4.media", videoModel.getMediaUrl());
        assertEquals(15_000, videoModel.getMediaDuration());
        HashMap<VideoAdEvent.Event, ArrayList<String>> videoEventUrls = videoModel.getVideoEventUrls();
        assertEquals(Arrays.asList("https://example.com/imp/2"), videoEventUrls.get(AD_IMPRESSION));
        assertEquals(Arrays.asList("https://example.com/error/2"), videoEventUrls.get(VideoAdEvent.Event.AD_ERROR));
        assertEquals(Arrays.asList("https://example.com/mp4/start"), videoEventUrls.get(VideoAdEvent.Event.AD_START));

        String endCardHtml = result.creativeModels.get(1).getHtml();
        assertTrue(endCardHtml.contains("https://example.com/companion-2.png"));
        assertFalse(endCardHtml.contains("https://example.com/companion-1.png"));
    }

    // The wrapper keeps contributing its own ad while the second ad of the wrapped response is rendered.
    @Test
    public void testMakeModels_wrapperThenPlayableSecondAd() throws Exception {
        String wrapperXml = vast(
                "<Ad id=\"w\"><Wrapper><AdSystem>test</AdSystem>"
                + "<VASTAdTagURI><![CDATA[https://example.com/wrapped]]></VASTAdTagURI>"
                + "<Impression><![CDATA[https://example.com/imp/w]]></Impression>"
                + "<Creatives><Creative><Linear><TrackingEvents>"
                + "<Tracking event=\"start\"><![CDATA[https://example.com/w/start]]></Tracking>"
                + "</TrackingEvents></Linear></Creative></Creatives>"
                + "</Wrapper></Ad>"
        );
        String wrappedXml = vast(
                inlineAd("1", linear("vpaid", "application/javascript", "00:00:10", 640, 480)),
                inlineAd("2", linear("mp4", "video/mp4", "00:00:15", 640, 480))
        );
        AdResponseParserVast rootParser = new AdResponseParserVast(wrapperXml);
        AdResponseParserVast latestParser = new AdResponseParserVast(wrappedXml);
        rootParser.setWrapper(latestParser);

        CreativeModelsMaker.Result result = makeModels(rootParser, latestParser);

        VideoCreativeModel videoModel = (VideoCreativeModel) result.creativeModels.get(0);
        assertEquals("https://example.com/mp4.media", videoModel.getMediaUrl());
        assertEquals(15_000, videoModel.getMediaDuration());
        HashMap<VideoAdEvent.Event, ArrayList<String>> videoEventUrls = videoModel.getVideoEventUrls();
        assertEquals(Arrays.asList("https://example.com/imp/w", "https://example.com/imp/2"), videoEventUrls.get(AD_IMPRESSION));
        assertEquals(Arrays.asList("https://example.com/w/start", "https://example.com/mp4/start"), videoEventUrls.get(VideoAdEvent.Event.AD_START));
    }

    // Without a playable Linear the first one is still used, and the load fails later for the missing media file.
    @Test
    public void testMakeModels_noSupportedMedia() throws Exception {
        String xml = vast(
                inlineAd("1", linear("vpaid", "application/javascript", "00:00:10", 640, 480)),
                inlineAd("2", linear("flv", "video/x-flv", "00:00:15", 640, 480))
        );

        CreativeModelsMaker.Result result = makeModels(xml);

        VideoCreativeModel videoModel = (VideoCreativeModel) result.creativeModels.get(0);
        assertNull(videoModel.getMediaUrl());
        assertEquals(10_000, videoModel.getMediaDuration());
        assertEquals(Arrays.asList("https://example.com/imp/1"), videoModel.getVideoEventUrls().get(AD_IMPRESSION));
    }

    private CreativeModelsMaker.Result makeModels(String inlineXml) throws Exception {
        AdResponseParserVast parser = new AdResponseParserVast(inlineXml);
        return makeModels(parser, parser);
    }

    private CreativeModelsMaker.Result makeModels(AdResponseParserVast rootParser, AdResponseParserVast latestParser) {
        new CreativeModelsMakerVast(null, mockListener).makeModels(adConfiguration, rootParser, latestParser);

        ArgumentCaptor<CreativeModelsMaker.Result> argumentCaptor = ArgumentCaptor.forClass(CreativeModelsMaker.Result.class);
        verify(mockListener).onCreativeModelReady(argumentCaptor.capture());
        return argumentCaptor.getValue();
    }

    private static String vast(String... ads) {
        return "<VAST version=\"3.0\">" + String.join("", ads) + "</VAST>";
    }

    private static String inlineAd(String id, String... creatives) {
        return "<Ad id=\"" + id + "\"><InLine><AdSystem>test</AdSystem><AdTitle>t</AdTitle>"
               + "<Error><![CDATA[https://example.com/error/" + id + "]]></Error>"
               + "<Impression><![CDATA[https://example.com/imp/" + id + "]]></Impression>"
               + "<Creatives>" + String.join("", creatives) + "</Creatives>"
               + "</InLine></Ad>";
    }

    // Every URL of the creative starts with https://example.com/<name>, so the tests can tell where it came from.
    private static String linear(String name, String mimeType, String duration, int width, int height) {
        String base = "https://example.com/" + name;
        return "<Creative><Linear skipoffset=\"00:00:05\">"
               + "<Duration>" + duration + "</Duration>"
               + "<TrackingEvents><Tracking event=\"start\"><![CDATA[" + base + "/start]]></Tracking></TrackingEvents>"
               + "<VideoClicks>"
               + "<ClickThrough><![CDATA[" + base + "/clickThrough]]></ClickThrough>"
               + "<ClickTracking><![CDATA[" + base + "/click]]></ClickTracking>"
               + "</VideoClicks>"
               + "<MediaFiles><MediaFile delivery=\"progressive\" type=\"" + mimeType + "\" width=\"" + width + "\" height=\"" + height + "\">"
               + "<![CDATA[" + base + ".media]]></MediaFile></MediaFiles>"
               + "</Linear></Creative>";
    }

    private static String companion(String imageUrl) {
        return "<Creative><CompanionAds><Companion width=\"300\" height=\"250\">"
               + "<StaticResource creativeType=\"image/png\"><![CDATA[" + imageUrl + "]]></StaticResource>"
               + "<CompanionClickThrough><![CDATA[https://example.com/companion/click]]></CompanionClickThrough>"
               + "</Companion></CompanionAds></Creative>";
    }

    private List<AdResponseParserBase> getVastParsers(BaseNetworkTask.GetUrlResult adResponse) throws NoSuchFieldException, IllegalAccessException {

        final VastParserExtractor.Listener mockListener = mock(VastParserExtractor.Listener.class);
        VastParserExtractor parserExtractor = new VastParserExtractor(mockListener);

        AsyncVastLoader asyncVastLoader = spy(new AsyncVastLoader());

        Field requesterVastField = VastParserExtractor.class.getDeclaredField("asyncVastLoader");
        requesterVastField.setAccessible(true);
        requesterVastField.set(parserExtractor, asyncVastLoader);

        parserExtractor.extract(adResponse.responseString);

        ArgumentCaptor<VastExtractorResult> varArgsCapture = ArgumentCaptor.forClass(VastExtractorResult.class);
        verify(mockListener).onResult(varArgsCapture.capture());

        final AdResponseParserBase[] vastResponseParserArray = varArgsCapture.getValue().getVastResponseParserArray();
        return Arrays.asList(vastResponseParserArray);
    }

    private void assertVastInline(List<CreativeModel> creativeModels, boolean isWrapper) {
        VideoCreativeModel videoModel = (VideoCreativeModel) creativeModels.get(0);

        assertEquals("http://i-cdn.prebid.com/5a7/5a731840-5ae7-4dca-ba66-6e959bb763e2/be2/be2cf3b2cf0648e0aa46c7c09afaf3f4.mp4",
                     videoModel.getMediaUrl());
        assertEquals(Utils.getMsFrom("00:01:36"), videoModel.getMediaDuration());
        assertEquals(
                "http://oxv4support-d3.prebidenterprise.com/v/1.0/rc?did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                videoModel.getVastClickthroughUrl()
        );
        assertEquals(adConfiguration, videoModel.getAdConfiguration());
        assertEquals(CreativeModelsMakerVast.VIDEO_CREATIVE_TAG, videoModel.getName());

        HashMap<VideoAdEvent.Event, ArrayList<String>> videoEventUrls = videoModel.getVideoEventUrls();
        // for wrapper case AD_ERROR is null cause of Error object is taken from InLine object, for Wrapper case it's null
        if (!isWrapper) {
            assertEquals("http://myErrorURL/error",
                         videoEventUrls.get(VideoAdEvent.Event.AD_ERROR).get(0));
        }
        assertEquals("http://myTrackingURL/click1",
                     videoEventUrls.get(VideoAdEvent.Event.AD_CLICK).get(0));
        assertEquals("http://myTrackingURL/click2",
                     videoEventUrls.get(VideoAdEvent.Event.AD_CLICK).get(1));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=unmute&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_UNMUTE).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=expand&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_EXPAND).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=creativeView&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_CREATIVEVIEW).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=close&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_CLOSE).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=acceptInvitation&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_ACCEPTINVITATION).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=complete&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_COMPLETE).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=mute&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_MUTE).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=pause&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_PAUSE).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=midpoint&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_MIDPOINT).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=start&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_START).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=resume&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_RESUME).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=thirdQuartile&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_THIRDQUARTILE).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=fullscreen&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_FULLSCREEN).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=collapse&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_COLLAPSE).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=firstQuartile&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_FIRSTQUARTILE).get(0));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/ri?did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(AD_IMPRESSION).get(0));
        assertEquals("http://myTrackingURL/anotherImpression",
                     videoEventUrls.get(AD_IMPRESSION).get(1));
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rv?t=rewind&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_REWIND).get(0));

        CreativeModel endCardModel = creativeModels.get(1);

        assertEquals("http://www.tremormedia.com", endCardModel.getClickUrl());

        HashMap<TrackingEvent.Events, ArrayList<String>> trackingURLs = endCardModel.trackingURLs;
        assertEquals("http://www.CompanionClickTracking.com",
                     trackingURLs.get(TrackingEvent.Events.CLICK).get(0));
        assertEquals("http://myTrackingURL/firstCompanionCreativeView",
                     trackingURLs.get(TrackingEvent.Events.IMPRESSION).get(0));
    }
}