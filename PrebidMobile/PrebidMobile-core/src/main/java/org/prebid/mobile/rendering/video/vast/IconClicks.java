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

public class IconClicks extends VASTParserBase {

	private final static String VAST_ICONCLICKS = "IconClicks";
	private final static String VAST_ICONCLICKTHROUGH = "IconClickThrough";
	private final static String VAST_ICONCLICKTRACKING = "IconClickTracking";

	private IconClickThrough iconClickThrough;
	private IconClickTracking iconClickTracking;

	public IconClicks(XmlPullParser p) throws XmlPullParserException, IOException {

		p.require(XmlPullParser.START_TAG, null, VAST_ICONCLICKS);
		while (p.next() != XmlPullParser.END_TAG) {
			if (p.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_ICONCLICKTHROUGH))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ICONCLICKTHROUGH);
				iconClickThrough = new IconClickThrough(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ICONCLICKTHROUGH);
			}
			else if (name != null && name.equals(VAST_ICONCLICKTRACKING))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ICONCLICKTRACKING);
				iconClickTracking = new IconClickTracking(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ICONCLICKTRACKING);
			}
			else
			{
				skip(p);
			}
		}

	}

    public IconClickThrough getIconClickThrough() {
		return iconClickThrough;
    }

    public IconClickTracking getIconClickTracking() {
		return iconClickTracking;
    }
}
