package org.prebid.mobile.rendering.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MediaFile extends VASTParserBase
{
    private String mId;
    private String mValue;
    private String mDelivery;
    private String mType;
    private String mBitrate;
    private String mMinBitrate;
    private String mMaxBitrate;
    private String mWidth;
    private String mHeight;
    private String mXPosition;
    private String mYPosition;
    private String mDuration;
    private String mOffset;
    private String mApiFramework;

	public MediaFile(XmlPullParser p) throws XmlPullParserException, IOException
	{

        mId = p.getAttributeValue(null, "id");
        mDelivery = p.getAttributeValue(null, "delivery");
        mType = p.getAttributeValue(null, "type");
        mBitrate = p.getAttributeValue(null, "bitrate");
        mMinBitrate = p.getAttributeValue(null, "minBitrate");
        mMaxBitrate = p.getAttributeValue(null, "maxBitrate");
        mWidth = p.getAttributeValue(null, "width");
        mHeight = p.getAttributeValue(null, "height");
        mXPosition = p.getAttributeValue(null, "xPosition");
        mYPosition = p.getAttributeValue(null, "yPosition");
        mDuration = p.getAttributeValue(null, "duration");
        mOffset = p.getAttributeValue(null, "offset");
        mApiFramework = p.getAttributeValue(null, "apiFramework");
        mValue = readText(p);
    }

    public String getId() {
        return mId;
    }

    public String getValue() {
        return mValue;
    }

    public String getDelivery() {
        return mDelivery;
    }

    public String getType() {
        return mType;
    }

    public String getBitrate() {
        return mBitrate;
    }

    public String getMinBitrate() {
        return mMinBitrate;
    }

    public String getMaxBitrate() {
        return mMaxBitrate;
    }

    public String getWidth() {
        return mWidth;
    }

    public String getHeight() {
        return mHeight;
    }

    public String getXPosition() {
        return mXPosition;
    }

    public String getYPosition() {
        return mYPosition;
    }

    public String getDuration() {
        return mDuration;
    }

    public String getOffset() {
        return mOffset;
    }

    public String getApiFramework() {
        return mApiFramework;
    }
}
