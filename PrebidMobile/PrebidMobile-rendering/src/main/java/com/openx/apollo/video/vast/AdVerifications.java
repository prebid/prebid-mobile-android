package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class AdVerifications extends VASTParserBase {
    private static final String VAST_VERIFICATION = "Verification";

    private final ArrayList<Verification> mVerifications;

    public AdVerifications(XmlPullParser p) throws IOException, XmlPullParserException {
        mVerifications = new ArrayList<>();
        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_VERIFICATION)) {
                p.require(XmlPullParser.START_TAG, null, VAST_VERIFICATION);
                Verification verification = new Verification(p);
                mVerifications.add(verification);
                p.require(XmlPullParser.END_TAG, null, VAST_VERIFICATION);
            }
            else {
                skip(p);
            }
        }
    }

    public ArrayList<Verification> getVerifications() {
        return mVerifications;
    }
}
