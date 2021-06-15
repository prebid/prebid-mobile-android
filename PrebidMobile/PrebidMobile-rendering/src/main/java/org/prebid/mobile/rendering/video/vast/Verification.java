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

public class Verification extends VASTParserBase {
    private static final String VAST_JS_RESOURCE = "JavaScriptResource";
    private static final String VAST_VERIFICATION_PARAMETERS = "VerificationParameters";

    private String vendor;
    private String jsResource;
    private String verificationParameters;
    private String apiFramework;

    public Verification(XmlPullParser p) throws IOException, XmlPullParserException {
        this.vendor = p.getAttributeValue(null, "vendor");

        while (p.next() != XmlPullParser.END_TAG) {
            if (p.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = p.getName();
            if (name != null && name.equals(VAST_JS_RESOURCE)) {
                p.require(XmlPullParser.START_TAG, null, VAST_JS_RESOURCE);
                this.apiFramework = p.getAttributeValue(null, "apiFramework");
                this.jsResource = readText(p);
                p.require(XmlPullParser.END_TAG, null, VAST_JS_RESOURCE);
            }
            else if (name != null && name.equals(VAST_VERIFICATION_PARAMETERS)) {
                p.require(XmlPullParser.START_TAG, null, VAST_VERIFICATION_PARAMETERS);
                this.verificationParameters = readText(p);
                p.require(XmlPullParser.END_TAG, null, VAST_VERIFICATION_PARAMETERS);
            }
            else {
                skip(p);
            }
        }
    }

    public String getVendor() {
        return vendor;
    }

    public String getJsResource() {
        return jsResource;
    }

    public String getVerificationParameters() {
        return verificationParameters;
    }

    public String getApiFramework() {
        return apiFramework;
    }
}
