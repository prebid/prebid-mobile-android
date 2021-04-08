package org.prebid.mobile.rendering.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class TrackingEvents extends VASTParserBase
{
    private final static String VAST_TRACKINGEVENTS = "TrackingEvents";
    private final static String VAST_TRACKING = "Tracking";

    private ArrayList<Tracking> mTrackingEvents;

    public TrackingEvents(XmlPullParser p) throws XmlPullParserException, IOException {

        mTrackingEvents = new ArrayList<>();

        p.require(XmlPullParser.START_TAG, null, VAST_TRACKINGEVENTS);
        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_TRACKING)) {

                p.require(XmlPullParser.START_TAG, null, VAST_TRACKING);
                mTrackingEvents.add(new Tracking(p));

                p.require(XmlPullParser.END_TAG, null, VAST_TRACKING);
            }
            else {
                skip(p);
            }
        }
    }

    public ArrayList<Tracking> getTrackingEvents() {
        return mTrackingEvents;
    }
}
