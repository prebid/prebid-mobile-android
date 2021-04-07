package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class AdParameters extends VASTParserBase
{
    private final String mXmlEncoded;
    private String mValue;

	public AdParameters(XmlPullParser p) throws XmlPullParserException, IOException
	{

        mXmlEncoded = p.getAttributeValue(null, "xmlEncoded");
        mValue = readText(p);
    }

    public String getXmlEncoded() {
        return mXmlEncoded;
    }

    public String getValue() {
        return mValue;
	}
}
