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

public class MediaFile extends VASTParserBase {

    private String id;
    private String value;
    private String delivery;
    private String type;
    private String bitrate;
    private String minBitrate;
    private String maxBitrate;
    private String width;
    private String height;
    private String xPosition;
    private String yPosition;
    private String duration;
    private String offset;
    private String apiFramework;

    public MediaFile(XmlPullParser p) throws XmlPullParserException, IOException {

        id = p.getAttributeValue(null, "id");
        delivery = p.getAttributeValue(null, "delivery");
        type = p.getAttributeValue(null, "type");
        bitrate = p.getAttributeValue(null, "bitrate");
        minBitrate = p.getAttributeValue(null, "minBitrate");
        maxBitrate = p.getAttributeValue(null, "maxBitrate");
        width = p.getAttributeValue(null, "width");
        height = p.getAttributeValue(null, "height");
        xPosition = p.getAttributeValue(null, "xPosition");
        yPosition = p.getAttributeValue(null, "yPosition");
        duration = p.getAttributeValue(null, "duration");
        offset = p.getAttributeValue(null, "offset");
        apiFramework = p.getAttributeValue(null, "apiFramework");
        value = readText(p);
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getDelivery() {
        return delivery;
    }

    public String getType() {
        return type;
    }

    public String getBitrate() {
        return bitrate;
    }

    public String getMinBitrate() {
        return minBitrate;
    }

    public String getMaxBitrate() {
        return maxBitrate;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    public String getXPosition() {
        return xPosition;
    }

    public String getYPosition() {
        return yPosition;
    }

    public String getDuration() {
        return duration;
    }

    public String getOffset() {
        return offset;
    }

    public String getApiFramework() {
        return apiFramework;
    }
}
