package org.prebid.mobile.rendering.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class Linear extends VASTParserBase
{
	private final static String VAST_LINEAR = "Linear";
	private final static String VAST_ADPARAMETERS = "AdParameters";
	private final static String VAST_DURATION = "Duration";
	private final static String VAST_MEDIAFILES = "MediaFiles";
	private final static String VAST_TRACKINGEVENTS = "TrackingEvents";
	private final static String VAST_VIDEOCLICKS = "VideoClicks";
	private final static String VAST_ICONS = "Icons";

    private String mSkipOffset;

    private AdParameters mAdParameters;
    private Duration mDuration;
    private ArrayList<MediaFile> mMediaFiles;
    private ArrayList<Tracking> mTrackingEvents;
    private VideoClicks mVideoClicks;
    private ArrayList<Icon> mIcons;

	public Linear(XmlPullParser p) throws XmlPullParserException, IOException
	{

		p.require(XmlPullParser.START_TAG, null, VAST_LINEAR);

        mSkipOffset = p.getAttributeValue(null, "skipoffset");

		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_ADPARAMETERS))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ADPARAMETERS);
                mAdParameters = new AdParameters(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ADPARAMETERS);
			}
			else if (name != null && name.equals(VAST_DURATION))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_DURATION);
                mDuration = new Duration(p);
				p.require(XmlPullParser.END_TAG, null, VAST_DURATION);
			}
			else if (name != null && name.equals(VAST_MEDIAFILES))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_MEDIAFILES);
                mMediaFiles = (new MediaFiles(p)).getMediaFiles();
				p.require(XmlPullParser.END_TAG, null, VAST_MEDIAFILES);
			}
			else if (name != null && name.equals(VAST_TRACKINGEVENTS))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_TRACKINGEVENTS);
                mTrackingEvents = (new TrackingEvents(p)).getTrackingEvents();
				p.require(XmlPullParser.END_TAG, null, VAST_TRACKINGEVENTS);
			}
			else if (name != null && name.equals(VAST_VIDEOCLICKS))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_VIDEOCLICKS);
                mVideoClicks = new VideoClicks(p);
				p.require(XmlPullParser.END_TAG, null, VAST_VIDEOCLICKS);
			}
			else if (name != null && name.equals(VAST_ICONS))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ICONS);
                mIcons = (new Icons(p)).getIcons();
				p.require(XmlPullParser.END_TAG, null, VAST_ICONS);
			}
			else
			{
				skip(p);
			}
		}

	}

    public String getSkipOffset() {
        return mSkipOffset;
    }

    public AdParameters getAdParameters() {
        return mAdParameters;
    }

    public Duration getDuration() {
        return mDuration;
    }

    public ArrayList<MediaFile> getMediaFiles() {
        return mMediaFiles;
    }

    public ArrayList<Tracking> getTrackingEvents() {
        return mTrackingEvents;
    }

    public VideoClicks getVideoClicks() {
        return mVideoClicks;
    }

    public ArrayList<Icon> getIcons() {
        return mIcons;
    }
}
