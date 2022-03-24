package org.prebid.mobile.reflection;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;

public class AdUnitReflection {

    public static void setBidLoader(AdUnit adUnit, BidLoader bidLoader) {
        Reflection.setVariableTo(adUnit, "bidLoader", bidLoader);
    }

}
