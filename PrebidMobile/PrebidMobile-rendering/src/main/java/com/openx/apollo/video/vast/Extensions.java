package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class Extensions extends VASTParserBase
{
	private final static String VAST_EXTENSIONS = "Extensions";
	private final static String VAST_EXTENSION = "Extension";

	private ArrayList<Extension> mExtensions;

	public Extensions(XmlPullParser p) throws XmlPullParserException, IOException
	{
		mExtensions = new ArrayList<>();

		p.require(XmlPullParser.START_TAG, null, VAST_EXTENSIONS);
		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_EXTENSION))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_EXTENSION);
				mExtensions.add(new Extension(p));
				p.require(XmlPullParser.END_TAG, null, VAST_EXTENSION);
			}
			else
			{
				skip(p);
			}
		}
	}

	public ArrayList<Extension> getExtensions() {
		return mExtensions;
	}
}
