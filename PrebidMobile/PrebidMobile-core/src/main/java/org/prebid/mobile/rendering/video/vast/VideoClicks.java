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

public class VideoClicks extends VASTParserBase
{
	private final static String VAST_VIDEOCLICKS = "VideoClicks";
	private final static String VAST_CLICKTHROUGH = "ClickThrough";
	private final static String VAST_CLICKTRACKING = "ClickTracking";
	private final static String VAST_CUSTOMCLICK = "CustomClick";

	private ClickThrough mClickThrough;
	private ArrayList<ClickTracking> mClickTrackings;
	private ArrayList<CustomClick> mCustomClicks;

	public VideoClicks(XmlPullParser p) throws XmlPullParserException, IOException
	{

        mClickTrackings = new ArrayList<>();
        mCustomClicks = new ArrayList<>();

		p.require(XmlPullParser.START_TAG, null, VAST_VIDEOCLICKS);
		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_CLICKTHROUGH))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_CLICKTHROUGH);
				mClickThrough = new ClickThrough(p);
				p.require(XmlPullParser.END_TAG, null, VAST_CLICKTHROUGH);
			}
			else if (name != null && name.equals(VAST_CLICKTRACKING))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_CLICKTRACKING);
				mClickTrackings.add(new ClickTracking(p));
				
				p.require(XmlPullParser.END_TAG, null, VAST_CLICKTRACKING);
			}
			else if (name != null && name.equals(VAST_CUSTOMCLICK))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_CUSTOMCLICK);
				mCustomClicks.add(new CustomClick(p));
				
				p.require(XmlPullParser.END_TAG, null, VAST_CUSTOMCLICK);
			}
			else
			{
				skip(p);
			}
		}

	}

	public ClickThrough getClickThrough() {
		return mClickThrough;
	}

	public ArrayList<ClickTracking> getClickTrackings() {
		return mClickTrackings;
	}

	public ArrayList<CustomClick> getCustomClicks() {
		return mCustomClicks;
	}
}
