package org.prebid.mobile.javademo.utils;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;

public class PrebidUtils {

    public static void enableAdditionalFunctionality(AdUnit adUnit) {
        enableCOPPA();
        addFirstPartyData(adUnit);
        setStoredResponse();
        setRequestTimeoutMillis();
        enablePbsDebug();
    }

    private static void enableCOPPA() {
        TargetingParams.setSubjectToCOPPA(true);
    }

    private static void addFirstPartyData(AdUnit adUnit) {
        /* Access Control List */
        TargetingParams.addBidderToAccessControlList(TargetingParams.BIDDER_NAME_RUBICON_PROJECT);
        /* PBAdSlot(should be set together with Access Control List) */
        adUnit.setPbAdSlot("/1111111/homepage/med-rect-2");

        /* Global user data */
        TargetingParams.addUserData("globalUserDataKey1", "globalUserDataValue1");
        /* Global context data */
        TargetingParams.addContextData("globalContextDataKey1", "globalContextDataValue1");
        /* Ad unit context data */
        adUnit.addContextData("adunitContextDataKey1", "adunitContextDataValue1");

        /* Context keywords */
        TargetingParams.addContextKeyword("globalContextKeywordValue1");
        TargetingParams.addContextKeyword("globalContextKeywordValue2");

        /* Global user keywords */
        TargetingParams.addUserKeyword("globalUserKeywordValue1");
        TargetingParams.addUserKeyword("globalUserKeywordValue2");

        /* Ad unit context keywords */
        adUnit.addContextKeyword("adunitContextKeywordValue1");
        adUnit.addContextKeyword("adunitContextKeywordValue2");
    }

    private static void setStoredResponse() {
        PrebidMobile.setStoredAuctionResponse("111122223333");
    }

    private static void setRequestTimeoutMillis() {
        PrebidMobile.setTimeoutMillis(5_000);
    }

    private static void enablePbsDebug() {
        PrebidMobile.setPbsDebug(true);
    }

}
