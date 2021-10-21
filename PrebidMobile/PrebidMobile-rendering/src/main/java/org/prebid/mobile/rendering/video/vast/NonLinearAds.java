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

public class NonLinearAds extends VASTParserBase
{
	private final static String VAST_NONLINEAERADS = "NonLinearAds";
	private final static String VAST_NONLINEAR = "NonLinear";
	private final static String VAST_TRACKINGEVENTS = "TrackingEvents";

    private ArrayList<NonLinear> mNonLinearAds;
    private ArrayList<Tracking> mTrackingEvents;

	public NonLinearAds(XmlPullParser p) throws XmlPullParserException, IOException
	{

		mNonLinearAds = new ArrayList<>();

		p.require(XmlPullParser.START_TAG, null, VAST_NONLINEAERADS);

		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_NONLINEAR))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_NONLINEAR);
                mNonLinearAds.add(new NonLinear(p));
				p.require(XmlPullParser.END_TAG, null, VAST_NONLINEAR);
			}
			else if (name != null && name.equals(VAST_TRACKINGEVENTS))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_TRACKINGEVENTS);
                mTrackingEvents = (new TrackingEvents(p)).getTrackingEvents();
				p.require(XmlPullParser.END_TAG, null, VAST_TRACKINGEVENTS);
			}
			else
			{
				skip(p);
			}
		}

	}

    public ArrayList<NonLinear> getNonLinearAds() {
        return mNonLinearAds;
    }

    public ArrayList<Tracking> getTrackingEvents() {
        return mTrackingEvents;
    }
}
