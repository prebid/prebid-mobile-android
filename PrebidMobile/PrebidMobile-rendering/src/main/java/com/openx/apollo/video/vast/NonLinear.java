package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class NonLinear extends VASTParserBase
{
	private final static String VAST_NONLINEAR = "NonLinear";
	private final static String VAST_STATICRESOURCE = "StaticResource";
	private final static String VAST_IFRAMERESOUCE = "IFrameResource";
	private final static String VAST_HTMLRESOURCE = "HTMLResource";
	private final static String VAST_ADPARAMETERS = "AdParameters";
	private final static String VAST_NONLINEARCLICKTHROUGH = "NonLinearClickThrough";
	private final static String VAST_NONLINEARCLICKTRACKING = "NonLinearClickTracking";

    private String mId;
    private String mWidth;
    private String mHeight;
    private String mExpandedWidth;
    private String mExpandedHeight;
    private String mScalable;
    private String mMaintainAspectRatio;
    private String mMinSuggestedDuration;
    private String mApiFramework;

    private StaticResource mStaticResource;
    private IFrameResource mIFrameResource;
    private HTMLResource mHTMLResource;
    private AdParameters mAdParameters;
    private NonLinearClickThrough mNonLinearClickThrough;
    private NonLinearClickTracking mNonLinearClickTracking;

	public NonLinear(XmlPullParser p) throws XmlPullParserException, IOException
	{

		p.require(XmlPullParser.START_TAG, null, VAST_NONLINEAR);

        mId = p.getAttributeValue(null, "id");
        mWidth = p.getAttributeValue(null, "width");
        mHeight = p.getAttributeValue(null, "height");
        mExpandedWidth = p.getAttributeValue(null, "expandedWidth");
        mExpandedHeight = p.getAttributeValue(null, "expandedHeight");
        mScalable = p.getAttributeValue(null, "scalable");
        mMaintainAspectRatio = p.getAttributeValue(null, "maintainAspectRatio");
        mMinSuggestedDuration = p.getAttributeValue(null, "minSuggestedDuration");
        mApiFramework = p.getAttributeValue(null, "apiFramework");

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
			else if (name != null && name.equals(VAST_NONLINEARCLICKTHROUGH))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_NONLINEARCLICKTHROUGH);
                mNonLinearClickThrough = new NonLinearClickThrough(p);
				p.require(XmlPullParser.END_TAG, null, VAST_NONLINEARCLICKTHROUGH);
			}
			else if (name != null && name.equals(VAST_NONLINEARCLICKTRACKING))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_NONLINEARCLICKTRACKING);
                mNonLinearClickTracking = new NonLinearClickTracking(p);
				p.require(XmlPullParser.END_TAG, null, VAST_NONLINEARCLICKTRACKING);
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

    public String getExpandedWidth() {
        return mExpandedWidth;
    }

    public String getExpandedHeight() {
        return mExpandedHeight;
    }

    public String getScalable() {
        return mScalable;
    }

    public String getMaintainAspectRatio() {
        return mMaintainAspectRatio;
    }

    public String getMinSuggestedDuration() {
        return mMinSuggestedDuration;
    }

    public String getApiFramework() {
        return mApiFramework;
    }

    public StaticResource getStaticResource() {
        return mStaticResource;
    }

    public IFrameResource getIFrameResource() {
        return mIFrameResource;
    }

    public HTMLResource getHTMLResource() {
        return mHTMLResource;
    }

    public AdParameters getAdParameters() {
        return mAdParameters;
    }

    public NonLinearClickThrough getNonLinearClickThrough() {
        return mNonLinearClickThrough;
    }

    public NonLinearClickTracking getNonLinearClickTracking() {
        return mNonLinearClickTracking;
    }
}
