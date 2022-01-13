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

public class Ad extends VASTParserBase
{
    private final static String VAST_AD = "Ad";
	private final static String VAST_INLINE = "InLine";
	private final static String VAST_WRAPPER = "Wrapper";

    private InLine mInline;
    private Wrapper mWrapper;

    private String mId;
    private String mSequence;

	public Ad(XmlPullParser p) throws XmlPullParserException, IOException
	{

		p.require(XmlPullParser.START_TAG, null, VAST_AD);

        mId = p.getAttributeValue(null, "id");
        mSequence = p.getAttributeValue(null, "sequence");

		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_INLINE))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_INLINE);
                mInline = new InLine(p);
				p.require(XmlPullParser.END_TAG, null, VAST_INLINE);
			}
			else if (name != null && name.equals(VAST_WRAPPER))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_WRAPPER);
                mWrapper = new Wrapper(p);
				p.require(XmlPullParser.END_TAG, null, VAST_WRAPPER);
			}
			else
			{
				skip(p);
			}
		}

	}

    public InLine getInline() {
        return mInline;
    }

    public Wrapper getWrapper() {
        return mWrapper;
    }

    public String getId() {
        return mId;
    }

    public String getSequence() {
        return mSequence;
    }

}
