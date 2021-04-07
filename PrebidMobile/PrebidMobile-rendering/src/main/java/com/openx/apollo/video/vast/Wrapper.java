package com.openx.apollo.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class Wrapper extends VASTParserBase {

    private final static String VAST_WRAPPER = "Wrapper";
    private final static String VAST_ADSYSTEM = "AdSystem";
    private final static String VAST_IMPRESSION = "Impression";
    //per Vast2.0, it's "VASTAdTagURI" always
    private final static String VAST_VASTADTAGURI = "VASTAdTagURI";
    private final static String VAST_ERROR = "Error";
    private final static String VAST_CREATIVES = "Creatives";
    private final static String VAST_EXTENSIONS = "Extensions";

    private String mFollowAdditionalWrappers;
    private String mAllowMultipleAds;
    private String mFallbackOnNoAd;
    private AdSystem mAdSystem;
    private VastUrl mVastUrl;
    private Error mError;
    private ArrayList<Impression> mImpressions;
    //SDK considers this to be an "optional" param in the video creative, right now.
    //https://github.com/InteractiveAdvertisingBureau/vast/blob/master/vast4.xsd#L1411
    private ArrayList<Creative> mCreatives;
    private Extensions mExtensions;

    public Wrapper(XmlPullParser p) throws XmlPullParserException, IOException {

        p.require(XmlPullParser.START_TAG, null, VAST_WRAPPER);

        mFollowAdditionalWrappers = p.getAttributeValue(null, "followAdditionalWrappers");
        mAllowMultipleAds = p.getAttributeValue(null, "allowMultipleAds");
        mFallbackOnNoAd = p.getAttributeValue(null, "fallbackOnNoAd");

        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_ADSYSTEM)) {
                p.require(XmlPullParser.START_TAG, null, VAST_ADSYSTEM);
                mAdSystem = new AdSystem(p);
                p.require(XmlPullParser.END_TAG, null, VAST_ADSYSTEM);
            }
            else if (name != null && name.equals(VAST_ERROR)) {
                p.require(XmlPullParser.START_TAG, null, VAST_ERROR);
                mError = new Error(p);
                p.require(XmlPullParser.END_TAG, null, VAST_ERROR);
            }
            else if (name != null && name.equals(VAST_VASTADTAGURI)) {
                p.require(XmlPullParser.START_TAG, null, VAST_VASTADTAGURI);
                mVastUrl = new VastUrl(p);
                p.require(XmlPullParser.END_TAG, null, VAST_VASTADTAGURI);
            }
            else if (name != null && name.equals(VAST_IMPRESSION)) {
                if (mImpressions == null) {
                    mImpressions = new ArrayList<>();
                }
                p.require(XmlPullParser.START_TAG, null, VAST_IMPRESSION);
                mImpressions.add(new Impression(p));
                p.require(XmlPullParser.END_TAG, null, VAST_IMPRESSION);
            }
            else if (name != null && name.equals(VAST_CREATIVES)) {
                p.require(XmlPullParser.START_TAG, null, VAST_CREATIVES);
                mCreatives = (new Creatives(p)).getCreatives();
                p.require(XmlPullParser.END_TAG, null, VAST_CREATIVES);
            }
            else if (name != null && name.equals(VAST_EXTENSIONS)) {
                p.require(XmlPullParser.START_TAG, null, VAST_EXTENSIONS);
                mExtensions = new Extensions(p);
                p.require(XmlPullParser.END_TAG, null, VAST_EXTENSIONS);
            }
            else {
                skip(p);
            }
        }
    }

    public String getAllowMultipleAds() {
        return mAllowMultipleAds;
    }

    public String getFallbackOnNoAd() {
        return mFallbackOnNoAd;
    }

    public AdSystem getAdSystem() {
        return mAdSystem;
    }

    public VastUrl getVastUrl() {
        return mVastUrl;
    }

    public Error getError() {
        return mError;
    }

    public ArrayList<Impression> getImpressions() {
        return mImpressions;
    }

    public ArrayList<Creative> getCreatives() {
        return mCreatives;
    }

    public Extensions getExtensions() {
        return mExtensions;
    }
}
