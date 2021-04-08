package org.prebid.mobile.rendering.models;

import com.apollo.test.utils.ResourceUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.prebid.mobile.rendering.errors.AdException;
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
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.prebid.mobile.rendering.video.VideoAdEvent.Event.AD_IMPRESSION;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, shadows = {MyShadowAsyncTask.class})
@SuppressWarnings("unchecked")
public class CreativeModelsMakerVastTest {

    private AdConfiguration mAdConfiguration;
    private AdLoadListener mMockListener;

    @Before
    public void setUp() throws Exception {
        mAdConfiguration = new AdConfiguration();
        mAdConfiguration.setRewarded(true);
        mMockListener = mock(AdLoadListener.class);
    }

    @Test
    public void testMakeModelsInline() throws Exception {
        String testFileName = "vast_inline_linear.xml";

        BaseNetworkTask.GetUrlResult adResponse = new BaseNetworkTask.GetUrlResult();
        adResponse.responseString = ResourceUtils.convertResourceToString(testFileName);

        CreativeModelsMakerVast creativeModelsMakerVast = new CreativeModelsMakerVast(null, mMockListener);

        List<AdResponseParserBase> parsers = getVastParsers(adResponse);

        AdResponseParserVast rootParser = (AdResponseParserVast) parsers.get(0);
        AdResponseParserVast latestParser = (AdResponseParserVast) parsers.get(1);

        // Null ad configuration
        creativeModelsMakerVast.makeModels(null, rootParser, latestParser);
        verify(mMockListener).onFailedToLoadAd(any(AdException.class), anyString());

        // Valid - Inline
        creativeModelsMakerVast.makeModels(mAdConfiguration, rootParser, latestParser);

        ArgumentCaptor<CreativeModelsMaker.Result> argumentCaptor = ArgumentCaptor.forClass(CreativeModelsMaker.Result.class);
        verify(mMockListener).onCreativeModelReady(argumentCaptor.capture());

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
        CreativeModelsMakerVast creativeModelsMakerVast = new CreativeModelsMakerVast(null, mMockListener);
        creativeModelsMakerVast.makeModels(mAdConfiguration, rootParser, latestParser);

        // Get result
        ArgumentCaptor<CreativeModelsMaker.Result> argumentCaptor = ArgumentCaptor.forClass(CreativeModelsMaker.Result.class);
        verify(mMockListener).onCreativeModelReady(argumentCaptor.capture());

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

        CreativeModelsMakerVast creativeModelsMakerVast = new CreativeModelsMakerVast(null, mMockListener);

        List<AdResponseParserBase> parsers = getVastParsers(adResponse);

        AdResponseParserVast rootParser = (AdResponseParserVast) parsers.get(0);
        AdResponseParserVast latestParser = (AdResponseParserVast) parsers.get(1);

        // Valid - Inline, non-opt-in
        mAdConfiguration.setRewarded(false);
        creativeModelsMakerVast.makeModels(mAdConfiguration, rootParser, latestParser);

        ArgumentCaptor<CreativeModelsMaker.Result> argumentCaptor = ArgumentCaptor.forClass(CreativeModelsMaker.Result.class);
        verify(mMockListener).onCreativeModelReady(argumentCaptor.capture());

        CreativeModelsMaker.Result result = argumentCaptor.getValue();

        // Expect only two video creative model, because contains companion ad
        assertEquals(2, result.creativeModels.size());
        assertTrue(result.creativeModels.get(0) instanceof VideoCreativeModel);
    }

    private List<AdResponseParserBase> getVastParsers(BaseNetworkTask.GetUrlResult adResponse)
    throws
    NoSuchFieldException,
    IllegalAccessException {

        final VastParserExtractor.Listener mockListener = mock(VastParserExtractor.Listener.class);
        VastParserExtractor parserExtractor = new VastParserExtractor(mockListener);

        AsyncVastLoader asyncVastLoader = spy(new AsyncVastLoader());

        Field requesterVastField = VastParserExtractor.class.getDeclaredField("mAsyncVastLoader");
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

        assertEquals("http://i-cdn.openx.com/5a7/5a731840-5ae7-4dca-ba66-6e959bb763e2/be2/be2cf3b2cf0648e0aa46c7c09afaf3f4.mp4",
                     videoModel.getMediaUrl());
        assertEquals(Utils.getMsFrom("00:01:36"), videoModel.getMediaDuration());
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rc?did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoModel.getVastClickthroughUrl());
        assertEquals(mAdConfiguration, videoModel.getAdConfiguration());
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
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=unmute&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_UNMUTE).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=expand&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_EXPAND).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=creativeView&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_CREATIVEVIEW).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=close&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_CLOSE).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=acceptInvitation&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_ACCEPTINVITATION).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=complete&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_COMPLETE).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=mute&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_MUTE).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=pause&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_PAUSE).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=midpoint&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_MIDPOINT).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=start&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_START).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=resume&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_RESUME).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=thirdQuartile&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_THIRDQUARTILE).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=fullscreen&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_FULLSCREEN).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=collapse&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_COLLAPSE).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=firstQuartile&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_FIRSTQUARTILE).get(0));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/ri?did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(AD_IMPRESSION).get(0));
        assertEquals("http://myTrackingURL/anotherImpression",
                     videoEventUrls.get(AD_IMPRESSION).get(1));
        assertEquals("http://oxv4support-d3.openxenterprise.com/v/1.0/rv?t=rewind&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51",
                     videoEventUrls.get(VideoAdEvent.Event.AD_REWIND).get(0));

        CreativeModel endCardModel = creativeModels.get(1);

        assertEquals("http://www.tremormedia.com", endCardModel.getClickUrl());

        HashMap<TrackingEvent.Events, ArrayList<String>> trackingURLs = endCardModel.mTrackingURLs;
        assertEquals("http://www.CompanionClickTracking.com",
                     trackingURLs.get(TrackingEvent.Events.CLICK).get(0));
        assertEquals("http://myTrackingURL/firstCompanionCreativeView",
                     trackingURLs.get(TrackingEvent.Events.IMPRESSION).get(0));
    }
}