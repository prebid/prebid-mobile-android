package org.prebid.mobile.rendering.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Impression extends BaseId
{
	public Impression(XmlPullParser p) throws XmlPullParserException, IOException
	{
		super(p);
	}
}
