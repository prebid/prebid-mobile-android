package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class IconClicks extends VASTParserBase
{
	private final static String VAST_ICONCLICKS = "IconClicks";
	private final static String VAST_ICONCLICKTHROUGH = "IconClickThrough";
	private final static String VAST_ICONCLICKTRACKING = "IconClickTracking";

    private IconClickThrough mIconClickThrough;
    private IconClickTracking mIconClickTracking;

	public IconClicks(XmlPullParser p) throws XmlPullParserException, IOException
	{

		p.require(XmlPullParser.START_TAG, null, VAST_ICONCLICKS);
		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_ICONCLICKTHROUGH))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ICONCLICKTHROUGH);
                mIconClickThrough = new IconClickThrough(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ICONCLICKTHROUGH);
			}
			else if (name != null && name.equals(VAST_ICONCLICKTRACKING))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ICONCLICKTRACKING);
                mIconClickTracking = new IconClickTracking(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ICONCLICKTRACKING);
			}
			else
			{
				skip(p);
			}
		}

	}

    public IconClickThrough getIconClickThrough() {
        return mIconClickThrough;
    }

    public IconClickTracking getIconClickTracking() {
        return mIconClickTracking;
    }
}
