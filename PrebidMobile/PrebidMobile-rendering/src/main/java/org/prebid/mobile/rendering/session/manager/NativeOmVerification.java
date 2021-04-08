package org.prebid.mobile.rendering.session.manager;

import org.prebid.mobile.rendering.bidding.data.ntv.NativeAdEventTracker;

public class NativeOmVerification {
    private final static String KEY_VENDOR_KEY = "vendorKey";
    private final static String KEY_VERIFICATION_PARAMETERS = "verification_parameters";

    private final String mOmidJsUrl;
    private String mVendorKey;
    private String mVerificationParameters;

    public NativeOmVerification(NativeAdEventTracker eventTracker) {
        mOmidJsUrl = eventTracker.getUrl();
        if (eventTracker.getExt() == null) {
            return;
        }
        mVendorKey = (String) eventTracker.getExt().getMap().get(KEY_VENDOR_KEY);
        mVerificationParameters = (String) eventTracker.getExt().getMap().get(KEY_VERIFICATION_PARAMETERS);
    }

    public String getOmidJsUrl() {
        return mOmidJsUrl;
    }

    public String getVendorKey() {
        return mVendorKey;
    }

    public String getVerificationParameters() {
        return mVerificationParameters;
    }
}
