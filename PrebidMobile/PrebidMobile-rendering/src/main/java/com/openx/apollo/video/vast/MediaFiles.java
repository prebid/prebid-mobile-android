package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class MediaFiles extends VASTParserBase
{
    private final static String VAST_MEDIAFILES = "MediaFiles";
    private final static String VAST_MEDIAFILE = "MediaFile";

    private ArrayList<MediaFile> mMediaFiles;

	public MediaFiles(XmlPullParser p) throws XmlPullParserException, IOException
	{

		mMediaFiles = new ArrayList<>();

		p.require(XmlPullParser.START_TAG, null, VAST_MEDIAFILES);
		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_MEDIAFILE))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_MEDIAFILE);
                mMediaFiles.add(new MediaFile(p));

				p.require(XmlPullParser.END_TAG, null, VAST_MEDIAFILE);

			}
			else
			{
				skip(p);
			}
		}

	}

    public ArrayList<MediaFile> getMediaFiles() {
        return mMediaFiles;
    }
}
