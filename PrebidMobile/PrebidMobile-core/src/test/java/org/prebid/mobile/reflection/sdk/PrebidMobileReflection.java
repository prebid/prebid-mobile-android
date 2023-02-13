package org.prebid.mobile.reflection.sdk;

import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.reflection.Reflection;

public class PrebidMobileReflection {

    public static void setCustomStatusEndpoint(String url) {
        Reflection.setStaticVariableTo(PrebidMobile.class, "customStatusEndpoint", url);
    }

    public static String getCustomStatusEndpoint() {
        return Reflection.getStaticFieldOf(PrebidMobile.class, "customStatusEndpoint");
    }

}
