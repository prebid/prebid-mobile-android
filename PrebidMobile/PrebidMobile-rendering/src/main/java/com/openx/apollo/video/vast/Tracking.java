package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Tracking extends VASTParserBase
{
    private String mEvent;
    private String mValue;

	public Tracking(XmlPullParser p) throws XmlPullParserException, IOException
	{
        mEvent = p.getAttributeValue(null, "event");
        mValue = readText(p);
    }

    public String getEvent() {
        return mEvent;
    }

    public void setEvent(String event) {
        mEvent = event;
    }

    public String getValue() {
        return mValue;
	}
}
