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

public class CreativeExtensions extends VASTParserBase
{
    private final static String VAST_CREATIVEEXTENSIONS = "CreativeExtensions";
    private final static String VAST_CREATIVEEXTENSION = "CreativeExtension";

    private ArrayList<CreativeExtension> creativeExtenstions;

	public CreativeExtensions(XmlPullParser p) throws XmlPullParserException, IOException
	{

		creativeExtenstions = new ArrayList<>();

		p.require(XmlPullParser.START_TAG, null, VAST_CREATIVEEXTENSIONS);
		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_CREATIVEEXTENSION))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_CREATIVEEXTENSION);
				creativeExtenstions.add(new CreativeExtension(p));

				p.require(XmlPullParser.END_TAG, null, VAST_CREATIVEEXTENSION);

			}
			else
			{
				skip(p);
			}
		}

	}

    public ArrayList<CreativeExtension> getCreativeExtenstions() {
		return creativeExtenstions;
    }
}
