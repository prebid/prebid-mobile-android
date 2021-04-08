package org.prebid.mobile.rendering.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class BaseValue extends VASTParserBase
{
    private String mValue;

	public BaseValue(XmlPullParser p) throws XmlPullParserException, IOException
	{

        mValue = readText(p);

	}

    public String getValue() {
        return mValue;
    }
}
