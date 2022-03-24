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

public class Icon extends VASTParserBase
{
	private final static String VAST_ICON = "Icon";
	private final static String VAST_STATICRESOURCE = "StaticResource";
	private final static String VAST_IFRAMERESOURCE = "IFrameResource";
	private final static String VAST_HTMLRESOURCE = "HTMLResource";
	private final static String VAST_ICONCLICKS = "IconClicks";
	private final static String VAST_ICONVIEWTRACKING = "IconViewTracking";

    private String mProgram;
    private String mWidth;
    private String mHeight;
    private String mXPosition;
    private String mYPosition;
    private String mDuration;
    private String mOffset;
    private String mApiFramework;

    private StaticResource mStaticResource;
    private IFrameResource mIFrameResource;
    private HTMLResource mHtmlResource;
    private IconClicks mIconClicks;
    private IconViewTracking mIconViewTracking;

	public Icon(XmlPullParser p) throws XmlPullParserException, IOException
	{

		p.require(XmlPullParser.START_TAG, null, VAST_ICON);

        mProgram = p.getAttributeValue(null, "program");
        mWidth = p.getAttributeValue(null, "width");
        mHeight = p.getAttributeValue(null, "height");
        mXPosition = p.getAttributeValue(null, "xPosition");
        mYPosition = p.getAttributeValue(null, "yPosition");
        mDuration = p.getAttributeValue(null, "duration");
        mOffset = p.getAttributeValue(null, "offset");
        mApiFramework = p.getAttributeValue(null, "apiFramework");

		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_STATICRESOURCE))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_STATICRESOURCE);
                mStaticResource = new StaticResource(p);
				p.require(XmlPullParser.END_TAG, null, VAST_STATICRESOURCE);
			}
			else if (name != null && name.equals(VAST_IFRAMERESOURCE))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_IFRAMERESOURCE);
                mIFrameResource = new IFrameResource(p);
				p.require(XmlPullParser.END_TAG, null, VAST_IFRAMERESOURCE);
			}
			else if (name != null && name.equals(VAST_HTMLRESOURCE))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_HTMLRESOURCE);
                mHtmlResource = new HTMLResource(p);
				p.require(XmlPullParser.END_TAG, null, VAST_HTMLRESOURCE);
			}
			else if (name != null && name.equals(VAST_ICONCLICKS))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ICONCLICKS);
                mIconClicks = new IconClicks(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ICONCLICKS);
			}
			else if (name != null && name.equals(VAST_ICONVIEWTRACKING))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ICONVIEWTRACKING);
                mIconViewTracking = new IconViewTracking(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ICONVIEWTRACKING);
			}
			else
			{
				skip(p);
			}
		}

	}

    public String getProgram() {
        return mProgram;
    }

    public String getWidth() {
        return mWidth;
    }

    public String getHeight() {
        return mHeight;
    }

    public String getXPosition() {
        return mXPosition;
    }

    public String getYPosition() {
        return mYPosition;
    }

    public String getDuration() {
        return mDuration;
    }

    public String getOffset() {
        return mOffset;
    }

    public String getApiFramework() {
        return mApiFramework;
    }
}
