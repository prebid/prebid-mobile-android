package org.prebid.mobile.rendering.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Ad extends VASTParserBase
{
    private final static String VAST_AD = "Ad";
	private final static String VAST_INLINE = "InLine";
	private final static String VAST_WRAPPER = "Wrapper";

    private InLine mInline;
    private Wrapper mWrapper;

    private String mId;
    private String mSequence;

	public Ad(XmlPullParser p) throws XmlPullParserException, IOException
	{

		p.require(XmlPullParser.START_TAG, null, VAST_AD);

        mId = p.getAttributeValue(null, "id");
        mSequence = p.getAttributeValue(null, "sequence");

		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_INLINE))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_INLINE);
                mInline = new InLine(p);
				p.require(XmlPullParser.END_TAG, null, VAST_INLINE);
			}
			else if (name != null && name.equals(VAST_WRAPPER))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_WRAPPER);
                mWrapper = new Wrapper(p);
				p.require(XmlPullParser.END_TAG, null, VAST_WRAPPER);
			}
			else
			{
				skip(p);
			}
		}

	}

    public InLine getInline() {
        return mInline;
    }

    public Wrapper getWrapper() {
        return mWrapper;
    }

    public String getId() {
        return mId;
    }

    public String getSequence() {
        return mSequence;
    }

}
