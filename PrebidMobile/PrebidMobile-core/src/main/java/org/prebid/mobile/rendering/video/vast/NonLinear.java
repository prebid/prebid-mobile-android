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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class NonLinear extends VASTParserBase {

    private final static String VAST_NONLINEAR = "NonLinear";
    private final static String VAST_STATICRESOURCE = "StaticResource";
    private final static String VAST_IFRAMERESOUCE = "IFrameResource";
    private final static String VAST_HTMLRESOURCE = "HTMLResource";
    private final static String VAST_ADPARAMETERS = "AdParameters";
    private final static String VAST_NONLINEARCLICKTHROUGH = "NonLinearClickThrough";
    private final static String VAST_NONLINEARCLICKTRACKING = "NonLinearClickTracking";

    private String id;
    private String width;
    private String height;
    private String expandedWidth;
    private String expandedHeight;
    private String scalable;
    private String maintainAspectRatio;
    private String minSuggestedDuration;
    private String apiFramework;

    private StaticResource staticResource;
    private IFrameResource iFrameResource;
    private HTMLResource HTMLResource;
    private AdParameters adParameters;
    private NonLinearClickThrough nonLinearClickThrough;
    private NonLinearClickTracking nonLinearClickTracking;

    public NonLinear(XmlPullParser p) throws XmlPullParserException, IOException {

        p.require(XmlPullParser.START_TAG, null, VAST_NONLINEAR);

        id = p.getAttributeValue(null, "id");
        width = p.getAttributeValue(null, "width");
        height = p.getAttributeValue(null, "height");
        expandedWidth = p.getAttributeValue(null, "expandedWidth");
        expandedHeight = p.getAttributeValue(null, "expandedHeight");
        scalable = p.getAttributeValue(null, "scalable");
        maintainAspectRatio = p.getAttributeValue(null, "maintainAspectRatio");
        minSuggestedDuration = p.getAttributeValue(null, "minSuggestedDuration");
        apiFramework = p.getAttributeValue(null, "apiFramework");

        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_STATICRESOURCE)) {
                p.require(XmlPullParser.START_TAG, null, VAST_STATICRESOURCE);
                staticResource = new StaticResource(p);
                p.require(XmlPullParser.END_TAG, null, VAST_STATICRESOURCE);
			}
			else if (name != null && name.equals(VAST_IFRAMERESOUCE))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_IFRAMERESOUCE);
                iFrameResource = new IFrameResource(p);
                p.require(XmlPullParser.END_TAG, null, VAST_IFRAMERESOUCE);
			}
			else if (name != null && name.equals(VAST_HTMLRESOURCE))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_HTMLRESOURCE);
                HTMLResource = new HTMLResource(p);
                p.require(XmlPullParser.END_TAG, null, VAST_HTMLRESOURCE);
			}
			else if (name != null && name.equals(VAST_ADPARAMETERS))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_ADPARAMETERS);
                adParameters = new AdParameters(p);
                p.require(XmlPullParser.END_TAG, null, VAST_ADPARAMETERS);
			}
			else if (name != null && name.equals(VAST_NONLINEARCLICKTHROUGH))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_NONLINEARCLICKTHROUGH);
                nonLinearClickThrough = new NonLinearClickThrough(p);
                p.require(XmlPullParser.END_TAG, null, VAST_NONLINEARCLICKTHROUGH);
			}
			else if (name != null && name.equals(VAST_NONLINEARCLICKTRACKING))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_NONLINEARCLICKTRACKING);
                nonLinearClickTracking = new NonLinearClickTracking(p);
                p.require(XmlPullParser.END_TAG, null, VAST_NONLINEARCLICKTRACKING);
			}
			else
			{
				skip(p);
			}
		}

	}

    public String getId() {
        return id;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    public String getExpandedWidth() {
        return expandedWidth;
    }

    public String getExpandedHeight() {
        return expandedHeight;
    }

    public String getScalable() {
        return scalable;
    }

    public String getMaintainAspectRatio() {
        return maintainAspectRatio;
    }

    public String getMinSuggestedDuration() {
        return minSuggestedDuration;
    }

    public String getApiFramework() {
        return apiFramework;
    }

    public StaticResource getStaticResource() {
        return staticResource;
    }

    public IFrameResource getIFrameResource() {
        return iFrameResource;
    }

    public HTMLResource getHTMLResource() {
        return HTMLResource;
    }

    public AdParameters getAdParameters() {
        return adParameters;
    }

    public NonLinearClickThrough getNonLinearClickThrough() {
        return nonLinearClickThrough;
    }

    public NonLinearClickTracking getNonLinearClickTracking() {
        return nonLinearClickTracking;
    }
}
