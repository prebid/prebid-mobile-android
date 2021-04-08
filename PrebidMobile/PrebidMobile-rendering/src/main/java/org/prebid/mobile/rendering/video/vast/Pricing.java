package org.prebid.mobile.rendering.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Pricing extends VASTParserBase
{
    private String mModel;
    private String mCurrency;
    private String mValue;

	public Pricing(XmlPullParser p) throws XmlPullParserException, IOException
	{
        mModel = p.getAttributeValue(null, "model");
        mCurrency = p.getAttributeValue(null, "currency");
        mValue = readText(p);
    }

    public String getModel() {
        return mModel;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public String getValue() {
        return mValue;
	}
}
