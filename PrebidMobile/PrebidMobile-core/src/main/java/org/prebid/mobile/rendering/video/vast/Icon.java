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

public class Icon extends VASTParserBase {

    private final static String VAST_ICON = "Icon";
    private final static String VAST_STATICRESOURCE = "StaticResource";
    private final static String VAST_IFRAMERESOURCE = "IFrameResource";
    private final static String VAST_HTMLRESOURCE = "HTMLResource";
    private final static String VAST_ICONCLICKS = "IconClicks";
    private final static String VAST_ICONVIEWTRACKING = "IconViewTracking";

    private String program;
    private String width;
    private String height;
    private String xPosition;
    private String yPosition;
    private String duration;
    private String offset;
    private String apiFramework;

    private StaticResource staticResource;
    private IFrameResource iFrameResource;
    private HTMLResource htmlResource;
    private IconClicks iconClicks;
    private IconViewTracking iconViewTracking;

    public Icon(XmlPullParser p) throws XmlPullParserException, IOException {

        p.require(XmlPullParser.START_TAG, null, VAST_ICON);

        program = p.getAttributeValue(null, "program");
        width = p.getAttributeValue(null, "width");
        height = p.getAttributeValue(null, "height");
        xPosition = p.getAttributeValue(null, "xPosition");
        yPosition = p.getAttributeValue(null, "yPosition");
        duration = p.getAttributeValue(null, "duration");
        offset = p.getAttributeValue(null, "offset");
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
			else if (name != null && name.equals(VAST_IFRAMERESOURCE))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_IFRAMERESOURCE);
                iFrameResource = new IFrameResource(p);
                p.require(XmlPullParser.END_TAG, null, VAST_IFRAMERESOURCE);
			}
			else if (name != null && name.equals(VAST_HTMLRESOURCE))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_HTMLRESOURCE);
                htmlResource = new HTMLResource(p);
                p.require(XmlPullParser.END_TAG, null, VAST_HTMLRESOURCE);
			}
			else if (name != null && name.equals(VAST_ICONCLICKS))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_ICONCLICKS);
                iconClicks = new IconClicks(p);
                p.require(XmlPullParser.END_TAG, null, VAST_ICONCLICKS);
			}
			else if (name != null && name.equals(VAST_ICONVIEWTRACKING))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_ICONVIEWTRACKING);
                iconViewTracking = new IconViewTracking(p);
                p.require(XmlPullParser.END_TAG, null, VAST_ICONVIEWTRACKING);
			}
			else
			{
				skip(p);
			}
		}

	}

    public String getProgram() {
        return program;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    public String getXPosition() {
        return xPosition;
    }

    public String getYPosition() {
        return yPosition;
    }

    public String getDuration() {
        return duration;
    }

    public String getOffset() {
        return offset;
    }

    public String getApiFramework() {
        return apiFramework;
    }
}
