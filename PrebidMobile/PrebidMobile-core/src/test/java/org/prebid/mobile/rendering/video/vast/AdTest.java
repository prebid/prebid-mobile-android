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

package org.prebid.mobile.rendering.video.vast;

import android.util.Xml;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.RobolectricTestRunner;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class AdTest {

    private final static String SAMPLE_GOOD_VAST = "vast_inline_nonlinear.xml";

    private final static String VAST_WRAPPER_LINEAR_NONLINEAR = "vast_wrapper_linear_nonlinear.xml";

    @Test
    public void testAdInLine() throws Exception {

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        String vastXML = ResourceUtils.convertResourceToString(SAMPLE_GOOD_VAST);

        parser.setInput(new StringReader(vastXML));
        parser.nextTag();
        parser.nextTag();

        Ad ad = new Ad(parser);

        assertEquals("NonLinear Test Campaign 1", ad.getInline().getAdTitle().getValue());
        assertEquals("NonLinear Test Campaign 1", ad.getInline().getDescription()
                                                    .getValue());
        assertEquals("http://mySurveyURL/survey", ad.getInline().getSurvey().getValue());
        assertEquals("602678-NonLinear", ad.getInline().getCreatives().get
            (0).getAdID());
        assertEquals("creativeView", ad.getInline().getCreatives().get
            (0).getNonLinearAds().getTrackingEvents().get(0).getEvent());
        assertEquals("http://myTrackingURL/nonlinear/creativeView", ad.getInline().getCreatives().get
            (0).getNonLinearAds().getTrackingEvents().get(0).getValue());

        assertEquals("300", ad.getInline().getCreatives().get
            (0).getNonLinearAds().getNonLinearAds().get(0).getWidth());

        assertEquals("50", ad.getInline().getCreatives().get
            (0).getNonLinearAds().getNonLinearAds().get(0).getHeight());

        assertEquals("00:00:15", ad.getInline().getCreatives().get
            (0).getNonLinearAds().getNonLinearAds().get(0).getMinSuggestedDuration());

        assertEquals("image/jpeg", ad.getInline().getCreatives().get
            (0).getNonLinearAds().getNonLinearAds().get(0).getStaticResource().getCreativeType());

        assertEquals("http://demo.tremormedia.com/proddev/vast/50x300_static.jpg", ad.getInline().getCreatives().get
            (0).getNonLinearAds().getNonLinearAds().get(0).getStaticResource().getValue());

        assertEquals("http://www.tremormedia.com", ad.getInline().getCreatives().get
            (0).getNonLinearAds().getNonLinearAds().get(0).getNonLinearClickThrough().getValue());

        assertEquals("602678-Companion", ad.getInline().getCreatives().get
            (1).getAdID());

        assertEquals("728", ad.getInline().getCreatives().get
            (1).getCompanionAds().get(0).getWidth());

        assertEquals("90", ad.getInline().getCreatives().get
            (1).getCompanionAds().get(0).getHeight());

        assertEquals("image/jpeg", ad.getInline().getCreatives().get
            (1).getCompanionAds().get(0).getStaticResource().getCreativeType());

        assertEquals("http://demo.tremormedia.com/proddev/vast/728x90_banner1.jpg", ad.getInline().getCreatives().get
            (1).getCompanionAds().get(0).getStaticResource().getValue());

        assertEquals("creativeView", ad.getInline().getCreatives().get
            (1).getCompanionAds().get(0).getTrackingEvents().get(0).getEvent());

        assertEquals("http://myTrackingURL/secondCompanion", ad.getInline()
                                                               .getCreatives().get
                (1).getCompanionAds().get(0).getTrackingEvents().get(0).getValue());
        assertEquals("http://www.tremormedia.com", ad.getInline().getCreatives().get
            (1).getCompanionAds().get(0).getCompanionClickThrough().getValue());
    }

    @Test
    public void testAdWrapper() throws Exception {
        String vastXML = ResourceUtils.convertResourceToString(VAST_WRAPPER_LINEAR_NONLINEAR);
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

        parser.setInput(new StringReader(vastXML));
        parser.nextTag();
        parser.nextTag();

        Ad ad = new Ad(parser);

        assertEquals("Acudeo Compatible", ad.getWrapper().getAdSystem().getValue());
        assertEquals("http://SecondaryAdServer.vast.tag", ad.getWrapper().getVastUrl().getValue());
        assertEquals("http://myErrorURL/wrapper/error", ad.getWrapper().getError()
                                                          .getValue());
        assertEquals("http://myTrackingURL/wrapper/impression", ad.getWrapper()
                                                                  .getImpressions().get(0).getValue());

        assertTrue("602833".equals(ad.getWrapper()
                                     .getCreatives().get(0).getAdID()));

        assertEquals(1, ad.getWrapper()
                          .getCreatives().get(0).getLinear().getTrackingEvents().size());
        assertEquals("creativeView", ad.getWrapper()
                                       .getCreatives().get(0).getLinear().getTrackingEvents().get(0).getEvent());
        assertEquals("http://myTrackingURL/wrapper/creativeView", ad.getWrapper()
                                                                    .getCreatives().get(0).getLinear().getTrackingEvents().get(0).getValue());

        assertEquals("http://myTrackingURL/wrapper/click", ad.getWrapper()
                                                             .getCreatives().get(1).getLinear().getVideoClicks().getClickTrackings().get(0).getValue());

        assertEquals(0, ad.getWrapper()
                          .getCreatives().get(2).getNonLinearAds().getTrackingEvents().size());
    }
}