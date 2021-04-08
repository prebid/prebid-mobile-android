package org.prebid.mobile.rendering.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class CompanionAds extends VASTParserBase
{
    private final static String VAST_COMPANIONADS = "CompanionAds";
    private final static String VAST_COMPANION = "Companion";

    private ArrayList<Companion> mCompanionAds;

	public CompanionAds(XmlPullParser p) throws XmlPullParserException, IOException
	{

        mCompanionAds = new ArrayList<>();

		p.require(XmlPullParser.START_TAG, null, VAST_COMPANIONADS);

		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_COMPANION))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_COMPANION);
                mCompanionAds.add(new Companion(p));
				p.require(XmlPullParser.END_TAG, null, VAST_COMPANION);
			}
			else
			{
				skip(p);
			}
		}

	}

    public ArrayList<Companion> getCompanionAds() {
        return mCompanionAds;
    }
}
