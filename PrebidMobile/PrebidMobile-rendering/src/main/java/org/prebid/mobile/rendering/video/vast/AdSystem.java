package org.prebid.mobile.rendering.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class AdSystem extends VASTParserBase
{
    private String mVersion;
    private String mValue;

    public AdSystem(XmlPullParser p) throws XmlPullParserException, IOException {
        mVersion = p.getAttributeValue(null, "version");
        mValue = readText(p);
    }

    public String getVersion() {
        return mVersion;
    }

    public String getValue() {
        return mValue;
    }
}