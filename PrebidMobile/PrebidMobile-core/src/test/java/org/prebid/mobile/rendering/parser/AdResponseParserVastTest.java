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

package org.prebid.mobile.rendering.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.errors.VastParseError;
import org.prebid.mobile.rendering.video.VideoAdEvent;
import org.prebid.mobile.rendering.video.vast.*;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AdResponseParserVastTest {

    private final static String SAMPLE_GOOD_VAST = "vast_inline_linear.xml";
    private final static String SAMPLE_INLINE_ERROR_VAST = "vast_inline_error.xml";
    private final static String SAMPEL_INLINE_NONLINEAR = "vast_inline_nonlinear.xml";
    private final static String WRONG_VAST = "vast_xmlError.xml";
    private final static String VAST_ERROR = "vast_error.xml";
    private final static String VAST_WRAPPER_LINEAR_NONLINEAR = "vast_wrapper_linear_nonlinear.xml";
    private final static String ADVERIFICATIONS_IN_EXTENSIONS = "vast_adverifications_in_extensions.xml";
    private final static String VAST_BOM = "vast_bom";
    private final static String VAST_CORRECT = "vast.xml";
    private final static String WRONG_VAST_WITH_NO_AD = "vast_noad.xml";

    @Test
    public void testSupportedVideoFormat() throws Exception {
        String type3gp = "video/3gp";
        assertFalse("Input is not video/3gpp. Hence not supported" + type3gp, AdResponseParserVast.supportedVideoFormat(type3gp));
        String type3gpp = "video/3gpp";
        assertTrue("Input is not video/3gpp. Hence not supported" + type3gpp, AdResponseParserVast.supportedVideoFormat(type3gpp));
        String typeMp4 = "video/mp4";
        assertTrue("Input is not video/mp4. Hence not supported" + typeMp4, AdResponseParserVast.supportedVideoFormat(typeMp4));
        String typewebM = "video/webm";
        assertTrue("Input is not video/webm. Hence not supported" + typewebM, AdResponseParserVast.supportedVideoFormat(typewebM));
        String wrongWebm = "video/webmm";
        assertFalse("Input is not video/webm. Hence not supported" + wrongWebm, AdResponseParserVast.supportedVideoFormat(wrongWebm));
        String typeMkv = "video/mkv";
        assertTrue("Input is not video/mkv. Hence not supported" + typeMkv, AdResponseParserVast.supportedVideoFormat(typeMkv));
        String wrongMkv = "video/mkvmkv";
        assertFalse("Input is not video/mkv. Hence not supported" + wrongMkv, AdResponseParserVast.supportedVideoFormat(wrongMkv));
    }

    @Test
    public void testInlineNonLinear() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(SAMPEL_INLINE_NONLINEAR);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);
        assertEquals("NonLinear Test Campaign 1", tempVast.getVast().getAds().get(0).getInline().getAdTitle().getValue());
        assertEquals("NonLinear Test Campaign 1", tempVast.getVast().getAds().get(0).getInline().getDescription()
                                                          .getValue());
        assertEquals("http://mySurveyURL/survey", tempVast.getVast().getAds().get(0).getInline().getSurvey().getValue());
        assertEquals("602678-NonLinear", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (0).getAdID());
        assertEquals("creativeView", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (0).getNonLinearAds().getTrackingEvents().get(0).getEvent());
        assertEquals("http://myTrackingURL/nonlinear/creativeView", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (0).getNonLinearAds().getTrackingEvents().get(0).getValue());

        assertEquals("300", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (0).getNonLinearAds().getNonLinearAds().get(0).getWidth());

        assertEquals("50", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (0).getNonLinearAds().getNonLinearAds().get(0).getHeight());

        assertEquals("00:00:15", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (0).getNonLinearAds().getNonLinearAds().get(0).getMinSuggestedDuration());

        assertEquals("image/jpeg", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (0).getNonLinearAds().getNonLinearAds().get(0).getStaticResource().getCreativeType());

        assertEquals("http://demo.tremormedia.com/proddev/vast/50x300_static.jpg", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (0).getNonLinearAds().getNonLinearAds().get(0).getStaticResource().getValue());

        assertEquals("http://www.tremormedia.com", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (0).getNonLinearAds().getNonLinearAds().get(0).getNonLinearClickThrough().getValue());

        assertEquals("602678-Companion", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (1).getAdID());

        assertEquals("728", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (1).getCompanionAds().get(0).getWidth());

        assertEquals("90", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (1).getCompanionAds().get(0).getHeight());

        assertEquals("image/jpeg", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (1).getCompanionAds().get(0).getStaticResource().getCreativeType());

        assertEquals("http://demo.tremormedia.com/proddev/vast/728x90_banner1.jpg", tempVast.getVast()
                                                                                            .getAds().get(0).getInline().getCreatives().get
                (1).getCompanionAds().get(0).getStaticResource().getValue());

        assertEquals("creativeView", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (1).getCompanionAds().get(0).getTrackingEvents().get(0).getEvent());

        assertEquals("http://myTrackingURL/secondCompanion", tempVast.getVast().getAds().get(0).getInline()
                                                                     .getCreatives().get
                (1).getCompanionAds().get(0).getTrackingEvents().get(0).getValue());
        assertEquals("http://www.tremormedia.com", tempVast.getVast().getAds().get(0).getInline().getCreatives().get
            (1).getCompanionAds().get(0).getCompanionClickThrough().getValue());
    }

    @Test
    public void testVastError() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(VAST_ERROR);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);
        assertNotNull(tempVast.getVast().getError());
    }

    @Test
    public void testGetClickTrackingUrl() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(SAMPLE_GOOD_VAST);
        AdResponseParserVastHelper vast = new AdResponseParserVastHelper(vastXML);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);

        tempVast.setWrapper(vast);
        assertNotNull(tempVast.getClickTrackingUrl());
        assertNotNull(tempVast.getImpressionTrackerUrl());
        assertNotNull(tempVast.getTrackings());
        assertNotNull(tempVast.getImpressions());
        assertNotNull(tempVast.getClickTrackings());
    }

    @Test
    public void testGetMediaFileUrl() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(SAMPLE_GOOD_VAST);
        AdResponseParserVastHelper vast = new AdResponseParserVastHelper(vastXML);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);
        assertEquals("http://i-cdn.prebid" +
                     ".com/5a7/5a731840-5ae7-4dca-ba66-6e959bb763e2/be2" +
                     "/be2cf3b2cf0648e0aa46c7c09afaf3f4.mp4", tempVast.getMediaFileUrl(vast, 0));
    }

    @Test
    public void testInlineGetImpressions() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(SAMPLE_GOOD_VAST);
        AdResponseParserVastHelper vast = new AdResponseParserVastHelper(vastXML);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/ri?did" +
                     ".adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts" +
                     "=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51", tempVast.getImpressions(vast, 0).get(0).getValue());
    }

    @Test
    public void testWrapperGetImpressions() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(VAST_WRAPPER_LINEAR_NONLINEAR);
        AdResponseParserVastHelper vast = new AdResponseParserVastHelper(vastXML);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);
        assertEquals("http://myTrackingURL/wrapper/impression", tempVast.getImpressions(vast, 0)
                                                                        .get(0).getValue());
    }

    @Test
    public void testGetAllTrackings() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(VAST_WRAPPER_LINEAR_NONLINEAR);
        AdResponseParserVastHelper vast = new AdResponseParserVastHelper(vastXML);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);
        ArrayList<Tracking> expectedTrackings = new ArrayList<>();
        Tracking expectedTrackingUrl = mock(Tracking.class);
        expectedTrackingUrl.setEvent("creativeView");
        when(expectedTrackingUrl.getValue()).thenReturn("http://myTrackingURL/wrapper/creativeView");
        expectedTrackings.add(expectedTrackingUrl);
        ArrayList<Tracking> actual = tempVast.getAllTrackings(vast, 0);
        assertEquals(expectedTrackings.get(0).getValue(), actual.get(0).getValue());
    }

    @Test
    public void testGetTrackingByType() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(VAST_WRAPPER_LINEAR_NONLINEAR);
        AdResponseParserVastHelper vast = new AdResponseParserVastHelper(vastXML);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);
        tempVast.getAllTrackings(vast, 0);
        assertEquals("http://myTrackingURL/wrapper/creativeView", tempVast.getTrackingByType
            (VideoAdEvent.Event.AD_CREATIVEVIEW).get(0));
    }

    @Test
    public void testGetClickThroughUrl() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(SAMPLE_GOOD_VAST);
        AdResponseParserVastHelper vast = new AdResponseParserVastHelper(vastXML);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);

        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rc?did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51", tempVast.getClickThroughUrl(vast, 0));
    }

    @Test
    public void testInlineGetClickTrackings() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(SAMPLE_GOOD_VAST);
        AdResponseParserVastHelper vast = new AdResponseParserVastHelper(vastXML);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);

        assertEquals("http://myTrackingURL/click1", tempVast.getClickTrackings(vast, 0).get(0).getValue());
    }

    @Test
    public void testWrapperGetClickTrackings() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(VAST_WRAPPER_LINEAR_NONLINEAR);
        AdResponseParserVastHelper vast = new AdResponseParserVastHelper(vastXML);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);

        assertEquals("http://myTrackingURL/wrapper/click", tempVast.getClickTrackings(vast, 0)
                                                                   .get(0).getValue());
    }

    @Test
    public void testGetTrackingEvents() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(SAMPLE_GOOD_VAST);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);
        assertEquals("creativeView", tempVast.getTrackingEvents(tempVast.getVast(), 0).get(0).getEvent());
        assertEquals("start", tempVast.getTrackingEvents(tempVast.getVast(), 0).get(1).getEvent());
    }

    @Test
    public void testWrapperAdTagURI() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(VAST_WRAPPER_LINEAR_NONLINEAR);
        AdResponseParserVastHelper mainVast = new AdResponseParserVastHelper(vastXML);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);

        assertNotNull(mainVast.getVastUrl());
        assertEquals("http://SecondaryAdServer.vast.tag", mainVast.getVastUrl());
        assertNotNull(tempVast.getImpressionEvents(mainVast.getVast(), 0));
        assertEquals("Acudeo Compatible", tempVast.getVast().getAds().get(0).getWrapper().getAdSystem().getValue());
        assertEquals("http://SecondaryAdServer.vast.tag", tempVast.getVast().getAds().get(0).getWrapper().getVastUrl().getValue());
        assertEquals("http://myErrorURL/wrapper/error", tempVast.getVast().getAds().get(0).getWrapper().getError()
                                                                .getValue());
        assertEquals("http://myTrackingURL/wrapper/impression", tempVast.getVast().getAds().get(0).getWrapper()
                                                                        .getImpressions().get(0).getValue());

        assertEquals("602833", tempVast.getVast().getAds().get(0).getWrapper()
                                       .getCreatives().get(0).getAdID());

        assertEquals(1, tempVast.getVast().getAds().get(0).getWrapper()
                                .getCreatives().get(0).getLinear().getTrackingEvents().size());
        assertEquals("creativeView", tempVast.getVast().getAds().get(0).getWrapper()
                .getCreatives().get(0).getLinear().getTrackingEvents().get(0).getEvent());
        assertEquals("http://myTrackingURL/wrapper/creativeView", tempVast.getVast().getAds().get(0).getWrapper()
                .getCreatives().get(0).getLinear().getTrackingEvents().get(0).getValue());

        assertEquals("http://myTrackingURL/wrapper/click", tempVast.getVast().getAds().get(0).getWrapper()
                .getCreatives().get(1).getLinear().getVideoClicks().getClickTrackings().get(0).getValue());

        assertEquals(0, tempVast.getVast().getAds().get(0).getWrapper()
                .getCreatives().get(2).getNonLinearAds().getTrackingEvents().size());
        assertEquals("samplexmlEncoded", tempVast.getVast().getAds().get(0).getWrapper().getCreatives().get(0).getLinear().getAdParameters().getXmlEncoded());
        assertNull(tempVast.getVast().getAds().get(0).getWrapper().getCreatives().get(1).getLinear().getAdParameters());
    }

    @Test
    public void testWrapperWithNoAds() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(VAST_WRAPPER_LINEAR_NONLINEAR);
        AdResponseParserVastHelper mainVast = new AdResponseParserVastHelper(vastXML);

        AdResponseParserVastHelper tempVast = new AdResponseParserVastHelper(vastXML);

        assertNotNull(mainVast.getVastUrl());
        assertEquals("http://SecondaryAdServer.vast.tag", mainVast.getVastUrl());
        assertNotNull(tempVast.getImpressionEvents(mainVast.getVast(), 0));
        assertEquals("Acudeo Compatible", tempVast.getVast().getAds().get(0).getWrapper().getAdSystem().getValue());
        assertEquals("http://SecondaryAdServer.vast.tag", tempVast.getVast().getAds().get(0).getWrapper().getVastUrl().getValue());
        assertEquals("http://myErrorURL/wrapper/error", tempVast.getVast().getAds().get(0).getWrapper().getError()
                .getValue());
        assertEquals("http://myTrackingURL/wrapper/impression", tempVast.getVast().getAds().get(0).getWrapper()
                .getImpressions().get(0).getValue());

        assertEquals("602833", tempVast.getVast().getAds().get(0).getWrapper()
                .getCreatives().get(0).getAdID());

        assertEquals(1, tempVast.getVast().getAds().get(0).getWrapper()
                .getCreatives().get(0).getLinear().getTrackingEvents().size());
        assertEquals("creativeView", tempVast.getVast().getAds().get(0).getWrapper()
                .getCreatives().get(0).getLinear().getTrackingEvents().get(0).getEvent());
        assertEquals("http://myTrackingURL/wrapper/creativeView", tempVast.getVast().getAds().get(0).getWrapper()
                .getCreatives().get(0).getLinear().getTrackingEvents().get(0).getValue());

        assertEquals("http://myTrackingURL/wrapper/click", tempVast.getVast().getAds().get(0).getWrapper()
                .getCreatives().get(1).getLinear().getVideoClicks().getClickTrackings().get(0).getValue());

        assertEquals(0, tempVast.getVast().getAds().get(0).getWrapper()
                .getCreatives().get(2).getNonLinearAds().getTrackingEvents().size());
        assertEquals("samplexmlEncoded", tempVast.getVast().getAds().get(0).getWrapper().getCreatives().get(0).getLinear().getAdParameters().getXmlEncoded());
        assertNull(tempVast.getVast().getAds().get(0).getWrapper().getCreatives().get(1).getLinear().getAdParameters());
    }

    @Test
    public void testVASTParserAllLinearElements() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(SAMPLE_GOOD_VAST);
        AdResponseParserVastHelper vast = new AdResponseParserVastHelper(vastXML);
        assertTrue("Vast should have been ready by now.", vast.isReady());
        assertEquals("2.0", vast.getVast().getVersion());
        assertNull(vast.getVast().getError());
        assertNotNull(vast.getVast().getAds());
        assertEquals(1, vast.getVast().getAds().size());
        assertEquals("537074373", vast.getVast().getAds().get(0).getId());

        assertNull(vast.getVast().getAds().get(0).getSequence());
        assertNotNull(vast.getVast().getAds().get(0).getInline());

        //inline ->adSystem
        assertNotNull(vast.getVast().getAds().get(0).getInline().getAdSystem());
        assertEquals("1.0", vast.getVast().getAds().get(0).getInline().getAdSystem().getVersion());
        assertEquals("Prebid Enterprise", vast.getVast().getAds().get(0).getInline().getAdSystem().getValue());

        //inline ->adTitle
        assertNotNull(vast.getVast().getAds().get(0).getInline().getAdTitle());
        assertEquals("VAST 2.0 Instream Test 1", vast.getVast().getAds().get(0).getInline().getAdTitle().getValue());

        //inline ->Description
        assertEquals("VAST 2.0 Instream Test 1", vast.getVast().getAds().get(0).getInline().getDescription().getValue());

        //inline ->advertiser
        assertEquals("Advertiser Text", vast.getVast().getAds().get(0).getInline().getAdvertiser().getValue());

        //inline ->pricing
        assertEquals("blah", vast.getVast().getAds().get(0).getInline().getPricing().getModel());
        assertEquals("USD", vast.getVast().getAds().get(0).getInline().getPricing().getCurrency());

        //inline -> survey
        assertEquals("http://mySurveyURL/survey", vast.getVast().getAds().get(0).getInline().getSurvey().getValue());

        //inline ->Error
        assertEquals("http://myErrorURL/error", vast.getVast().getAds().get(0).getInline().getError().getValue());

        //inline ->impressions
        assertNotNull(vast.getVast().getAds().get(0).getInline().getImpressions());
        assertEquals(2, vast.getVast().getAds().get(0).getInline().getImpressions().size());
        assertEquals("first", vast.getVast().getAds().get(0).getInline().getImpressions().get(0).getId());
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/ri?did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51", vast.getVast().getAds().get(0).getInline().getImpressions().get(0).getValue());

        assertEquals("second", vast.getVast().getAds().get(0).getInline().getImpressions().get(1).getId());
        assertEquals("got:" + vast.getVast().getAds().get(0).getInline().getImpressions().get(1).getValue(), "http://myTrackingURL/anotherImpression", vast.getVast().getAds().get(0).getInline().getImpressions().get(1).getValue());

        //inline ->creatives->creative(0)
        assertNotNull(vast.getVast().getAds().get(0).getInline().getCreatives());
        assertEquals(3, vast.getVast().getAds().get(0).getInline().getCreatives().size());
        assertEquals("601364", vast.getVast().getAds().get(0).getInline().getCreatives().get(0).getAdID());
        assertEquals("537074373", vast.getVast().getAds().get(0).getInline().getCreatives().get(0).getId());
        assertEquals("1", vast.getVast().getAds().get(0).getInline().getCreatives().get(0).getSequence());

        assertEquals(1, vast.getVast().getAds().get(0).getInline().getCreatives().get(0).getCreativeExtensions().size());
        assertNotNull(vast.getVast().getAds().get(0).getInline().getCreatives().get(0).getLinear());
        assertEquals("00:01:36", vast.getVast().getAds().get(0).getInline().getCreatives().get(0).getLinear().getDuration().getValue());

        //inline ->creatives -> trackingEvents
        assertEquals(16, vast.getVast().getAds().get(0).getInline().getCreatives().get(0).getLinear().getTrackingEvents().size());

        String expected_track_event_name_0 = "creativeView";
        String expected_track_event_value_0 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                              ".0/rv?t=creativeView&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_0, vast.getVast().getAds().get(0).getInline().getCreatives().get(0).getLinear()
                                                      .getTrackingEvents().get(0)
                                                      .getEvent());
        assertEquals(expected_track_event_value_0, vast.getVast().getAds().get(0).getInline().getCreatives().get(0).getLinear()
                                                       .getTrackingEvents().get(0)
                                                       .getValue());

        String expected_track_event_name_1 = "start";
        String expected_track_event_value_1 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                              ".0/rv?t=start&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_1, vast.getVast().getAds().get(0).getInline().getCreatives().get(0).getLinear()
                                                      .getTrackingEvents().get(1)
                                                      .getEvent());
        assertEquals(expected_track_event_value_1, vast.getVast().getAds().get(0).getInline().getCreatives().get(0).getLinear()
                                                       .getTrackingEvents().get(1)
                                                       .getValue());

        String expected_track_event_name_2 = "midpoint";
        String expected_track_event_value_2 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                              ".0/rv?t=midpoint&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_2, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                      .getLinear()
                                                      .getTrackingEvents().get(2)
                                                      .getEvent());
        assertEquals(expected_track_event_value_2, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(2)
                                                       .getValue());

        String expected_track_event_name_3 = "firstQuartile";
        String expected_track_event_value_3 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                              ".0/rv?t=firstQuartile&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_3, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                      .getLinear()
                                                      .getTrackingEvents().get(3)
                                                      .getEvent());
        assertEquals(expected_track_event_value_3, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(3)
                                                       .getValue());

        String expected_track_event_name_4 = "thirdQuartile";
        String expected_track_event_value_4 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                              ".0/rv?t=thirdQuartile&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_4, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                      .getLinear()
                                                      .getTrackingEvents().get(4)
                                                      .getEvent());
        assertEquals(expected_track_event_value_4, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(4)
                                                       .getValue());

        String expected_track_event_name_5 = "complete";
        String expected_track_event_value_5 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                              ".0/rv?t=complete&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_5, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                      .getLinear()
                                                      .getTrackingEvents().get(5)
                                                      .getEvent());
        assertEquals(expected_track_event_value_5, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(5)
                                                       .getValue());

        String expected_track_event_name_6 = "mute";
        String expected_track_event_value_6 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                              ".0/rv?t=mute&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_6, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                      .getLinear()
                                                      .getTrackingEvents().get(6)
                                                      .getEvent());
        assertEquals(expected_track_event_value_6, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(6)
                                                       .getValue());

        String expected_track_event_name_7 = "unmute";
        String expected_track_event_value_7 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                              ".0/rv?t=unmute&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_7, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                      .getLinear()
                                                      .getTrackingEvents().get(7)
                                                      .getEvent());
        assertEquals(expected_track_event_value_7, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(7)
                                                       .getValue());

        String expected_track_event_name_8 = "pause";
        String expected_track_event_value_8 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                              ".0/rv?t=pause&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_8, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                      .getLinear()
                                                      .getTrackingEvents().get(8)
                                                      .getEvent());
        assertEquals(expected_track_event_value_8, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(8)
                                                       .getValue());

        String expected_track_event_name_9 = "rewind";
        String expected_track_event_value_9 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                              ".0/rv?t=rewind&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_9, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                      .getLinear()
                                                      .getTrackingEvents().get(9)
                                                      .getEvent());
        assertEquals(expected_track_event_value_9, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(9)
                                                       .getValue());

        String expected_track_event_name_10 = "resume";
        String expected_track_event_value_10 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                               ".0/rv?t=resume&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_10, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(10)
                                                       .getEvent());
        assertEquals(expected_track_event_value_10, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                        .getLinear()
                                                        .getTrackingEvents().get(10)
                                                        .getValue());

        String expected_track_event_name_11 = "fullscreen";
        String expected_track_event_value_11 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                               ".0/rv?t=fullscreen&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_11, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(11)
                                                       .getEvent());
        assertEquals(expected_track_event_value_11, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                        .getLinear()
                                                        .getTrackingEvents().get(11)
                                                        .getValue());

        String expected_track_event_name_12 = "expand";
        String expected_track_event_value_12 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                               ".0/rv?t=expand&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_12, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(12)
                                                       .getEvent());
        assertEquals(expected_track_event_value_12, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                        .getLinear()
                                                        .getTrackingEvents().get(12)
                                                        .getValue());

        String expected_track_event_name_13 = "collapse";
        String expected_track_event_value_13 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                               ".0/rv?t=collapse&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_13, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(13)
                                                       .getEvent());
        assertEquals(expected_track_event_value_13, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                        .getLinear()
                                                        .getTrackingEvents().get(13)
                                                        .getValue());

        String expected_track_event_name_14 = "acceptInvitation";
        String expected_track_event_value_14 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                               ".0/rv?t=acceptInvitation&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_14, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(14)
                                                       .getEvent());
        assertEquals(expected_track_event_value_14, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                        .getLinear()
                                                        .getTrackingEvents().get(14)
                                                        .getValue());

        String expected_track_event_name_15 = "close";
        String expected_track_event_value_15 = "http://oxv4support-d3.prebidenterprise.com/v/1" +
                                               ".0/rv?t=close&did.adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51";
        assertEquals(expected_track_event_name_15, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                       .getLinear()
                                                       .getTrackingEvents().get(15)
                                                       .getEvent());
        assertEquals(expected_track_event_value_15, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                        .getLinear()
                                                        .getTrackingEvents().get(15)
                                                        .getValue());

        assertEquals("params=for&request=gohere", vast.getVast().getAds().get(0).getInline().getCreatives().get(0).getLinear().getAdParameters().getValue());

        ////inline ->creatives->creative(0) -> videoClicks
        assertNotNull(vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                          .getLinear().getVideoClicks());
        assertNotNull(vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                          .getLinear().getVideoClicks().getClickThrough());
        assertEquals("http://oxv4support-d3.prebidenterprise.com/v/1.0/rc?did" +
                     ".adid=2c544905-f613-46ac-95f4-7d81e8fc3505&ts" +
                     "=1fHU9MXxyaWQ9MmVkNDBjOGYtNjA4YS00ZDY5LWIyNzMtMDBjYWZiNjEyMWQ0fHJ0PTE0MzM4MDQ5Mjd8YXVpZD01MzcwNzQzNzN8YXVtPURNSUQuTElORUFSVklERU98c2lkPTUzNzA2NDIxMXxwdWI9NTM3MDcxNzg3fHBjPVVTRHxyYWlkPTVlOTk0N2E1LWM5YzItNDNjZi1hZTY3LTMzMjZjNWU2N2IwYnxhaWQ9NTM3MTI5MDI1fHQ9M3xhcz02NDB4MzYwfGxpZD01MzcxMDYzNzR8b2lkPTUzNzA4ODg4NnxwPTEwMDB8cHI9MTAwMHxhZHY9NTM3MDcxNzgyfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxibT1CVVlJTkcuTk9OR1VBUkFOVEVFRHx1cj1XUTVDVHpydG51", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                                                                                                                                                                                                                                                                                                                                                                                                                                       .getLinear().getVideoClicks().getClickThrough().getValue());

        assertEquals("first", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                  .getLinear().getVideoClicks().getClickTrackings().get(0).getId());
        assertEquals("http://myTrackingURL/click1", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                        .getLinear().getVideoClicks().getClickTrackings().get(0).getValue());

        assertEquals("second", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                   .getLinear().getVideoClicks().getClickTrackings().get(1).getId());
        assertEquals("http://myTrackingURL/click2", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                        .getLinear().getVideoClicks().getClickTrackings().get(1).getValue());

        assertFalse(vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                        .getLinear().getVideoClicks().getCustomClicks().isEmpty());

        assertEquals("http://myTrackingURL/CustomClick", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                             .getLinear().getVideoClicks().getCustomClicks().get(0).getValue());

        //////inline ->creatives->creative(0)->MediaFiles(0)
        assertNotNull(vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                          .getLinear().getMediaFiles());
        assertEquals(2, vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                            .getLinear().getMediaFiles().size());
        assertEquals("537129025", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                      .getLinear().getMediaFiles().get(0).getId());

        assertEquals("streaming", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                      .getLinear().getMediaFiles().get(0).getDelivery());
        assertEquals("video/mp4", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                      .getLinear().getMediaFiles().get(0).getType());

        assertEquals("640", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                .getLinear().getMediaFiles().get(0).getWidth());

        assertEquals("360", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                .getLinear().getMediaFiles().get(0).getHeight());

        assertNull(vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                       .getLinear().getMediaFiles().get(0).getBitrate());

        assertNull(vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                       .getLinear().getMediaFiles().get(0).getMinBitrate());

        assertNull(vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                       .getLinear().getMediaFiles().get(0).getMaxBitrate());

        assertNull(vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                       .getLinear().getMediaFiles().get(0).getOffset());
        assertNull(vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                       .getLinear().getMediaFiles().get(0).getXPosition());
        assertNull(vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                       .getLinear().getMediaFiles().get(0).getYPosition());
        assertNull(vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                       .getLinear().getMediaFiles().get(0).getApiFramework());
        assertNull(vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                       .getLinear().getMediaFiles().get(0).getDuration());

        assertEquals("http://i-cdn.prebid" +
                     ".com/5a7/5a731840-5ae7-4dca-ba66-6e959bb763e2/be2" +
                     "/be2cf3b2cf0648e0aa46c7c09afaf3f4.mp4", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                                  .getLinear().getMediaFiles().get(0).getValue());

        assertEquals("http://cdnp.tremormedia.com/video/acudeo/Carrot_400x300_500kb.mp4", vast.getVast().getAds().get(0).getInline().getCreatives().get(0)
                                                                                              .getLinear().getMediaFiles().get(1).getValue());

        //inline ->creatives -> creative(1)
        assertEquals("601364-Companion", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getAdID());
        assertEquals(3, vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().size());
        //inline ->creatives -> creative(1) ->companionads -> companionad(0)
        assertEquals("VPAID", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getApiFramework());
        assertEquals("500", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getExpandedHeight());
        assertEquals("600", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getExpandedWidth());
        assertEquals("250", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getHeight());
        assertEquals("big_box", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getId());
        assertEquals("300", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getWidth());
        assertEquals("image/jpeg", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getStaticResource().getCreativeType());
        assertEquals("http://demo.tremormedia.com/proddev/vast/Blistex1.jpg", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getStaticResource().getValue());
        assertEquals(1, vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getTrackingEvents().size());
        assertEquals("creativeView", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getTrackingEvents().get(0).getEvent());
        assertEquals("http://myTrackingURL/firstCompanionCreativeView", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getTrackingEvents().get(0).getValue());
        assertEquals("http://www.tremormedia.com", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getCompanionClickThrough().getValue());

        assertEquals("http://www.CompanionClickTracking.com", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getCompanionClickTracking().getValue());
        assertEquals("Display this instead of the ad", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getAltText().getValue());
        assertEquals("params=companionParams&request=gohere", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(0).getAdParameters().getValue());
        //inline ->creatives -> creative(1) ->companionads -> companionad(1)
        assertEquals("90", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(1).getHeight());
        assertEquals("http://ad3.liverail.com/util/companions.php", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(1).getIFrameResource().getValue());
        //inline ->creatives -> creative(1) ->companionads -> companionad(2)
        assertEquals("http://ad3.liverail.com/util/HTMLResource.php", vast.getVast().getAds().get(0).getInline().getCreatives().get(1).getCompanionAds().get(2).getHtmlResource().getValue());

        //inline ->creatives -> creative(2) ->Nonlinearads
        assertEquals("601365", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getAdID());
        //inline ->creatives -> creative(2) ->Nonlinearads(0)
        assertEquals("VPAID", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(0).getApiFramework());
        assertEquals("500", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(0).getExpandedHeight());
        assertEquals("600", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(0).getExpandedWidth());
        assertEquals("50", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(0).getHeight());
        assertEquals("special_overlay", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(0).getId());
        assertEquals("true", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(0).getMaintainAspectRatio());
        assertEquals("true", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(0).getScalable());
        assertEquals("300", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(0).getWidth());
        assertEquals("image/jpeg", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(0).getStaticResource().getCreativeType());
        assertEquals("http://cdn.liverail.com/adasset/228/330/overlay.jpg", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(0).getStaticResource().getValue());
        assertEquals("http://t3.liverail.com", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(0).getNonLinearClickThrough().getValue());

        //inline -> creatives -> creative(2) -> Nonlinearads(1)
        assertEquals("90", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(1).getHeight());
        assertEquals("http://ad3.liverail.com/util/non_linear.php", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(1).getIFrameResource().getValue());

        //inline -> creatives -> creative(2) -> Nonlinearads(2)
        assertEquals("http://ad3.liverail.com/util/non_linear_HTMLResource.php", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(2).getHTMLResource().getValue());
        assertEquals("params=companionParams&request=gohere", vast.getVast().getAds().get(0).getInline().getCreatives().get(2).getNonLinearAds().getNonLinearAds().get(2).getAdParameters().getValue());

        //inline -> extensions(0)
        assertEquals("DART", vast.getVast().getAds().get(0).getInline().getExtensions().getExtensions().get(0).getType());

        //inline -> adVerifications -> Verification
        ArrayList<Verification> verifications = vast.getVast().getAds().get(0).getInline().getAdVerifications().getVerifications();
        assertEquals(2, verifications.size());

        Verification verification1 = verifications.get(0);
        assertEquals("Prebid1", verification1.getVendor());
        assertEquals("omidPrebid1", verification1.getApiFramework());
        assertEquals("https://measurement.domain.com/tag.js", verification1.getJsResource());
        assertEquals("{1}", verification1.getVerificationParameters());

        Verification verification2 = verifications.get(1);
        assertEquals("Prebid2", verification2.getVendor());
        assertEquals("https://measurement.domain.com/tag2.js", verification2.getJsResource());
        assertEquals("omidPrebid2", verification2.getApiFramework());
        assertEquals("{2}", verification2.getVerificationParameters());
    }

    @Test
    // This test ensures that the adVerifications Node is parsed correctly when it appears in the Extensions Node.
    public void testVASTParserAdVerificationsInExtensions() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(ADVERIFICATIONS_IN_EXTENSIONS);
        AdResponseParserVastHelper vast = new AdResponseParserVastHelper(vastXML);

        //inline -> extensions -> extension -> adVerifications -> Verification
        ArrayList<Verification> verifications = vast.getVast().getAds().get(0).getInline().getExtensions().getExtensions().get(0).getAdVerifications().getVerifications();
        assertEquals(1, verifications.size());
        Verification verification = verifications.get(0);

        assertEquals("https://s3-us-west-2.amazonaws.com/omsdk-files/compliance-js/omid-validation-verification-script-v1.js", verification.getJsResource());
        assertEquals("omid", verification.getApiFramework());
        assertEquals("prebid", verification.getVendor());
        assertEquals("parameter1=value1&parameter2=value2&parameter3=value3", verification.getVerificationParameters());
    }

    @Test
    public void testVASTParserInlineError() throws Exception {
        String inline_error_vastXML = ResourceUtils.convertResourceToString(SAMPLE_INLINE_ERROR_VAST);
        AdResponseParserVastHelper vast = new AdResponseParserVastHelper(inline_error_vastXML);

        assertNotNull(vast.getVast().getAds().get(0).getInline());
        assertNotNull(vast.getVast().getAds().get(0).getInline().getError());
        assertEquals("http://adserver.com/error.gif", vast.getVast().getAds().get(0).getInline().getError().getValue());

        assertEquals("http://adserver.com/error.gif", vast.getError(vast, 0));
        assertFalse("http://adserver.com/error.png".equals(vast.getError(vast, 0)));
    }

    @Test
    public void testTrackingIndex() {

        AdResponseParserVast.Tracking track = new AdResponseParserVast.Tracking("impression", "www.impression.url");

        assertEquals(21, track.getEvent());
        assertEquals("www.impression.url", track.getUrl());
    }

    @Test
    public void testWrongVastThrowError() throws IOException {

        String wrongvast = ResourceUtils.convertResourceToString(WRONG_VAST);
        AdResponseParserVastHelper vast = null;
        VastParseError error = null;
        try {
            vast = new AdResponseParserVastHelper(wrongvast);
        }
        catch (VastParseError vastParseError) {
            vastParseError.printStackTrace();
            error = vastParseError;
        }

        assertNotNull("Syntax error for inline->impressoin", error);
        assertTrue(error instanceof VastParseError);
        assertNull(vast);
    }



    @Test
    public void testVastWithNoAd() throws IOException {
        String vast_noad = ResourceUtils.convertResourceToString(WRONG_VAST_WITH_NO_AD);

        AdResponseParserVastHelper vastParserHelper = null;
        VastParseError error = null;
        try {
            vastParserHelper = new AdResponseParserVastHelper(vast_noad);
        }
        catch (VastParseError vastParseError) {
            vastParseError.printStackTrace();
            error = vastParseError;
        }
        assertNull(vastParserHelper.getVast().getAds());
        assertNull(error);
        assertNull(vastParserHelper.getVastUrl());
    }

    @Test
    public void testVastWithNoVersion() {
        String vast = "<VAST > </VAST>";

        AdResponseParserVastHelper vastParserHelper = null;
        VastParseError error = null;
        try {
            vastParserHelper = new AdResponseParserVastHelper(vast);
        }
        catch (VastParseError vastParseError) {
            vastParseError.printStackTrace();
            error = vastParseError;
        }
        assertNull(vastParserHelper.getVast().getVersion());
        assertNull(error);
    }

    @Test
    public void testVastBom() throws Exception {
        String vastBomString = ResourceUtils.convertResourceToString(VAST_BOM);
        AdResponseParserVastHelper helper = new AdResponseParserVastHelper(vastBomString);
        Method checkForBom = helper.getClass().getSuperclass().getDeclaredMethod("checkForBOM", String.class);
        checkForBom.setAccessible(true);
        String invoke = (String) checkForBom.invoke(helper, vastBomString);
        assertNotNull(invoke);
    }

    @Test
    public void testVastNoBom() throws Exception {
        String vastCorrect = ResourceUtils.convertResourceToString(VAST_CORRECT);
        AdResponseParserVastHelper helper = new AdResponseParserVastHelper(vastCorrect);
        Method checkForBom = helper.getClass().getSuperclass().getDeclaredMethod("checkForBOM", String.class);
        checkForBom.setAccessible(true);
        String invoke = (String) checkForBom.invoke(helper, vastCorrect);
        assertNull(invoke);
    }

    @Test
    public void testGetCompanionAd() {
        InLine mockInLine;
        ArrayList<Companion> companions;
        Companion mockCompanionA;
        Companion mockCompanionB;
        Companion result;

        // Null creatives
        mockInLine = setupInLine();
        mockInLine.setCreatives(null);
        result = AdResponseParserVast.getCompanionAd(mockInLine);
        assertNull(result);

        // Empty creatives list
        mockInLine = setupInLine();
        mockInLine.setCreatives(new ArrayList<>());
        result = AdResponseParserVast.getCompanionAd(mockInLine);
        assertNull(result);

        // Empty companion ad list
        mockInLine = setupInLine();
        mockInLine.getCreatives().get(0).setCompanionAds(new ArrayList<>());
        result = AdResponseParserVast.getCompanionAd(mockInLine);
        assertNull(result);

        // One companion
        mockInLine = setupInLine();
        companions = mockInLine.getCreatives().get(0).getCompanionAds();
        mockCompanionA = mock(Companion.class);
        companions.add(mockCompanionA);
        result = AdResponseParserVast.getCompanionAd(mockInLine);
        assertEquals(mockCompanionA, result);

        // Companion B format better than Companion A format
        mockInLine = setupInLine();
        companions = mockInLine.getCreatives().get(0).getCompanionAds();
        mockCompanionA = mock(Companion.class);
        when(mockCompanionA.getWidth()).thenReturn("2");
        when(mockCompanionA.getHeight()).thenReturn("2");
        when(mockCompanionA.getStaticResource()).thenReturn(mock(StaticResource.class));
        companions.add(mockCompanionA);
        mockCompanionB = mock(Companion.class);
        when(mockCompanionB.getWidth()).thenReturn("1");
        when(mockCompanionB.getHeight()).thenReturn("1");
        when(mockCompanionB.getHtmlResource()).thenReturn(mock(HTMLResource.class));
        companions.add(mockCompanionB);
        result = AdResponseParserVast.getCompanionAd(mockInLine);
        assertEquals(mockCompanionB, result);

        // Same format for Companion A & B, but B has better resolution
        mockInLine = setupInLine();
        companions = mockInLine.getCreatives().get(0).getCompanionAds();
        mockCompanionA = mock(Companion.class);
        when(mockCompanionA.getWidth()).thenReturn("1");
        when(mockCompanionA.getHeight()).thenReturn("1");
        when(mockCompanionA.getStaticResource()).thenReturn(mock(StaticResource.class));
        companions.add(mockCompanionA);
        mockCompanionB = mock(Companion.class);
        when(mockCompanionB.getWidth()).thenReturn("2");
        when(mockCompanionB.getHeight()).thenReturn("2");
        when(mockCompanionB.getStaticResource()).thenReturn(mock(StaticResource.class));
        companions.add(mockCompanionB);
        result = AdResponseParserVast.getCompanionAd(mockInLine);
        assertEquals(mockCompanionB, result);
    }

    private InLine setupInLine() {
        InLine mockInLine = mock(InLine.class);

        mockInLine.setCreatives(new ArrayList<>());
        Creative mockCreative = mock(Creative.class);
        when(mockCreative.getCompanionAds()).thenReturn(new ArrayList<>());

        ArrayList<Creative> creatives = new ArrayList<>();
        creatives.add(mockCreative);
        when(mockInLine.getCreatives()).thenReturn(creatives);
        return mockInLine;
    }
}
