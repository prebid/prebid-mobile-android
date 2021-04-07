package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class CreativeExtensions extends VASTParserBase
{
    private final static String VAST_CREATIVEEXTENSIONS = "CreativeExtensions";
    private final static String VAST_CREATIVEEXTENSION = "CreativeExtension";

    private ArrayList<CreativeExtension> mCreativeExtenstions;

	public CreativeExtensions(XmlPullParser p) throws XmlPullParserException, IOException
	{

        mCreativeExtenstions = new ArrayList<>();

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
                mCreativeExtenstions.add(new CreativeExtension(p));

				p.require(XmlPullParser.END_TAG, null, VAST_CREATIVEEXTENSION);

			}
			else
			{
				skip(p);
			}
		}

	}

    public ArrayList<CreativeExtension> getCreativeExtenstions() {
        return mCreativeExtenstions;
    }
}
