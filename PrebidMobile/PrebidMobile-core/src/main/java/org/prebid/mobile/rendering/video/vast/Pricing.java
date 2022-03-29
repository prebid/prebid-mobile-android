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

public class Pricing extends VASTParserBase {

    private String model;
    private String currency;
    private String value;

    public Pricing(XmlPullParser p) throws XmlPullParserException, IOException {
        model = p.getAttributeValue(null, "model");
        currency = p.getAttributeValue(null, "currency");
        value = readText(p);
    }

    public String getModel() {
        return model;
    }

    public String getCurrency() {
        return currency;
    }

    public String getValue() {
        return value;
	}
}
