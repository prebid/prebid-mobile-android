package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Extension extends VASTParserBase
{
    private String mType;
    private AdVerifications mAdVerifications;

	private final static String VAST_AD_VERIFICATIONS = "AdVerifications";
	private final static String VAST_EXTENSION = "Extension";

	public Extension(XmlPullParser p) throws XmlPullParserException, IOException
	{
        mType = p.getAttributeValue(null, "type");

		p.require(XmlPullParser.START_TAG, null, VAST_EXTENSION);
		while (p.next() != XmlPullParser.END_TAG) {

			if (p.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = p.getName();
			if (name != null && name.equals(VAST_AD_VERIFICATIONS)) {
				p.require(XmlPullParser.START_TAG, null, VAST_AD_VERIFICATIONS);
                mAdVerifications = new AdVerifications(p);
				p.require(XmlPullParser.END_TAG, null, VAST_AD_VERIFICATIONS);
			} else {
				skip(p);
			}
		}
	}

    public String getType() {
        return mType;
    }

    public AdVerifications getAdVerifications() {
        return mAdVerifications;
    }
}
