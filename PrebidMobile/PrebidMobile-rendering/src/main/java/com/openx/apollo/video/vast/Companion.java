package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class Companion extends VASTParserBase
{
	private final static String VAST_COMPANION = "Companion";
	private final static String VAST_STATICRESOURCE = "StaticResource";
	private final static String VAST_IFRAMERESOUCE = "IFrameResource";
	private final static String VAST_HTMLRESOURCE = "HTMLResource";
	private final static String VAST_ADPARAMETERS = "AdParameters";
	private final static String VAST_ALTTEXT = "AltText";
	private final static String VAST_COMPANIONCLICKTHROUGH = "CompanionClickThrough";
	private final static String VAST_COMPANIONCLICKTRACKING = "CompanionClickTracking";
	private final static String VAST_TRACKINGEVENTS = "TrackingEvents";

    private String mId;
    private String mWidth;
    private String mHeight;
    private String mAssetWidth;
    private String mAssetHeight;
    private String mExpandedWidth;
    private String mExpandedHeight;
    private String mApiFramework;
    private String mAdSlotID;

    private StaticResource mStaticResource;
    private IFrameResource mIFrameResource;
    private HTMLResource mHTMLResource;
    private AdParameters mAdParameters;
    private AltText mAltText;
    private CompanionClickThrough mCompanionClickThrough;
    private CompanionClickTracking mCompanionClickTracking;
    private ArrayList<Tracking> mTrackingEvents;

	public Companion(XmlPullParser p) throws XmlPullParserException, IOException
	{

		p.require(XmlPullParser.START_TAG, null, VAST_COMPANION);

        mId = p.getAttributeValue(null, "id");
        mWidth = p.getAttributeValue(null, "width");
        mHeight = p.getAttributeValue(null, "height");
        mAssetWidth = p.getAttributeValue(null, "assetWidth");
        mAssetHeight = p.getAttributeValue(null, "assetHeight");
        mExpandedWidth = p.getAttributeValue(null, "expandedWidth");
        mExpandedHeight = p.getAttributeValue(null, "expandedHeight");
        mApiFramework = p.getAttributeValue(null, "apiFramework");
        mAdSlotID = p.getAttributeValue(null, "adSlotID");

		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_STATICRESOURCE))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_STATICRESOURCE);
                mStaticResource = new StaticResource(p);
				p.require(XmlPullParser.END_TAG, null, VAST_STATICRESOURCE);
			}
			else if (name != null && name.equals(VAST_IFRAMERESOUCE))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_IFRAMERESOUCE);
                mIFrameResource = new IFrameResource(p);
				p.require(XmlPullParser.END_TAG, null, VAST_IFRAMERESOUCE);
			}
			else if (name != null && name.equals(VAST_HTMLRESOURCE))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_HTMLRESOURCE);
                mHTMLResource = new HTMLResource(p);
				p.require(XmlPullParser.END_TAG, null, VAST_HTMLRESOURCE);
			}
			else if (name != null && name.equals(VAST_ADPARAMETERS))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ADPARAMETERS);
                mAdParameters = new AdParameters(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ADPARAMETERS);
			}
			else if (name != null && name.equals(VAST_ALTTEXT))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ALTTEXT);
                mAltText = new AltText(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ALTTEXT);
			}
			else if (name != null && name.equals(VAST_COMPANIONCLICKTHROUGH))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_COMPANIONCLICKTHROUGH);
                mCompanionClickThrough = new CompanionClickThrough(p);
				p.require(XmlPullParser.END_TAG, null, VAST_COMPANIONCLICKTHROUGH);
			}
			else if (name != null && name.equals(VAST_COMPANIONCLICKTRACKING))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_COMPANIONCLICKTRACKING);
                mCompanionClickTracking = new CompanionClickTracking(p);
				p.require(XmlPullParser.END_TAG, null, VAST_COMPANIONCLICKTRACKING);
			}
			else if (name != null && name.equals(VAST_TRACKINGEVENTS))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_TRACKINGEVENTS);
                mTrackingEvents = (new TrackingEvents(p)).getTrackingEvents();
				p.require(XmlPullParser.END_TAG, null, VAST_TRACKINGEVENTS);
			}
			else
			{
				skip(p);
			}
		}

	}

    public String getId() {
        return mId;
    }

    public String getWidth() {
        return mWidth;
    }

    public String getHeight() {
        return mHeight;
    }

    public String getAssetWidth() {
        return mAssetWidth;
    }

    public String getAssetHeight() {
        return mAssetHeight;
    }

    public String getExpandedWidth() {
        return mExpandedWidth;
    }

    public String getExpandedHeight() {
        return mExpandedHeight;
    }

    public String getApiFramework() {
        return mApiFramework;
    }

    public String getAdSlotID() {
        return mAdSlotID;
    }

    public StaticResource getStaticResource() {
        return mStaticResource;
    }

    public IFrameResource getIFrameResource() {
        return mIFrameResource;
    }

    public HTMLResource getHtmlResource() {
        return mHTMLResource;
    }

    public AdParameters getAdParameters() {
        return mAdParameters;
    }

    public AltText getAltText() {
        return mAltText;
    }

    public CompanionClickThrough getCompanionClickThrough() {
        return mCompanionClickThrough;
    }

    public CompanionClickTracking getCompanionClickTracking() {
        return mCompanionClickTracking;
    }

    public ArrayList<Tracking> getTrackingEvents() {
        return mTrackingEvents;
    }
}
