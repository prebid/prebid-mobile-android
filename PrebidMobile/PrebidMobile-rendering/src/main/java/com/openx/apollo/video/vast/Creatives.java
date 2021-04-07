package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class Creatives extends VASTParserBase
{
    private final static String VAST_CREATIVES = "Creatives";
    private final static String VAST_CREATIVE = "Creative";

    private ArrayList<Creative> mCreatives;

	public Creatives(XmlPullParser p) throws XmlPullParserException, IOException
	{

		mCreatives = new ArrayList<>();

		p.require(XmlPullParser.START_TAG, null, VAST_CREATIVES);
		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_CREATIVE))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_CREATIVE);
                mCreatives.add(new Creative(p));
				p.require(XmlPullParser.END_TAG, null, VAST_CREATIVE);
			}
			else
			{
				skip(p);
			}
		}

	}

    public ArrayList<Creative> getCreatives() {
        return mCreatives;
    }
}
