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
