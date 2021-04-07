package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class BaseId extends VASTParserBase
{
    private String mId;
    private String mValue;

	public BaseId(XmlPullParser p) throws XmlPullParserException, IOException
	{

        mId = p.getAttributeValue(null, "id");
        mValue = readText(p);
    }

    public String getId() {
        return mId;
    }

    public String getValue() {
        return mValue;
    }
}
