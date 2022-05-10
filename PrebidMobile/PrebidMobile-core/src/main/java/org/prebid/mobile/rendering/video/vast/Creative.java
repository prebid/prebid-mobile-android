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

public class Creative extends VASTParserBase {

    private final static String VAST_CREATIVE = "Creative";
    private final static String VAST_CREATIVEEXTENSTONS = "CreativeExtensions";
    private final static String VAST_LINEAR = "Linear";
    private final static String VAST_COMPANIONADS = "CompanionAds";
    private final static String VAST_NONLINEARADS = "NonLinearAds";

    private String id;
    private String sequence;
    private String adID;
    private String apiFramework;

    private ArrayList<CreativeExtension> creativeExtensions;
    private Linear linear;
    private ArrayList<Companion> companionAds;
    private NonLinearAds nonLinearAds;

    public Creative(XmlPullParser p) throws XmlPullParserException, IOException {

        p.require(XmlPullParser.START_TAG, null, VAST_CREATIVE);

        id = p.getAttributeValue(null, "id");
        sequence = p.getAttributeValue(null, "sequence");
        adID = p.getAttributeValue(null, "adID");
        apiFramework = p.getAttributeValue(null, "apiFramework");

        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_CREATIVEEXTENSTONS)) {
                p.require(XmlPullParser.START_TAG, null, VAST_CREATIVEEXTENSTONS);
                creativeExtensions = (new CreativeExtensions(p)).getCreativeExtenstions();
                p.require(XmlPullParser.END_TAG, null, VAST_CREATIVEEXTENSTONS);
            }
            else if (name != null && name.equals(VAST_LINEAR)) {
                p.require(XmlPullParser.START_TAG, null, VAST_LINEAR);
                linear = new Linear(p);
                p.require(XmlPullParser.END_TAG, null, VAST_LINEAR);
            }
            else if (name != null && name.equals(VAST_COMPANIONADS)) {
                p.require(XmlPullParser.START_TAG, null, VAST_COMPANIONADS);
                companionAds = (new CompanionAds(p)).getCompanionAds();
                p.require(XmlPullParser.END_TAG, null, VAST_COMPANIONADS);
            }
            else if (name != null && name.equals(VAST_NONLINEARADS)) {
                p.require(XmlPullParser.START_TAG, null, VAST_NONLINEARADS);
                nonLinearAds = new NonLinearAds(p);
                p.require(XmlPullParser.END_TAG, null, VAST_NONLINEARADS);
            }
            else {
                skip(p);
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getSequence() {
        return sequence;
    }

    public String getAdID() {
        return adID;
    }

    public String getApiFramework() {
        return apiFramework;
    }

    public ArrayList<CreativeExtension> getCreativeExtensions() {
        return creativeExtensions;
    }

    public Linear getLinear() {
        return linear;
    }

    public ArrayList<Companion> getCompanionAds() {
        return companionAds;
    }

    public void setCompanionAds(ArrayList<Companion> companionAds) {
        this.companionAds = companionAds;
    }

    public NonLinearAds getNonLinearAds() {
        return nonLinearAds;
    }
}
