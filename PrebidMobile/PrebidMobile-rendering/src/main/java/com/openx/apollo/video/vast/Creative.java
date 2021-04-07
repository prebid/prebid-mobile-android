package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class Creative extends VASTParserBase {
    private final static String VAST_CREATIVE = "Creative";
    private final static String VAST_CREATIVEEXTENSTONS = "CreativeExtensions";
    private final static String VAST_LINEAR = "Linear";
    private final static String VAST_COMPANIONADS = "CompanionAds";
    private final static String VAST_NONLINEARADS = "NonLinearAds";

    private String mId;
    private String mSequence;
    private String mAdID;
    private String mApiFramework;

    private ArrayList<CreativeExtension> mCreativeExtensions;
    private Linear mLinear;
    private ArrayList<Companion> mCompanionAds;
    private NonLinearAds mNonLinearAds;

    public Creative(XmlPullParser p) throws XmlPullParserException, IOException {

        p.require(XmlPullParser.START_TAG, null, VAST_CREATIVE);

        mId = p.getAttributeValue(null, "id");
        mSequence = p.getAttributeValue(null, "sequence");
        mAdID = p.getAttributeValue(null, "adID");
        mApiFramework = p.getAttributeValue(null, "apiFramework");

        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_CREATIVEEXTENSTONS)) {
                p.require(XmlPullParser.START_TAG, null, VAST_CREATIVEEXTENSTONS);
                mCreativeExtensions = (new CreativeExtensions(p)).getCreativeExtenstions();
                p.require(XmlPullParser.END_TAG, null, VAST_CREATIVEEXTENSTONS);
            }
            else if (name != null && name.equals(VAST_LINEAR)) {
                p.require(XmlPullParser.START_TAG, null, VAST_LINEAR);
                mLinear = new Linear(p);
                p.require(XmlPullParser.END_TAG, null, VAST_LINEAR);
            }
            else if (name != null && name.equals(VAST_COMPANIONADS)) {
                p.require(XmlPullParser.START_TAG, null, VAST_COMPANIONADS);
                mCompanionAds = (new CompanionAds(p)).getCompanionAds();
                p.require(XmlPullParser.END_TAG, null, VAST_COMPANIONADS);
            }
            else if (name != null && name.equals(VAST_NONLINEARADS)) {
                p.require(XmlPullParser.START_TAG, null, VAST_NONLINEARADS);
                mNonLinearAds = new NonLinearAds(p);
                p.require(XmlPullParser.END_TAG, null, VAST_NONLINEARADS);
            }
            else {
                skip(p);
            }
        }
    }

    public String getId() {
        return mId;
    }

    public String getSequence() {
        return mSequence;
    }

    public String getAdID() {
        return mAdID;
    }

    public String getApiFramework() {
        return mApiFramework;
    }

    public ArrayList<CreativeExtension> getCreativeExtensions() {
        return mCreativeExtensions;
    }

    public Linear getLinear() {
        return mLinear;
    }

    public ArrayList<Companion> getCompanionAds() {
        return mCompanionAds;
    }

    public void setCompanionAds(ArrayList<Companion> companionAds) {
        mCompanionAds = companionAds;
    }

    public NonLinearAds getNonLinearAds() {
        return mNonLinearAds;
    }
}
