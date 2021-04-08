package org.prebid.mobile.rendering.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class Icons extends VASTParserBase
{
    private final static String VAST_ICONS = "Icons";
    private final static String VAST_ICON = "Icon";

    private ArrayList<Icon> mIcons;

	public Icons(XmlPullParser p) throws XmlPullParserException, IOException
	{

		mIcons = new ArrayList<>();

		p.require(XmlPullParser.START_TAG, null, VAST_ICONS);
		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_ICON))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ICON);
                mIcons.add(new Icon(p));
				p.require(XmlPullParser.END_TAG, null, VAST_ICON);
			}
			else
			{
				skip(p);
			}
		}

	}

    public ArrayList<Icon> getIcons() {
        return mIcons;
    }
}
