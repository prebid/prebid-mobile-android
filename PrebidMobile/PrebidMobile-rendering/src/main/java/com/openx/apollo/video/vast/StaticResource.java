package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class StaticResource extends VASTParserBase
{
    private String mCreativeType;
    private String mValue;

	public StaticResource(XmlPullParser p) throws XmlPullParserException, IOException
	{
        mCreativeType = p.getAttributeValue(null, "creativeType");
        mValue = readText(p);
    }

    public String getCreativeType() {
        return mCreativeType;
    }

    public String getValue() {
        return mValue;
	}
}
