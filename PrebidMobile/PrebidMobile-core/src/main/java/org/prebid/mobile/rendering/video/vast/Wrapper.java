/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.video.vast;

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

    private String followAdditionalWrappers;
    private String allowMultipleAds;
    private String fallbackOnNoAd;
    private AdSystem adSystem;
    private VastUrl vastUrl;
    private Error error;
    private ArrayList<Impression> impressions;
    //SDK considers this to be an "optional" param in the video creative, right now.
    //https://github.com/InteractiveAdvertisingBureau/vast/blob/master/vast4.xsd#L1411
    private ArrayList<Creative> creatives;
    private Extensions extensions;

    public Wrapper(XmlPullParser p) throws XmlPullParserException, IOException {

        p.require(XmlPullParser.START_TAG, null, VAST_WRAPPER);

        followAdditionalWrappers = p.getAttributeValue(null, "followAdditionalWrappers");
        allowMultipleAds = p.getAttributeValue(null, "allowMultipleAds");
        fallbackOnNoAd = p.getAttributeValue(null, "fallbackOnNoAd");

        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_ADSYSTEM)) {
                p.require(XmlPullParser.START_TAG, null, VAST_ADSYSTEM);
                adSystem = new AdSystem(p);
                p.require(XmlPullParser.END_TAG, null, VAST_ADSYSTEM);
            }
            else if (name != null && name.equals(VAST_ERROR)) {
                p.require(XmlPullParser.START_TAG, null, VAST_ERROR);
                error = new Error(p);
                p.require(XmlPullParser.END_TAG, null, VAST_ERROR);
            }
            else if (name != null && name.equals(VAST_VASTADTAGURI)) {
                p.require(XmlPullParser.START_TAG, null, VAST_VASTADTAGURI);
                vastUrl = new VastUrl(p);
                p.require(XmlPullParser.END_TAG, null, VAST_VASTADTAGURI);
            }
            else if (name != null && name.equals(VAST_IMPRESSION)) {
                if (impressions == null) {
                    impressions = new ArrayList<>();
                }
                p.require(XmlPullParser.START_TAG, null, VAST_IMPRESSION);
                impressions.add(new Impression(p));
                p.require(XmlPullParser.END_TAG, null, VAST_IMPRESSION);
            }
            else if (name != null && name.equals(VAST_CREATIVES)) {
                p.require(XmlPullParser.START_TAG, null, VAST_CREATIVES);
                creatives = (new Creatives(p)).getCreatives();
                p.require(XmlPullParser.END_TAG, null, VAST_CREATIVES);
            }
            else if (name != null && name.equals(VAST_EXTENSIONS)) {
                p.require(XmlPullParser.START_TAG, null, VAST_EXTENSIONS);
                extensions = new Extensions(p);
                p.require(XmlPullParser.END_TAG, null, VAST_EXTENSIONS);
            }
            else {
                skip(p);
            }
        }
    }

    public String getAllowMultipleAds() {
        return allowMultipleAds;
    }

    public String getFallbackOnNoAd() {
        return fallbackOnNoAd;
    }

    public AdSystem getAdSystem() {
        return adSystem;
    }

    public VastUrl getVastUrl() {
        return vastUrl;
    }

    public Error getError() {
        return error;
    }

    public ArrayList<Impression> getImpressions() {
        return impressions;
    }

    public ArrayList<Creative> getCreatives() {
        return creatives;
    }

    public Extensions getExtensions() {
        return extensions;
    }
}
