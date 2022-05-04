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
import java.util.ArrayList;

public class Companion extends VASTParserBase
{

    private final static String VAST_COMPANION = "Companion";
    private final static String VAST_STATICRESOURCE = "StaticResource";
    private final static String VAST_IFRAMERESOUCE = "IFrameResource";
    private final static String VAST_HTMLRESOURCE = "HTMLResource";
    private final static String VAST_ADPARAMETERS = "AdParameters";
    private final static String VAST_ALTTEXT = "AltText";
    private final static String VAST_COMPANIONCLICKTHROUGH = "CompanionClickThrough";
    private final static String VAST_COMPANIONCLICKTRACKING = "CompanionClickTracking";
    private final static String VAST_TRACKINGEVENTS = "TrackingEvents";

    private String id;
    private String width;
    private String height;
    private String assetWidth;
    private String assetHeight;
    private String expandedWidth;
    private String expandedHeight;
    private String apiFramework;
    private String adSlotID;

    private StaticResource staticResource;
    private IFrameResource iFrameResource;
    private HTMLResource HTMLResource;
    private AdParameters adParameters;
    private AltText altText;
    private CompanionClickThrough companionClickThrough;
    private CompanionClickTracking companionClickTracking;
    private ArrayList<Tracking> trackingEvents;

    public Companion(XmlPullParser p) throws XmlPullParserException, IOException {

        p.require(XmlPullParser.START_TAG, null, VAST_COMPANION);

        id = p.getAttributeValue(null, "id");
        width = p.getAttributeValue(null, "width");
        height = p.getAttributeValue(null, "height");
        assetWidth = p.getAttributeValue(null, "assetWidth");
        assetHeight = p.getAttributeValue(null, "assetHeight");
        expandedWidth = p.getAttributeValue(null, "expandedWidth");
        expandedHeight = p.getAttributeValue(null, "expandedHeight");
        apiFramework = p.getAttributeValue(null, "apiFramework");
        adSlotID = p.getAttributeValue(null, "adSlotID");

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
			else if (name != null && name.equals(VAST_ALTTEXT))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_ALTTEXT);
                altText = new AltText(p);
                p.require(XmlPullParser.END_TAG, null, VAST_ALTTEXT);
			}
			else if (name != null && name.equals(VAST_COMPANIONCLICKTHROUGH))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_COMPANIONCLICKTHROUGH);
                companionClickThrough = new CompanionClickThrough(p);
                p.require(XmlPullParser.END_TAG, null, VAST_COMPANIONCLICKTHROUGH);
			}
			else if (name != null && name.equals(VAST_COMPANIONCLICKTRACKING))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_COMPANIONCLICKTRACKING);
                companionClickTracking = new CompanionClickTracking(p);
                p.require(XmlPullParser.END_TAG, null, VAST_COMPANIONCLICKTRACKING);
			}
			else if (name != null && name.equals(VAST_TRACKINGEVENTS))
			{
                p.require(XmlPullParser.START_TAG, null, VAST_TRACKINGEVENTS);
                trackingEvents = (new TrackingEvents(p)).getTrackingEvents();
                p.require(XmlPullParser.END_TAG, null, VAST_TRACKINGEVENTS);
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

    public String getAssetWidth() {
        return assetWidth;
    }

    public String getAssetHeight() {
        return assetHeight;
    }

    public String getExpandedWidth() {
        return expandedWidth;
    }

    public String getExpandedHeight() {
        return expandedHeight;
    }

    public String getApiFramework() {
        return apiFramework;
    }

    public String getAdSlotID() {
        return adSlotID;
    }

    public StaticResource getStaticResource() {
        return staticResource;
    }

    public IFrameResource getIFrameResource() {
        return iFrameResource;
    }

    public HTMLResource getHtmlResource() {
        return HTMLResource;
    }

    public AdParameters getAdParameters() {
        return adParameters;
    }

    public AltText getAltText() {
        return altText;
    }

    public CompanionClickThrough getCompanionClickThrough() {
        return companionClickThrough;
    }

    public CompanionClickTracking getCompanionClickTracking() {
        return companionClickTracking;
    }

    public ArrayList<Tracking> getTrackingEvents() {
        return trackingEvents;
    }
}
