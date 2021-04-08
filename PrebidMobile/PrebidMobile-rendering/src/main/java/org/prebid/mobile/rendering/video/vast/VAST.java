package org.prebid.mobile.rendering.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class VAST extends VASTParserBase
{
	private final static String VAST_START = "VAST";
	private final static String VAST_ERROR = "Error";
	private final static String VAST_AD = "Ad";

    private Error mError;
    private ArrayList<Ad> mAds;

    private String mVersion;

	public VAST(XmlPullParser p) throws XmlPullParserException, IOException
	{

		p.require(XmlPullParser.START_TAG, null, VAST_START);

        mVersion = p.getAttributeValue(null, "version");

		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_ERROR))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ERROR);
                mError = new Error(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ERROR);
			}
			else if (name != null && name.equals(VAST_AD))
			{

                if (mAds == null)
				{
					mAds = new ArrayList<>();
				}

				p.require(XmlPullParser.START_TAG, null, VAST_AD);
                mAds.add(new Ad(p));
				p.require(XmlPullParser.END_TAG, null, VAST_AD);
			}
			else
			{
				skip(p);
			}
		}
	}

    public Error getError() {
        return mError;
    }

    public ArrayList<Ad> getAds() {
        return mAds;
    }

    public String getVersion() {
        return mVersion;
    }
}
